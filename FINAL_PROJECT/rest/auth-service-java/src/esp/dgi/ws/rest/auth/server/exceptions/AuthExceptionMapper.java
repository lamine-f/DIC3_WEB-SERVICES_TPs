package esp.dgi.ws.rest.auth.server.exceptions;

import esp.dgi.ws.rest.auth.api.dto.ErrorDTO;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Context;

@Provider
public class AuthExceptionMapper implements ExceptionMapper<AuthException> {

    @Context
    private HttpHeaders headers;

    @Override
    public Response toResponse(AuthException e) {
        MediaType type = pickType();
        return Response.status(e.getHttpStatus())
            .type(type)
            .entity(new ErrorDTO(e.getCode(), e.getMessage()))
            .build();
    }

    private MediaType pickType() {
        if (headers != null) {
            for (MediaType m : headers.getAcceptableMediaTypes()) {
                if (MediaType.APPLICATION_XML_TYPE.isCompatible(m)) return MediaType.APPLICATION_XML_TYPE;
                if (MediaType.APPLICATION_JSON_TYPE.isCompatible(m)) return MediaType.APPLICATION_JSON_TYPE;
            }
        }
        return MediaType.APPLICATION_JSON_TYPE;
    }
}
