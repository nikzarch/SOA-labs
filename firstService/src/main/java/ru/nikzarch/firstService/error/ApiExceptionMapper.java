package ru.nikzarch.firstService.error;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.nikzarch.firstService.dto.ErrorResponse;
import ru.nikzarch.firstService.i18n.Messages;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Provider
public class ApiExceptionMapper implements ExceptionMapper<ApiException> {
    @Inject
    Messages messages;

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(ApiException exception) {
        Locale locale = messages.locale(headers);

        List<Map<String, String>> details = exception.getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.field(),
                        "message", messages.text(locale, error.message())
                ))
                .toList();

        return Response.status(exception.getStatus())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .language(locale)
                .header("Vary", "Accept-Language")
                .entity(new ErrorResponse(
                        messages.text(
                                locale,
                                exception.getCode().key(),
                                exception.getArguments()
                        ),
                        details
                ))
                .build();
    }
}
