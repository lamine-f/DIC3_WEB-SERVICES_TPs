package esp.dgi.ws.rest.auth.server.resources;

import esp.dgi.ws.rest.auth.api.dto.CredentialsDTO;
import esp.dgi.ws.rest.auth.api.dto.TokenDTO;
import esp.dgi.ws.rest.auth.api.dto.TokenInfoDTO;
import esp.dgi.ws.rest.auth.server.exceptions.AuthException;
import esp.dgi.ws.rest.auth.server.services.AuthService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class AuthResource {

    private final AuthService auth = new AuthService();

    @POST
    @Path("/register")
    public TokenDTO register(CredentialsDTO body) {
        return new TokenDTO(auth.register(body.getUsername(), body.getPassword()));
    }

    @POST
    @Path("/login")
    public TokenDTO login(CredentialsDTO body) {
        return new TokenDTO(auth.login(body.getUsername(), body.getPassword()));
    }

    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String authorization) {
        String token = extractBearer(authorization);
        auth.logout(token);
        return Response.noContent().build();
    }

    @GET
    @Path("/validate")
    public TokenInfoDTO validate(@HeaderParam("Authorization") String authorization) {
        String token = extractBearer(authorization);
        return auth.validateToken(token);
    }

    private static String extractBearer(String authorization) {
        if (authorization == null) {
            throw new AuthException(401, "MISSING_TOKEN", "en-tete Authorization manquant");
        }
        String trimmed = authorization.trim();
        if (trimmed.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return trimmed.substring(7).trim();
        }
        return trimmed;
    }
}
