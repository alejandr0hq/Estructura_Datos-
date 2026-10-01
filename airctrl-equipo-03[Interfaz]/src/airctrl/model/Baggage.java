package airctrl.model;

public record Baggage(String bagId, String flightId, String passengerCode, String status) {
    public boolean isPending() {
        return !"DELIVERED".equals(status);
    }
}
