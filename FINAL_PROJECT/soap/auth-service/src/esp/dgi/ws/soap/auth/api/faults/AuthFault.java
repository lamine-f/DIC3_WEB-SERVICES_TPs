package esp.dgi.ws.soap.auth.api.faults;

import jakarta.xml.ws.WebFault;

@WebFault(name = "AuthFault", targetNamespace = "http://chatroom.dic3/auth")
public class AuthFault extends Exception {
    private final String code;

    public AuthFault(String message) {
        super(message);
        this.code = "AUTH_ERROR";
    }

    public AuthFault(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getFaultInfo() {
        return code + ": " + getMessage();
    }
}
