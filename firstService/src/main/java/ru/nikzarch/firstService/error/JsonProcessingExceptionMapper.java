package ru.nikzarch.firstService.error;

import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.nikzarch.firstService.dto.ErrorResponse;
import ru.nikzarch.firstService.i18n.Messages;
import ru.nikzarch.firstService.model.Climate;
import ru.nikzarch.firstService.model.Government;
import ru.nikzarch.firstService.model.StandardOfLiving;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<ProcessingException> {
    private static final String ENUM_ERROR_PREFIX = "No enum constant ";
    private static final Map<String, String> ENUM_FIELDS = Map.of(
            Climate.class.getName(), "climate",
            Government.class.getName(), "government",
            StandardOfLiving.class.getName(), "standardOfLiving"
    );

    @Inject
    Messages messages;

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(ProcessingException exception) {
        Locale locale = messages.locale(headers);
        List<Map<String, String>> details = enumError(exception, locale);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .language(locale)
                .header("Vary", "Accept-Language")
                .entity(new ErrorResponse(messages.text(locale, ErrorCode.REQUEST_INVALID.key()), details))
                .build();
    }

    private List<Map<String, String>> enumError(Throwable exception, Locale locale) {
        var cause = exception;
        while (cause != null) {
            String message = cause.getMessage();
            if (!(cause instanceof IllegalArgumentException) || message == null
                    || !message.startsWith(ENUM_ERROR_PREFIX)) {
                continue;
            }
            String qualifiedName = message.substring(ENUM_ERROR_PREFIX.length());
            int dot = qualifiedName.lastIndexOf('.');
            if (dot < 0) {
                continue;
            }
            String field = ENUM_FIELDS.get(qualifiedName.substring(0, dot));
            if (field != null) {
                String rawValue = qualifiedName.substring(dot + 1);
                return List.of(Map.of("field", field, "message",
                        messages.text(locale, ErrorCode.JSON_INVALID_ENUM.key(), rawValue)));
            }
            cause = cause.getCause();
        }
        return List.of();
    }
}
