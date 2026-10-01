package airctrl.service;

public final class AirCtrlException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AirCtrlException(String message) {
        super(message);
    }

    public AirCtrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
