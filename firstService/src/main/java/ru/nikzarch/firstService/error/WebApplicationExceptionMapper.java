package ru.nikzarch.firstService.error;

import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.nikzarch.firstService.dto.ErrorResponse;
import ru.nikzarch.firstService.i18n.Messages;

import java.util.Locale;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
    @Inject
    Messages messages;

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(WebApplicationException exception) {
        int status = exception.getResponse().getStatus();
        ErrorCode code;
        if (status == Response.Status.NOT_FOUND.getStatusCode()) {
            code = ErrorCode.RESOURCE_NOT_FOUND;
        } else if (status >= 400 && status < 500) {
            code = ErrorCode.REQUEST_INVALID;
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            code = ErrorCode.INTERNAL_ERROR;
        }
        Locale locale = messages.locale(headers);
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .language(locale)
                .header("Vary", "Accept-Language")
                .entity(ErrorResponse.of(messages.text(locale, code.key())))
                .build();
    }
}
