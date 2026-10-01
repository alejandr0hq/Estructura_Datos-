package airctrl.model;

import java.time.LocalDateTime;

public final class Incident {
    private final String incidentId;
    private final String type;
    private final String description;
    private final int priority;
    private final LocalDateTime createdAt;
    private boolean resolved;

    public Incident(String incidentId, String type, String description, int priority) {
        this.incidentId = incidentId;
        this.type = type;
        this.description = description;
        this.priority = priority;
        this.createdAt = LocalDateTime.now();
    }

    /** Reconstruye un incidente guardado previamente (por ejemplo, desde PostgreSQL). */
    public Incident(String incidentId, String type, String description, int priority,
                    LocalDateTime createdAt, boolean resolved) {
        this.incidentId = incidentId;
        this.type = type;
        this.description = description;
        this.priority = priority;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.resolved = resolved;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void resolve() {
        resolved = true;
    }
}
