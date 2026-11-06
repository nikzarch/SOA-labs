package ru.nikzarch.firstService.error;

import jakarta.inject.Inject;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.nikzarch.firstService.dto.ErrorResponse;
import ru.nikzarch.firstService.i18n.Messages;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Provider
public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
    @Inject
    Messages messages;

    @Context
    HttpHeaders headers;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Locale locale = messages.locale(headers);
        List<Map<String, String>> details = exception
                .getConstraintViolations()
                .stream()
                .map(violation ->
                        Map.of(
                                "field", normalizePath(violation.getPropertyPath()),
                                "message", messages.validationText(locale,
                                        violation.getMessageTemplate(),
                                        violation.getMessage())))
                .toList();
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .language(locale)
                .header("Vary", "Accept-Language")
                .entity(new ErrorResponse(messages.text(locale, ErrorCode.REQUEST_INVALID.key()), details))
                .build();
    }

    private String normalizePath(Path path) {
        List<String> properties = new ArrayList<>();
        for (Path.Node node : path) {
            if (node.getKind() == ElementKind.PROPERTY && node.getName() != null) {
                properties.add(node.getName());
            }
        }
        return properties.isEmpty() ? path.toString() : String.join(".", properties);
    }
}
