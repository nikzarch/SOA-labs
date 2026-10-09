package ru.nikzarch.firstService.error;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ru.nikzarch.firstService.dto.ErrorResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Provider
public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<Map<String, String>> details = new ArrayList<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            Map<String, String> detail = new LinkedHashMap<>();
            detail.put("field", normalizePath(violation.getPropertyPath().toString()));
            detail.put("message", violation.getMessage());
            details.add(detail);
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorResponse("Некорректный запрос", details))
                .build();
    }

    private String normalizePath(String value) {
        int dot = value.indexOf('.');
        return dot >= 0 ? value.substring(dot + 1) : value;
    }
}
