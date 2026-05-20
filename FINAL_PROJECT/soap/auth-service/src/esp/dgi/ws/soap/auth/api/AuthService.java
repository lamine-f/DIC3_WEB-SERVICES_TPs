package esp.dgi.ws.soap.auth.api;

import esp.dgi.ws.soap.auth.api.dto.TokenInfoDTO;
import esp.dgi.ws.soap.auth.api.faults.AuthFault;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;

@WebService(targetNamespace = "http://chatroom.dic3/auth", name = "AuthService")
public interface AuthService {

    @WebMethod
    String register(@WebParam(name = "username") String username,
                    @WebParam(name = "password") String password) throws AuthFault;

    @WebMethod
    String login(@WebParam(name = "username") String username,
                 @WebParam(name = "password") String password) throws AuthFault;

    @WebMethod
    boolean logout(@WebParam(name = "token") String token);

    @WebMethod
    TokenInfoDTO validateToken(@WebParam(name = "token") String token) throws AuthFault;
}
