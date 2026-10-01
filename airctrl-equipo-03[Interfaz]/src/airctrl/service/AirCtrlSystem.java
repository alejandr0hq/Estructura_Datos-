package airctrl.service;

import airctrl.data.DataStore;
import airctrl.data.Persistence;
import airctrl.data.ProcessedRecord;
import airctrl.graph.AirportGraph;
import airctrl.model.Aircraft;
import airctrl.model.Baggage;
import airctrl.model.Flight;
import airctrl.model.Gate;
import airctrl.model.Incident;
import airctrl.model.Operation;
import airctrl.model.Route;
import airctrl.structure.EmptyStructureException;
import airctrl.structure.HashTable;
import airctrl.structure.LinkedStack;
import airctrl.structure.PriorityOperationQueue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class AirCtrlSystem {
    private final DataStore store;
    private final PriorityOperationQueue<Operation> pending = new PriorityOperationQueue<>();
    private final LinkedStack<Operation> history = new LinkedStack<>();
    private final HashTable<String, Flight> flightIndex = new HashTable<>();
    private final AirportGraph airportGraph;
    private long nextArrivalOrder = 1;
    private Persistence persistence = Persistence.NONE;

    public static final List<String> GATE_STATUSES =
            List.of("AVAILABLE", "OCCUPIED", "MAINTENANCE", "CLOSED");
    public static final List<String> BAGGAGE_STATUSES =
            List.of("CHECKED", "LOADED", "IN_TRANSIT", "DELIVERED", "MISSING");
    public static final List<String> FLIGHT_STATUSES =
            List.of("SCHEDULED", "BOARDING", "DELAYED", "EMERGENCY", "DEPARTED", "LANDED", "CANCELLED");

    public AirCtrlSystem(DataStore store) {
        this.store = store;
        Set<String> processed = new HashSet<>();
        for (ProcessedRecord record : store.getProcessedLog()) {
            processed.add(record.kind() + ":" + normalize(record.referenceId()));
        }
        for (Flight flight : store.getFlights()) {
            flightIndex.put(normalize(flight.getFlightId()), flight);
            if (flight.isPending() && !processed.contains("FLIGHT:" + normalize(flight.getFlightId()))) {
                enqueueFlight(flight);
            }
        }
        List<Incident> openIncidents = new ArrayList<>();
        for (Incident incident : store.getIncidents().values()) {
            if (!incident.isResolved()) {
                openIncidents.add(incident);
            }
        }
        openIncidents.sort(Comparator.comparing(Incident::getCreatedAt));
        for (Incident incident : openIncidents) {
            pending.enqueue(new Operation(incident, nextArrivalOrder++), incident.getPriority());
        }
        restoreHistory(store.getProcessedLog());
        airportGraph = new AirportGraph(store.getConnections());
    }

    /** Conecta la capa de persistencia (PostgreSQL). Por defecto no se guarda nada. */
    public void setPersistence(Persistence persistence) {
        this.persistence = persistence == null ? Persistence.NONE : persistence;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    private void restoreHistory(List<ProcessedRecord> records) {
        for (ProcessedRecord record : records) {
            Operation operation = null;
            if ("FLIGHT".equals(record.kind())) {
                Flight flight = flightIndex.get(normalize(record.referenceId()));
                if (flight != null) {
                    operation = new Operation(flight, nextArrivalOrder++);
                }
            } else {
                Incident incident = store.getIncidents().get(normalize(record.referenceId()));
                if (incident != null) {
                    operation = new Operation(incident, nextArrivalOrder++);
                }
            }
            if (operation != null) {
                operation.markProcessed(record.processedAt());
                history.push(operation);
            }
        }
    }

    public Flight findFlight(String flightId) {
        String id = normalize(flightId);
        Flight flight = flightIndex.get(id);
        if (flight == null) {
            throw new AirCtrlException("Vuelo " + id + " no encontrado");
        }
        return flight;
    }

    public List<Flight> findFlightsByAirline(String airline) {
        String value = normalize(airline);
        List<Flight> result = new ArrayList<>();
        for (Flight flight : store.getFlights()) {
            if (normalize(flight.getAirline()).contains(value)) {
                result.add(flight);
            }
        }
        return result;
    }

    public List<Flight> findFlightsByStatus(String status) {
        String value = normalize(status);
        List<Flight> result = new ArrayList<>();
        for (Flight flight : store.getFlights()) {
            if (normalize(flight.getStatus()).equals(value)) {
                result.add(flight);
            }
        }
        return result;
    }

    public List<Flight> getFlights() {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : store.getFlights()) {
            result.add(flight);
        }
        return result;
    }

    public Baggage findBaggage(String bagId) {
        String id = normalize(bagId);
        Baggage bag = store.getBaggage().get(id);
        if (bag == null) {
            throw new AirCtrlException("Equipaje " + id + " no encontrado");
        }
        return bag;
    }

    public void registerFlight(Flight flight) {
        String flightId = normalize(flight.getFlightId());
        if (flightId.isEmpty()) {
            throw new AirCtrlException("El identificador del vuelo es obligatorio");
        }
        if (flightIndex.containsKey(flightId)) {
            throw new AirCtrlException("El vuelo " + flightId + " ya esta registrado");
        }
        validatePriority(flight.getPriority());
        String aircraftId = normalize(flight.getAircraftId());
        if (!store.getAircraft().containsKey(aircraftId)) {
            throw new AirCtrlException("La aeronave " + aircraftId + " no existe");
        }
        if (!flight.getGate().isBlank()) {
            Gate gate = store.getGates().get(normalize(flight.getGate()));
            if (gate == null) {
                throw new AirCtrlException("La puerta " + flight.getGate() + " no existe");
            }
            if (!"AVAILABLE".equals(gate.getStatus())) {
                throw new AirCtrlException("La puerta " + gate.getGateId()
                        + " no esta disponible (" + gate.getStatus() + ")");
            }
            flight.setGate(gate.getGateId());
            persistence.insertFlight(flight, gate.getGateId());
            gate.setStatus("OCCUPIED");
            gate.setCurrentFlight(flightId);
        } else {
            persistence.insertFlight(flight, null);
        }
        store.getFlights().addLast(flight);
        flightIndex.put(flightId, flight);
        if (flight.isPending()) {
            enqueueFlight(flight);
        }
    }

    public void registerAircraft(Aircraft aircraft) {
        String id = normalize(aircraft.aircraftId());
        if (id.isEmpty()) {
            throw new AirCtrlException("El identificador de la aeronave es obligatorio");
        }
        if (store.getAircraft().containsKey(id)) {
            throw new AirCtrlException("La aeronave " + id + " ya esta registrada");
        }
        if (aircraft.capacity() <= 0) {
            throw new AirCtrlException("La capacidad debe ser mayor que cero");
        }
        persistence.insertAircraft(aircraft);
        store.getAircraft().put(id, aircraft);
    }

    public void registerBaggage(Baggage baggage) {
        String id = normalize(baggage.bagId());
        if (id.isEmpty()) {
            throw new AirCtrlException("El identificador del equipaje es obligatorio");
        }
        if (store.getBaggage().containsKey(id)) {
            throw new AirCtrlException("El equipaje " + id + " ya esta registrado");
        }
        findFlight(baggage.flightId());
        if (!BAGGAGE_STATUSES.contains(baggage.status())) {
            throw new AirCtrlException("Estado de equipaje no valido: " + baggage.status());
        }
        persistence.insertBaggage(baggage);
        store.getBaggage().put(id, baggage);
    }

    public void registerIncident(Incident incident) {
        String id = normalize(incident.getIncidentId());
        if (id.isEmpty()) {
            throw new AirCtrlException("El identificador del incidente es obligatorio");
        }
        if (store.getIncidents().containsKey(id)) {
            throw new AirCtrlException("El incidente " + id + " ya esta registrado");
        }
        validatePriority(incident.getPriority());
        persistence.insertIncident(incident);
        store.getIncidents().put(id, incident);
        pending.enqueue(new Operation(incident, nextArrivalOrder++), incident.getPriority());
    }

    public Operation nextOperation() {
        try {
            return pending.peek();
        } catch (EmptyStructureException exception) {
            throw new AirCtrlException("No hay operaciones pendientes", exception);
        }
    }

    public Operation processNext() {
        try {
            Operation operation = pending.peek();
            LocalDateTime now = LocalDateTime.now();
            persistence.recordProcessed(operation, now);
            pending.dequeue();
            operation.markProcessed(now);
            history.push(operation);
            return operation;
        } catch (EmptyStructureException exception) {
            throw new AirCtrlException("No hay operaciones pendientes", exception);
        }
    }

    public List<Operation> recentHistory(int limit) {
        List<Operation> result = new ArrayList<>();
        if (limit < 1) {
            return result;
        }
        for (Operation operation : history) {
            result.add(operation);
            if (result.size() == limit) {
                break;
            }
        }
        return result;
    }

    public void assignGate(String flightId, String gateId) {
        Flight flight = findFlight(flightId);
        String id = normalize(gateId);
        Gate gate = store.getGates().get(id);
        if (gate == null) {
            throw new AirCtrlException("Puerta " + id + " no encontrada");
        }
        if (id.equals(normalize(flight.getGate()))
                && normalize(flight.getFlightId()).equals(normalize(gate.getCurrentFlight()))) {
            return;
        }
        if (!"AVAILABLE".equals(gate.getStatus())) {
            throw new AirCtrlException("La puerta " + id
                    + " no esta disponible (" + gate.getStatus() + ")");
        }

        Gate previousGate = store.getGates().get(normalize(flight.getGate()));
        boolean releasePrevious = previousGate != null
                && normalize(flight.getFlightId())
                .equals(normalize(previousGate.getCurrentFlight()));
        persistence.assignGate(flight.getFlightId(), id,
                releasePrevious ? previousGate.getGateId() : null);
        if (releasePrevious) {
            previousGate.setStatus("AVAILABLE");
            previousGate.setCurrentFlight(null);
        }

        gate.setStatus("OCCUPIED");
        gate.setCurrentFlight(flight.getFlightId());
        flight.setGate(id);
    }

    public Route findShortestRoute(String source, String target) {
        try {
            return airportGraph.shortestRoute(source, target)
                    .orElseThrow(() -> new AirCtrlException(
                            "No existe una ruta entre " + normalize(source)
                                    + " y " + normalize(target)));
        } catch (IllegalArgumentException exception) {
            throw new AirCtrlException(exception.getMessage(), exception);
        }
    }

    public List<String> traverseConnections(String source) {
        try {
            return airportGraph.breadthFirst(source);
        } catch (IllegalArgumentException exception) {
            throw new AirCtrlException(exception.getMessage(), exception);
        }
    }

    /** Cambia el estado de un equipaje respetando el flujo CHECKED -> LOADED -> IN_TRANSIT -> DELIVERED. */
    public Baggage updateBaggageStatus(String bagId, String newStatus) {
        Baggage bag = findBaggage(bagId);
        String status = normalize(newStatus);
        if (!allowedBaggageTransitions(bag.status()).contains(status)) {
            throw new AirCtrlException("No se puede pasar el equipaje " + bag.bagId()
                    + " de " + bag.status() + " a " + status);
        }
        persistence.updateBaggageStatus(bag.bagId(), status);
        Baggage updated = new Baggage(bag.bagId(), bag.flightId(), bag.passengerCode(), status);
        store.getBaggage().put(normalize(bag.bagId()), updated);
        return updated;
    }

    public static List<String> allowedBaggageTransitions(String current) {
        return switch (current == null ? "" : current) {
            case "CHECKED" -> List.of("LOADED", "MISSING");
            case "LOADED" -> List.of("IN_TRANSIT", "MISSING");
            case "IN_TRANSIT" -> List.of("DELIVERED", "MISSING");
            case "MISSING" -> List.of("IN_TRANSIT", "DELIVERED");
            default -> List.of();
        };
    }

    /** Libera una puerta ocupada y desvincula al vuelo que la usaba. */
    public void releaseGate(String gateId) {
        Gate gate = findGate(gateId);
        if (!"OCCUPIED".equals(gate.getStatus())) {
            throw new AirCtrlException("La puerta " + gate.getGateId() + " no esta ocupada");
        }
        persistence.updateGate(gate.getGateId(), "AVAILABLE", null);
        gate.setStatus("AVAILABLE");
        gate.setCurrentFlight(null);
    }

    /** Pone una puerta libre en mantenimiento, la cierra o la vuelve a habilitar. */
    public void setGateStatus(String gateId, String newStatus) {
        Gate gate = findGate(gateId);
        String status = normalize(newStatus);
        if (!GATE_STATUSES.contains(status) || "OCCUPIED".equals(status)) {
            throw new AirCtrlException("Para ocupar una puerta asignala a un vuelo");
        }
        if ("OCCUPIED".equals(gate.getStatus())) {
            throw new AirCtrlException("La puerta " + gate.getGateId()
                    + " esta ocupada por " + gate.getCurrentFlight() + "; liberala primero");
        }
        persistence.updateGate(gate.getGateId(), status, null);
        gate.setStatus(status);
        gate.setCurrentFlight(null);
    }

    public Gate findGate(String gateId) {
        String id = normalize(gateId);
        Gate gate = store.getGates().get(id);
        if (gate == null) {
            throw new AirCtrlException("Puerta " + id + " no encontrada");
        }
        return gate;
    }

    public List<Operation> pendingOperations() {
        List<Operation> result = new ArrayList<>();
        for (Operation operation : pending) {
            result.add(operation);
        }
        return result;
    }

    public int historySize() {
        return history.size();
    }

    public List<Gate> getGates() {
        return new ArrayList<>(store.getGates().values());
    }

    public List<Gate> availableGates() {
        List<Gate> result = new ArrayList<>();
        for (Gate gate : store.getGates().values()) {
            if ("AVAILABLE".equals(gate.getStatus())) {
                result.add(gate);
            }
        }
        return result;
    }

    public List<Aircraft> getAircraft() {
        return new ArrayList<>(store.getAircraft().values());
    }

    public List<Baggage> getBaggage() {
        return new ArrayList<>(store.getBaggage().values());
    }

    public List<Baggage> baggageForFlight(String flightId) {
        String id = normalize(flightId);
        List<Baggage> result = new ArrayList<>();
        for (Baggage bag : store.getBaggage().values()) {
            if (normalize(bag.flightId()).equals(id)) {
                result.add(bag);
            }
        }
        return result;
    }

    public List<Incident> getIncidents() {
        return new ArrayList<>(store.getIncidents().values());
    }

    public List<String> getWarnings() {
        return List.copyOf(store.getWarnings());
    }

    public List<String> locations() {
        Set<String> result = new TreeSet<>();
        for (var connection : store.getConnections()) {
            if (!connection.sourceLocation().isBlank()) {
                result.add(normalize(connection.sourceLocation()));
            }
            if (!connection.targetLocation().isBlank()) {
                result.add(normalize(connection.targetLocation()));
            }
        }
        return new ArrayList<>(result);
    }

    public Map<String, Integer> summary() {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("flights", store.getFlights().size());
        result.put("aircraft", store.getAircraft().size());
        result.put("gates", store.getGates().size());
        result.put("baggage", store.getBaggage().size());
        result.put("connections", store.getConnections().size());
        result.put("locations", airportGraph.vertexCount());
        result.put("validRoutes", airportGraph.edgeCount());
        result.put("incidents", store.getIncidents().size());
        result.put("pendingOperations", pending.size());
        result.put("dataWarnings", store.getWarnings().size());
        return result;
    }

    public Map<String, Map<String, Integer>> reports() {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        result.put("Vuelos por estado", countFlightsByStatus());
        result.put("Puertas por estado", countGatesByStatus());
        result.put("Equipajes por estado", countBaggageByStatus());
        result.put("Incidentes por tipo", countIncidentsByType());
        return result;
    }

    public DataStore getStore() {
        return store;
    }

    public PriorityOperationQueue<Operation> getPending() {
        return pending;
    }

    public AirportGraph getAirportGraph() {
        return airportGraph;
    }

    private Map<String, Integer> countFlightsByStatus() {
        Map<String, Integer> result = new TreeMap<>();
        for (Flight flight : store.getFlights()) {
            result.merge(flight.getStatus(), 1, Integer::sum);
        }
        return result;
    }

    private Map<String, Integer> countGatesByStatus() {
        Map<String, Integer> result = new TreeMap<>();
        for (Gate gate : store.getGates().values()) {
            result.merge(gate.getStatus(), 1, Integer::sum);
        }
        return result;
    }

    private Map<String, Integer> countBaggageByStatus() {
        Map<String, Integer> result = new TreeMap<>();
        for (Baggage baggage : store.getBaggage().values()) {
            result.merge(baggage.status(), 1, Integer::sum);
        }
        return result;
    }

    private Map<String, Integer> countIncidentsByType() {
        Map<String, Integer> result = new TreeMap<>();
        for (Incident incident : store.getIncidents().values()) {
            result.merge(incident.getType(), 1, Integer::sum);
        }
        return result;
    }

    private void enqueueFlight(Flight flight) {
        pending.enqueue(new Operation(flight, nextArrivalOrder++), flight.getPriority());
    }

    private static void validatePriority(int priority) {
        if (priority < 1 || priority > 5) {
            throw new AirCtrlException("La prioridad debe estar entre 1 y 5");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
