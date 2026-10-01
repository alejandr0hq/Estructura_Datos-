package tests;

import airctrl.data.CsvRepository;
import airctrl.data.DataStore;
import airctrl.model.Aircraft;
import airctrl.model.Baggage;
import airctrl.model.Connection;
import airctrl.model.Flight;
import airctrl.model.Gate;
import airctrl.model.Incident;
import airctrl.model.Operation;
import airctrl.model.Route;
import airctrl.service.AirCtrlException;
import airctrl.service.AirCtrlSystem;
import airctrl.structure.EmptyStructureException;
import airctrl.structure.HashTable;
import airctrl.structure.LinkedQueue;
import airctrl.structure.LinkedStack;
import airctrl.structure.PriorityOperationQueue;
import airctrl.structure.SinglyLinkedList;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public class AllTests {
    private static int passed;
    private static int failed;
    private static DataStore loadedStore;

    public static void main(String[] args) throws Exception {
        loadedStore = new CsvRepository(Path.of("data")).load();

        run("Linked list insertion and search", "2:XA901", () -> {
            SinglyLinkedList<String> list = new SinglyLinkedList<>();
            list.addLast("AM101");
            list.addLast("XA901");
            return list.size() + ":" + list.find("XA901"::equals);
        });
        run("Queue keeps FIFO order", "AM101", () -> {
            LinkedQueue<String> queue = new LinkedQueue<>();
            queue.enqueue("AM101");
            queue.enqueue("XA901");
            return queue.dequeue();
        });
        run("Empty queue is controlled", "EmptyStructureException", () -> {
            try {
                new LinkedQueue<>().dequeue();
                return "NO_EXCEPTION";
            } catch (EmptyStructureException exception) {
                return exception.getClass().getSimpleName();
            }
        });
        run("Stack keeps LIFO order", "XA901", () -> {
            LinkedStack<String> stack = new LinkedStack<>();
            stack.push("AM101");
            stack.push("XA901");
            return stack.pop();
        });
        run("Priority five is served first", "EMERGENCY", () -> {
            PriorityOperationQueue<String> queue = new PriorityOperationQueue<>();
            queue.enqueue("NORMAL", 2);
            queue.enqueue("EMERGENCY", 5);
            return queue.dequeue();
        });
        run("Same priority keeps arrival order", "FIRST", () -> {
            PriorityOperationQueue<String> queue = new PriorityOperationQueue<>();
            queue.enqueue("FIRST", 3);
            queue.enqueue("SECOND", 3);
            return queue.dequeue();
        });
        run("Invalid priority is rejected", "IllegalArgumentException", () -> {
            try {
                new PriorityOperationQueue<>().enqueue("INVALID", 6);
                return "NO_EXCEPTION";
            } catch (IllegalArgumentException exception) {
                return exception.getClass().getSimpleName();
            }
        });
        run("Hash table resolves collisions", "one:two", () -> {
            HashTable<String, String> table = new HashTable<>();
            table.put("Aa", "one");
            table.put("BB", "two");
            return table.get("Aa") + ":" + table.get("BB");
        });
        run("CSV files load valid records", "50:33:45:140:35", () ->
                loadedStore.getFlights().size() + ":"
                        + loadedStore.getAircraft().size() + ":"
                        + loadedStore.getGates().size() + ":"
                        + loadedStore.getBaggage().size() + ":"
                        + loadedStore.getConnections().size());
        run("CSV validation reports inconsistencies", 12,
                () -> loadedStore.getWarnings().size());
        run("Flight search ignores letter case", "AM101", () ->
                new AirCtrlSystem(loadedStore).findFlight("am101").getFlightId());
        run("Airline search returns matching flights", true, () ->
                new AirCtrlSystem(loadedStore).findFlightsByAirline("AirMex").stream()
                        .allMatch(flight -> "AirMex".equals(flight.getAirline())));
        run("Status search returns matching flights", true, () ->
                new AirCtrlSystem(loadedStore).findFlightsByStatus("DELAYED").stream()
                        .allMatch(flight -> "DELAYED".equals(flight.getStatus())));
        run("Aircraft registration updates storage", 2, () -> {
            AirCtrlSystem system = emptySystem();
            system.registerAircraft(new Aircraft("AC200", "New Model", 150, "ACTIVE"));
            return system.summary().get("aircraft");
        });
        run("Invalid aircraft capacity is rejected", "AirCtrlException", () -> {
            AirCtrlSystem system = emptySystem();
            Aircraft aircraft = new Aircraft("AC200", "New Model", 0, "ACTIVE");
            return exceptionName(() -> system.registerAircraft(aircraft));
        });
        run("Normal flight becomes pending", "TF100", () -> {
            AirCtrlSystem system = emptySystem();
            system.registerFlight(flight("TF100", 2));
            return system.nextOperation().getFlight().getFlightId();
        });
        run("Emergency overtakes normal flight", "TF500", () -> {
            AirCtrlSystem system = emptySystem();
            system.registerFlight(flight("TF100", 2));
            system.registerFlight(flight("TF500", 5));
            return system.nextOperation().getFlight().getFlightId();
        });
        run("Duplicate flight is rejected", "AirCtrlException", () -> {
            AirCtrlSystem system = emptySystem();
            system.registerFlight(flight("TF100", 2));
            return exceptionName(() -> system.registerFlight(flight("TF100", 2)));
        });
        run("Missing aircraft is rejected", "AirCtrlException", () -> {
            AirCtrlSystem system = emptySystem();
            Flight flight = new Flight("TF200", "Test", "MEX", "TIJ",
                    "AC999", "", "SCHEDULED", 2);
            return exceptionName(() -> system.registerFlight(flight));
        });
        run("Occupied gate is rejected", "AirCtrlException", () -> {
            AirCtrlSystem system = emptySystem();
            system.registerFlight(flight("TF100", 2));
            return exceptionName(() -> system.assignGate("TF100", "G02"));
        });
        run("Available gate is assigned", "OCCUPIED:TF100", () -> {
            AirCtrlSystem system = emptySystem();
            system.registerFlight(flight("TF100", 2));
            system.assignGate("TF100", "G01");
            Gate gate = system.getStore().getGates().get("G01");
            return gate.getStatus() + ":" + gate.getCurrentFlight();
        });
        run("Missing baggage is controlled", "AirCtrlException", () ->
                exceptionName(() -> emptySystem().findBaggage("BAG999")));
        run("Baggage requires an existing flight", "AirCtrlException", () -> {
            AirCtrlSystem system = emptySystem();
            Baggage baggage = new Baggage("BAG900", "ZZ999", "PAX900", "CHECKED");
            return exceptionName(() -> system.registerBaggage(baggage));
        });
        run("Baggage registration links an existing flight", "TF100", () -> {
            AirCtrlSystem system = emptySystem();
            system.registerFlight(flight("TF100", 2));
            system.registerBaggage(new Baggage(
                    "BAG100", "TF100", "PAX100", "CHECKED"));
            return system.findBaggage("BAG100").flightId();
        });
        run("Processed operation enters history", "1:true", () -> {
            AirCtrlSystem system = emptySystem();
            system.registerFlight(flight("TF100", 2));
            Operation operation = system.processNext();
            return system.recentHistory(10).size() + ":"
                    + (operation.getProcessedAt() != null);
        });
        run("Empty operation queue is controlled", "AirCtrlException", () ->
                exceptionName(() -> emptySystem().processNext()));
        run("Incident appears in reports", 1, () -> {
            AirCtrlSystem system = emptySystem();
            system.registerIncident(new Incident("INC01", "SECURITY", "Test", 5));
            return system.reports().get("Incidentes por tipo").get("SECURITY");
        });
        run("Incident joins the priority queue", "INC01", () -> {
            AirCtrlSystem system = emptySystem();
            system.registerFlight(flight("TF100", 2));
            system.registerIncident(new Incident("INC01", "SECURITY", "Test", 5));
            return system.nextOperation().getIncident().getIncidentId();
        });
        run("Operational reports count flight status", 12, () ->
                new AirCtrlSystem(loadedStore).reports()
                        .get("Vuelos por estado").get("SCHEDULED"));
        run("Dijkstra finds the shortest route", "G19>G06>BELT02:1304", () -> {
            Route route = new AirCtrlSystem(loadedStore)
                    .findShortestRoute("G19", "BELT02");
            return String.join(">", route.locations()) + ":" + route.distanceMeters();
        });
        run("BFS traverses a connected component", true, () -> {
            List<String> locations = new AirCtrlSystem(loadedStore)
                    .traverseConnections("G19");
            return locations.contains("G19") && locations.contains("BELT02");
        });
        run("BFS includes connections without distance", true, () -> {
            List<String> locations = new AirCtrlSystem(loadedStore)
                    .traverseConnections("BAGROOM-1");
            return locations.contains("BAGROOM-1") && locations.contains("BELT03");
        });
        run("Unknown graph location is controlled", "AirCtrlException", () ->
                exceptionName(() -> new AirCtrlSystem(loadedStore)
                        .findShortestRoute("UNKNOWN", "G19")));
        run("Disconnected route is controlled", "AirCtrlException", () -> {
            DataStore store = new DataStore();
            store.getConnections().add(new Connection("A", "B", 10));
            store.getConnections().add(new Connection("C", "D", 20));
            AirCtrlSystem system = new AirCtrlSystem(store);
            return exceptionName(() -> system.findShortestRoute("A", "D"));
        });

        System.out.println("\nTOTAL: " + (passed + failed)
                + " | PASSED: " + passed + " | FAILED: " + failed);
        if (failed > 0) {
            throw new AssertionError(failed + " pruebas fallaron");
        }
    }

    private static AirCtrlSystem emptySystem() {
        DataStore store = new DataStore();
        store.getAircraft().put("AC100",
                new Aircraft("AC100", "Test Model", 100, "ACTIVE"));
        store.getGates().put("G01", new Gate("G01", "T1", "AVAILABLE", null));
        store.getGates().put("G02", new Gate("G02", "T1", "OCCUPIED", "OTHER"));
        return new AirCtrlSystem(store);
    }

    private static Flight flight(String id, int priority) {
        return new Flight(id, "Test", "MEX", "TIJ",
                "AC100", "", "SCHEDULED", priority);
    }

    private static String exceptionName(Action action) {
        try {
            action.execute();
            return "NO_EXCEPTION";
        } catch (AirCtrlException exception) {
            return exception.getClass().getSimpleName();
        }
    }

    private static void run(String name, Object expected, TestCase test) {
        Object obtained;
        try {
            obtained = test.execute();
        } catch (Exception exception) {
            obtained = "ERROR:" + exception.getClass().getSimpleName()
                    + ":" + exception.getMessage();
        }
        boolean success = Objects.equals(expected, obtained);
        if (success) {
            passed++;
        } else {
            failed++;
        }
        System.out.println((success ? "PASS" : "FAIL") + " | " + name
                + " | expected=" + expected + " | obtained=" + obtained);
    }

    @FunctionalInterface
    private interface TestCase {
        Object execute() throws Exception;
    }

    @FunctionalInterface
    private interface Action {
        void execute();
    }
}
