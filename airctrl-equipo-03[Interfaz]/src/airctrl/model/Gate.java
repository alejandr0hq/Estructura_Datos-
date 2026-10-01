package airctrl.model;

public final class Gate {
    private final String gateId;
    private final String terminal;
    private String status;
    private String currentFlight;

    public Gate(String gateId, String terminal, String status, String currentFlight) {
        this.gateId = gateId;
        this.terminal = terminal;
        this.status = status;
        this.currentFlight = currentFlight;
    }

    public String getGateId() {
        return gateId;
    }

    public String getTerminal() {
        return terminal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentFlight() {
        return currentFlight;
    }

    public void setCurrentFlight(String currentFlight) {
        this.currentFlight = currentFlight;
    }
}
