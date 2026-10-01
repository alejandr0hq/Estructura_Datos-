package airctrl.data;

import airctrl.model.Aircraft;
import airctrl.model.Baggage;
import airctrl.model.Connection;
import airctrl.model.Flight;
import airctrl.model.Gate;
import airctrl.model.Incident;
import airctrl.structure.SinglyLinkedList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DataStore {
    private final SinglyLinkedList<Flight> flights = new SinglyLinkedList<>();
    private final Map<String, Aircraft> aircraft = new LinkedHashMap<>();
    private final Map<String, Gate> gates = new LinkedHashMap<>();
    private final Map<String, Baggage> baggage = new LinkedHashMap<>();
    private final Map<String, Incident> incidents = new LinkedHashMap<>();
    private final List<Connection> connections = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final List<ProcessedRecord> processedLog = new ArrayList<>();

    public SinglyLinkedList<Flight> getFlights() {
        return flights;
    }

    public Map<String, Aircraft> getAircraft() {
        return aircraft;
    }

    public Map<String, Gate> getGates() {
        return gates;
    }

    public Map<String, Baggage> getBaggage() {
        return baggage;
    }

    public Map<String, Incident> getIncidents() {
        return incidents;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    /** Operaciones ya procesadas en sesiones anteriores (vacio en modo CSV). */
    public List<ProcessedRecord> getProcessedLog() {
        return processedLog;
    }
}
