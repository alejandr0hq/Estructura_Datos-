package airctrl.data;

import java.time.LocalDateTime;

/** Registro persistido de una operacion atendida. kind es FLIGHT o INCIDENT. */
public record ProcessedRecord(String kind, String referenceId, int priority, LocalDateTime processedAt) {
}
