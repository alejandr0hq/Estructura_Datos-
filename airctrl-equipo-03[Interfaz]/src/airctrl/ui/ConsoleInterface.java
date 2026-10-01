package airctrl.ui;

import airctrl.model.Aircraft;
import airctrl.model.Baggage;
import airctrl.model.Flight;
import airctrl.model.Incident;
import airctrl.model.Operation;
import airctrl.model.Route;
import airctrl.service.AirCtrlException;
import airctrl.service.AirCtrlSystem;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public final class ConsoleInterface {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AirCtrlSystem system;

    public ConsoleInterface(AirCtrlSystem system) {
        this.system = system;
    }

    public void runDemo() {
        System.out.println("AIRCTRL - Demostracion final");
        printSummary();
        printPending(5);
        printRoute("G19", "BELT02");
        try {
            Operation next = system.nextOperation();
            System.out.printf("%nSiguiente operacion: %s (prioridad %d)%n",
                    operationId(next), next.getPriority());
            Operation processed = system.processNext();
            System.out.println("Operacion procesada: " + operationLine(processed));
        } catch (AirCtrlException exception) {
            System.out.println("Aviso: " + exception.getMessage());
        }
        printHistory();
        printReports();
    }

    public void runInteractive() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("AIRCTRL - Centro de Operaciones Aeroportuarias");
        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "0" -> {
                        System.out.println("Sesion finalizada.");
                        return;
                    }
                    case "1" -> printSummary();
                    case "2" -> printFlights(system.getFlights());
                    case "3" -> printPending(null);
                    case "4" -> processNext();
                    case "5" -> findFlight(scanner);
                    case "6" -> findFlightsByAirline(scanner);
                    case "7" -> findFlightsByStatus(scanner);
                    case "8" -> findBaggage(scanner);
                    case "9" -> assignGate(scanner);
                    case "10" -> registerFlight(scanner);
                    case "11" -> registerAircraft(scanner);
                    case "12" -> registerBaggage(scanner);
                    case "13" -> registerIncident(scanner);
                    case "14" -> printHistory();
                    case "15" -> findRoute(scanner);
                    case "16" -> traverseConnections(scanner);
                    case "17" -> printReports();
                    case "18" -> printWarnings();
                    default -> System.out.println(
                            "Opcion no valida. Elige un numero del menu.");
                }
            } catch (AirCtrlException exception) {
                System.out.println("No fue posible completar la operacion: "
                        + exception.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println("\nMenu principal");
        System.out.println("1. Ver resumen");
        System.out.println("2. Listar vuelos");
        System.out.println("3. Ver operaciones pendientes");
        System.out.println("4. Procesar siguiente operacion");
        System.out.println("5. Buscar vuelo por identificador");
        System.out.println("6. Buscar vuelos por aerolinea");
        System.out.println("7. Buscar vuelos por estado");
        System.out.println("8. Buscar equipaje");
        System.out.println("9. Asignar puerta");
        System.out.println("10. Registrar vuelo");
        System.out.println("11. Registrar aeronave");
        System.out.println("12. Registrar equipaje");
        System.out.println("13. Registrar incidente");
        System.out.println("14. Ver historial reciente");
        System.out.println("15. Calcular ruta interna mas corta");
        System.out.println("16. Recorrer conexiones desde una ubicacion");
        System.out.println("17. Ver reportes");
        System.out.println("18. Ver advertencias de datos");
        System.out.println("0. Salir");
        System.out.print("Selecciona una opcion: ");
    }

    private void processNext() {
        Operation operation = system.processNext();
        System.out.println("Operacion procesada: " + operationLine(operation));
    }

    private void findFlight(Scanner scanner) {
        System.out.print("Identificador del vuelo: ");
        System.out.println(flightLine(system.findFlight(scanner.nextLine())));
    }

    private void findFlightsByAirline(Scanner scanner) {
        System.out.print("Aerolinea: ");
        printFlights(system.findFlightsByAirline(scanner.nextLine()));
    }

    private void findFlightsByStatus(Scanner scanner) {
        System.out.print("Estado: ");
        printFlights(system.findFlightsByStatus(scanner.nextLine()));
    }

    private void findBaggage(Scanner scanner) {
        System.out.print("Identificador del equipaje: ");
        System.out.println(baggageLine(system.findBaggage(scanner.nextLine())));
    }

    private void assignGate(Scanner scanner) {
        System.out.print("Identificador del vuelo: ");
        String flightId = scanner.nextLine();
        System.out.print("Puerta a asignar: ");
        String gateId = scanner.nextLine();
        system.assignGate(flightId, gateId);
        System.out.println("Puerta " + gateId.trim().toUpperCase()
                + " asignada correctamente.");
    }

    private void registerFlight(Scanner scanner) {
        System.out.print("Identificador del vuelo: ");
        String flightId = scanner.nextLine().trim().toUpperCase();
        System.out.print("Aerolinea: ");
        String airline = scanner.nextLine().trim();
        System.out.print("Origen: ");
        String origin = scanner.nextLine().trim().toUpperCase();
        System.out.print("Destino: ");
        String destination = scanner.nextLine().trim().toUpperCase();
        System.out.print("Aeronave: ");
        String aircraftId = scanner.nextLine().trim().toUpperCase();
        System.out.print("Puerta vacia o identificador: ");
        String gate = scanner.nextLine().trim().toUpperCase();
        System.out.print("Estado: ");
        String status = scanner.nextLine().trim().toUpperCase();
        int priority = readInteger(scanner, "Prioridad de 1 a 5: ");
        system.registerFlight(new Flight(flightId, airline, origin, destination,
                aircraftId, gate, status, priority));
        System.out.println("Vuelo " + flightId + " registrado.");
    }

    private void registerAircraft(Scanner scanner) {
        System.out.print("Identificador de la aeronave: ");
        String id = scanner.nextLine().trim().toUpperCase();
        System.out.print("Modelo: ");
        String model = scanner.nextLine().trim();
        int capacity = readInteger(scanner, "Capacidad: ");
        System.out.print("Estado: ");
        String status = scanner.nextLine().trim().toUpperCase();
        system.registerAircraft(new Aircraft(id, model, capacity, status));
        System.out.println("Aeronave " + id + " registrada.");
    }

    private void registerBaggage(Scanner scanner) {
        System.out.print("Identificador del equipaje: ");
        String bagId = scanner.nextLine().trim().toUpperCase();
        System.out.print("Vuelo: ");
        String flightId = scanner.nextLine().trim().toUpperCase();
        System.out.print("Codigo de pasajero: ");
        String passengerCode = scanner.nextLine().trim().toUpperCase();
        System.out.print("Estado: ");
        String status = scanner.nextLine().trim().toUpperCase();
        system.registerBaggage(new Baggage(bagId, flightId, passengerCode, status));
        System.out.println("Equipaje " + bagId + " registrado.");
    }

    private void registerIncident(Scanner scanner) {
        System.out.print("Identificador del incidente: ");
        String id = scanner.nextLine().trim().toUpperCase();
        System.out.print("Tipo: ");
        String type = scanner.nextLine().trim().toUpperCase();
        System.out.print("Descripcion: ");
        String description = scanner.nextLine().trim();
        int priority = readInteger(scanner, "Prioridad de 1 a 5: ");
        system.registerIncident(new Incident(id, type, description, priority));
        System.out.println("Incidente " + id + " registrado.");
    }

    private void findRoute(Scanner scanner) {
        System.out.print("Ubicacion inicial: ");
        String source = scanner.nextLine();
        System.out.print("Ubicacion final: ");
        String target = scanner.nextLine();
        printRoute(source, target);
    }

    private void printRoute(String source, String target) {
        Route route = system.findShortestRoute(source, target);
        System.out.println("Ruta: " + String.join(" -> ", route.locations()));
        System.out.println("Distancia total: " + route.distanceMeters() + " m");
    }

    private void traverseConnections(Scanner scanner) {
        System.out.print("Ubicacion inicial: ");
        List<String> locations = system.traverseConnections(scanner.nextLine());
        System.out.println("Recorrido BFS: " + String.join(" -> ", locations));
    }

    private void printSummary() {
        Map<String, Integer> summary = system.summary();
        System.out.println("\nResumen de datos cargados");
        System.out.println("----------------------------");
        System.out.printf("%-23s: %d%n", "Vuelos", summary.get("flights"));
        System.out.printf("%-23s: %d%n", "Aeronaves", summary.get("aircraft"));
        System.out.printf("%-23s: %d%n", "Puertas", summary.get("gates"));
        System.out.printf("%-23s: %d%n", "Equipajes", summary.get("baggage"));
        System.out.printf("%-23s: %d%n", "Conexiones recibidas", summary.get("connections"));
        System.out.printf("%-23s: %d%n", "Ubicaciones en grafo", summary.get("locations"));
        System.out.printf("%-23s: %d%n", "Rutas validas", summary.get("validRoutes"));
        System.out.printf("%-23s: %d%n", "Incidentes", summary.get("incidents"));
        System.out.printf("%-23s: %d%n", "Operaciones pendientes",
                summary.get("pendingOperations"));
        System.out.printf("%-23s: %d%n", "Advertencias de datos",
                summary.get("dataWarnings"));
    }

    private void printFlights(List<Flight> flights) {
        System.out.println("\nVuelos");
        System.out.println("------------------------------------------------------------------------------");
        if (flights.isEmpty()) {
            System.out.println("No se encontraron vuelos.");
            return;
        }
        for (Flight flight : flights) {
            System.out.println(flightLine(flight));
        }
    }

    private void printPending(Integer limit) {
        System.out.println("\nPendientes en orden de atencion");
        System.out.println("------------------------------------------------------------------------------");
        int printed = 0;
        for (Operation operation : system.getPending()) {
            if (limit != null && printed == limit) {
                break;
            }
            System.out.println(operationLine(operation));
            printed++;
        }
        if (printed == 0) {
            System.out.println("No hay operaciones pendientes.");
        }
    }

    private void printHistory() {
        System.out.println("\nHistorial reciente");
        System.out.println("------------------------------------------------------------------------------");
        List<Operation> operations = system.recentHistory(10);
        if (operations.isEmpty()) {
            System.out.println("Todavia no se han procesado operaciones.");
            return;
        }
        for (Operation operation : operations) {
            String timestamp = operation.getProcessedAt().format(DATE_FORMAT);
            System.out.println(timestamp + "  " + operationLine(operation));
        }
    }

    private void printReports() {
        System.out.println("\nReportes operativos");
        for (Map.Entry<String, Map<String, Integer>> report : system.reports().entrySet()) {
            System.out.println(report.getKey());
            if (report.getValue().isEmpty()) {
                System.out.println("  Sin registros");
            } else {
                for (Map.Entry<String, Integer> value : report.getValue().entrySet()) {
                    System.out.println("  " + value.getKey() + ": " + value.getValue());
                }
            }
        }
    }

    private void printWarnings() {
        if (system.getStore().getWarnings().isEmpty()) {
            System.out.println("No se detectaron advertencias.");
            return;
        }
        for (String warning : system.getStore().getWarnings()) {
            System.out.println("- " + warning);
        }
    }

    private static int readInteger(Scanner scanner, String prompt) {
        System.out.print(prompt);
        String value = scanner.nextLine().trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new AirCtrlException("Se esperaba un numero entero", exception);
        }
    }

    private static String flightLine(Flight flight) {
        String gate = flight.getGate().isBlank() ? "SIN PUERTA" : flight.getGate();
        return String.format("%-7s %-10s %s->%s estado=%-9s puerta=%-10s prioridad=%d",
                flight.getFlightId(), flight.getAirline(), flight.getOrigin(),
                flight.getDestination(), flight.getStatus(), gate, flight.getPriority());
    }

    private static String baggageLine(Baggage baggage) {
        String pending = baggage.isPending() ? "si" : "no";
        return String.format("%s vuelo=%s pasajero=%s estado=%s pendiente=%s",
                baggage.bagId(), baggage.flightId(), baggage.passengerCode(),
                baggage.status(), pending);
    }

    private static String operationId(Operation operation) {
        return operation.isFlightOperation()
                ? operation.getFlight().getFlightId()
                : operation.getIncident().getIncidentId();
    }

    private static String operationLine(Operation operation) {
        if (operation.isFlightOperation()) {
            return flightLine(operation.getFlight());
        }
        Incident incident = operation.getIncident();
        return String.format("%-7s incidente=%-12s prioridad=%d estado=%s",
                incident.getIncidentId(), incident.getType(), incident.getPriority(),
                incident.isResolved() ? "RESOLVED" : "OPEN");
    }
}
