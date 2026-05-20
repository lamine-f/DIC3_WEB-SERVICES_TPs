package esp.dgi.ws.rest.auth.server.exceptions;

public class AuthException extends RuntimeException {
    private final int httpStatus;
    private final String code;

    public AuthException(int httpStatus, String code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public int getHttpStatus() { return httpStatus; }
    public String getCode() { return code; }
}
