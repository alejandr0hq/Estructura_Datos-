package airctrl.data;

import airctrl.model.Aircraft;
import airctrl.model.Baggage;
import airctrl.model.Connection;
import airctrl.model.Flight;
import airctrl.model.Gate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CsvRepository {
    private final Path dataDirectory;

    public CsvRepository(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public DataStore load() throws IOException {
        DataStore store = loadRaw();
        validateReferences(store);
        return store;
    }

    /**
     * Carga los CSV aplicando las validaciones por fila, pero sin la validacion
     * de referencias cruzadas. Se usa para importar los datos a PostgreSQL.
     */
    public DataStore loadRaw() throws IOException {
        DataStore store = new DataStore();
        loadAircraft(store);
        loadGates(store);
        loadFlights(store);
        loadBaggage(store);
        loadConnections(store);
        return store;
    }

    private void loadAircraft(DataStore store) throws IOException {
        CsvTable table = read("aircraft.csv",
                "aircraft_id", "model", "capacity", "status");
        int rowNumber = 2;
        for (Map<String, String> row : table.rows()) {
            String id = row.get("aircraft_id").trim();
            if (id.isEmpty()) {
                store.getWarnings().add("aircraft.csv fila " + rowNumber
                        + ": identificador vacio");
                rowNumber++;
                continue;
            }
            if (store.getAircraft().containsKey(id)) {
                store.getWarnings().add("aircraft.csv fila " + rowNumber
                        + ": aeronave duplicada " + id);
                rowNumber++;
                continue;
            }
            try {
                int capacity = Integer.parseInt(row.get("capacity").trim());
                if (capacity <= 0) {
                    throw new NumberFormatException();
                }
                store.getAircraft().put(id, new Aircraft(
                        id,
                        row.get("model").trim(),
                        capacity,
                        row.get("status").trim()
                ));
            } catch (NumberFormatException exception) {
                store.getWarnings().add("aircraft.csv fila " + rowNumber
                        + ": capacidad invalida");
            }
            rowNumber++;
        }
    }

    private void loadGates(DataStore store) throws IOException {
        CsvTable table = read("gates.csv",
                "gate_id", "terminal", "status", "current_flight");
        int rowNumber = 2;
        for (Map<String, String> row : table.rows()) {
            String id = row.get("gate_id").trim();
            if (id.isEmpty()) {
                store.getWarnings().add("gates.csv fila " + rowNumber
                        + ": identificador vacio");
                rowNumber++;
                continue;
            }
            if (store.getGates().containsKey(id)) {
                store.getWarnings().add("gates.csv fila " + rowNumber
                        + ": puerta duplicada " + id);
                rowNumber++;
                continue;
            }
            String currentFlight = blankToNull(row.get("current_flight"));
            String status = row.get("status").trim();
            if ("OCCUPIED".equals(status) && currentFlight == null) {
                store.getWarnings().add("gates.csv fila " + rowNumber
                        + ": puerta ocupada sin vuelo");
            }
            store.getGates().put(id, new Gate(
                    id,
                    row.get("terminal").trim(),
                    status,
                    currentFlight
            ));
            rowNumber++;
        }
    }

    private void loadFlights(DataStore store) throws IOException {
        CsvTable table = read("flights.csv", "flight_id", "airline", "origin",
                "destination", "aircraft_id", "gate", "status", "priority");
        Set<String> seen = new HashSet<>();
        int rowNumber = 2;
        for (Map<String, String> row : table.rows()) {
            String id = row.get("flight_id").trim();
            if (id.isEmpty()) {
                store.getWarnings().add("flights.csv fila " + rowNumber
                        + ": identificador vacio");
                rowNumber++;
                continue;
            }
            if (!seen.add(id)) {
                store.getWarnings().add("flights.csv fila " + rowNumber
                        + ": vuelo duplicado " + id);
                rowNumber++;
                continue;
            }
            try {
                int priority = Integer.parseInt(row.get("priority").trim());
                if (priority < 1 || priority > 5) {
                    throw new NumberFormatException();
                }
                store.getFlights().addLast(new Flight(
                        id,
                        row.get("airline").trim(),
                        row.get("origin").trim(),
                        row.get("destination").trim(),
                        row.get("aircraft_id").trim(),
                        row.get("gate").trim(),
                        row.get("status").trim(),
                        priority
                ));
            } catch (NumberFormatException exception) {
                store.getWarnings().add("flights.csv fila " + rowNumber
                        + ": prioridad invalida");
            }
            rowNumber++;
        }
    }

    private void loadBaggage(DataStore store) throws IOException {
        CsvTable table = read("baggage.csv",
                "bag_id", "flight_id", "passenger_code", "status");
        int rowNumber = 2;
        for (Map<String, String> row : table.rows()) {
            String id = row.get("bag_id").trim();
            if (id.isEmpty()) {
                store.getWarnings().add("baggage.csv fila " + rowNumber
                        + ": identificador vacio");
                rowNumber++;
                continue;
            }
            if (store.getBaggage().containsKey(id)) {
                store.getWarnings().add("baggage.csv fila " + rowNumber
                        + ": equipaje duplicado " + id);
                rowNumber++;
                continue;
            }
            store.getBaggage().put(id, new Baggage(
                    id,
                    row.get("flight_id").trim(),
                    row.get("passenger_code").trim(),
                    row.get("status").trim()
            ));
            rowNumber++;
        }
    }

    private void loadConnections(DataStore store) throws IOException {
        CsvTable table = read("connections.csv",
                "source_location", "target_location", "distance_m");
        int rowNumber = 2;
        for (Map<String, String> row : table.rows()) {
            String source = row.get("source_location").trim();
            String target = row.get("target_location").trim();
            String rawDistance = row.get("distance_m").trim();
            Integer distance = null;
            if (source.isEmpty() || target.isEmpty()) {
                store.getWarnings().add("connections.csv fila " + rowNumber
                        + ": ubicacion vacia");
            }
            if (rawDistance.isEmpty()) {
                store.getWarnings().add("connections.csv fila " + rowNumber
                        + ": distancia vacia");
            } else {
                try {
                    distance = Integer.valueOf(rawDistance);
                    if (distance <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException exception) {
                    distance = null;
                    store.getWarnings().add("connections.csv fila " + rowNumber
                            + ": distancia invalida");
                }
            }
            store.getConnections().add(new Connection(source, target, distance));
            rowNumber++;
        }
    }

    public static void validateReferences(DataStore store) {
        Set<String> flightIds = new HashSet<>();
        for (Flight flight : store.getFlights()) {
            flightIds.add(flight.getFlightId());
            if (!store.getAircraft().containsKey(flight.getAircraftId())) {
                store.getWarnings().add("Vuelo " + flight.getFlightId()
                        + ": aeronave inexistente " + flight.getAircraftId());
            }
            if (!flight.getGate().isBlank()
                    && !store.getGates().containsKey(flight.getGate())) {
                store.getWarnings().add("Vuelo " + flight.getFlightId()
                        + ": puerta inexistente " + flight.getGate());
            }
        }
        for (Baggage baggage : store.getBaggage().values()) {
            if (!flightIds.contains(baggage.flightId())) {
                store.getWarnings().add("Equipaje " + baggage.bagId()
                        + ": vuelo inexistente " + baggage.flightId());
            }
        }
        for (Gate gate : store.getGates().values()) {
            if (gate.getCurrentFlight() != null
                    && !flightIds.contains(gate.getCurrentFlight())) {
                store.getWarnings().add("Puerta " + gate.getGateId()
                        + ": vuelo inexistente " + gate.getCurrentFlight());
            }
        }
    }

    private CsvTable read(String filename, String... requiredHeaders) throws IOException {
        Path file = dataDirectory.resolve(filename);
        if (!Files.exists(file)) {
            throw new IOException("No se encontro " + filename);
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        if (lines.isEmpty()) {
            throw new IOException(filename + " esta vacio");
        }
        String[] headers = lines.get(0).replace("\uFEFF", "").split(",", -1);
        Set<String> availableHeaders = new HashSet<>();
        for (int index = 0; index < headers.length; index++) {
            headers[index] = headers[index].trim();
            availableHeaders.add(headers[index]);
        }
        for (String required : requiredHeaders) {
            if (!availableHeaders.contains(required)) {
                throw new IOException(filename + ": falta la columna " + required);
            }
        }

        List<Map<String, String>> rows = new ArrayList<>();
        for (int lineIndex = 1; lineIndex < lines.size(); lineIndex++) {
            if (lines.get(lineIndex).isBlank()) {
                continue;
            }
            String[] values = lines.get(lineIndex).split(",", -1);
            Map<String, String> row = new HashMap<>();
            for (int column = 0; column < headers.length; column++) {
                String value = column < values.length ? values[column] : "";
                row.put(headers[column], value);
            }
            rows.add(row);
        }
        return new CsvTable(rows);
    }

    private static String blankToNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record CsvTable(List<Map<String, String>> rows) {
    }
}
