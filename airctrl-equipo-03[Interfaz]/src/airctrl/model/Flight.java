package airctrl.model;

import java.util.Set;

public final class Flight {
    private static final Set<String> FINAL_STATUSES = Set.of("DEPARTED", "LANDED", "CANCELLED");

    private final String flightId;
    private final String airline;
    private final String origin;
    private final String destination;
    private final String aircraftId;
    private String gate;
    private final String status;
    private final int priority;

    public Flight(String flightId, String airline, String origin, String destination,
                  String aircraftId, String gate, String status, int priority) {
        this.flightId = flightId;
        this.airline = airline;
        this.origin = origin;
        this.destination = destination;
        this.aircraftId = aircraftId;
        this.gate = gate;
        this.status = status;
        this.priority = priority;
    }

    public boolean isPending() {
        return !FINAL_STATUSES.contains(status);
    }

    public String getFlightId() {
        return flightId;
    }

    public String getAirline() {
        return airline;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getAircraftId() {
        return aircraftId;
    }

    public String getGate() {
        return gate;
    }

    public void setGate(String gate) {
        this.gate = gate;
    }

    public String getStatus() {
        return status;
    }

    public int getPriority() {
        return priority;
    }
}
