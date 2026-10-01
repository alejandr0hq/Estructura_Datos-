package airctrl.data;

import airctrl.model.Aircraft;
import airctrl.model.Baggage;
import airctrl.model.Flight;
import airctrl.model.Incident;
import airctrl.model.Operation;

import java.time.LocalDateTime;

/**
 * Puerto de persistencia que usa AirCtrlSystem. Cada metodo se invoca despues de
 * validar y antes de modificar la memoria: si falla, el estado en memoria no cambia.
 */
public interface Persistence {
    Persistence NONE = new Persistence() {
    };

    default void insertFlight(Flight flight, String occupiedGateId) {
    }

    default void insertAircraft(Aircraft aircraft) {
    }

    default void insertBaggage(Baggage baggage) {
    }

    default void updateBaggageStatus(String bagId, String status) {
    }

    default void insertIncident(Incident incident) {
    }

    default void updateGate(String gateId, String status, String currentFlight) {
    }

    default void assignGate(String flightId, String newGateId, String releasedGateId) {
    }

    default void recordProcessed(Operation operation, LocalDateTime processedAt) {
    }

    default boolean isPersistent() {
        return false;
    }

    default String describe() {
        return "Archivos CSV (los cambios no se guardan)";
    }
}
