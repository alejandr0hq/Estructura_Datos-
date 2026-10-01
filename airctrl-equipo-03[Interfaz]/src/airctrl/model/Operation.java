package airctrl.model;

import java.time.LocalDateTime;

public final class Operation {
    private final Flight flight;
    private final Incident incident;
    private final long arrivalOrder;
    private final int priority;
    private LocalDateTime processedAt;

    public Operation(Flight flight, long arrivalOrder) {
        this.flight = flight;
        this.incident = null;
        this.arrivalOrder = arrivalOrder;
        this.priority = flight.getPriority();
    }

    public Operation(Incident incident, long arrivalOrder) {
        this.flight = null;
        this.incident = incident;
        this.arrivalOrder = arrivalOrder;
        this.priority = incident.getPriority();
    }

    public void markProcessed() {
        markProcessed(LocalDateTime.now());
    }

    public void markProcessed(LocalDateTime when) {
        processedAt = when;
        if (incident != null) {
            incident.resolve();
        }
    }

    public String getReferenceId() {
        return flight != null ? flight.getFlightId() : incident.getIncidentId();
    }

    public Flight getFlight() {
        return flight;
    }

    public Incident getIncident() {
        return incident;
    }

    public boolean isFlightOperation() {
        return flight != null;
    }

    public long getArrivalOrder() {
        return arrivalOrder;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
