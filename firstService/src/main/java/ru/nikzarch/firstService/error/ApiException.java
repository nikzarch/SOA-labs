package ru.nikzarch.firstService.error;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ApiException extends RuntimeException {
    private final int status;
    private final ErrorCode code;
    private final Object[] arguments;
    private final List<FieldError> fieldErrors;

    private ApiException(int status, ErrorCode code,
                         List<FieldError> fieldErrors, Object... arguments) {
        super(code.key());
        this.status = status;
        this.code = code;
        this.arguments = arguments.clone();
        this.fieldErrors = List.copyOf(fieldErrors);
    }

    public static ApiException badRequest(ErrorCode code, Object... args) {
        return new ApiException(400, code, List.of(), args);
    }

    public static ApiException notFound(ErrorCode code, Object... args) {
        return new ApiException(404, code, List.of(), args);
    }

    public static ApiException invalidField(String field, String message) {
        return new ApiException(
                Response.Status.BAD_REQUEST.getStatusCode(),
                ErrorCode.REQUEST_INVALID,
                List.of(new FieldError(field, message))
        );
    }

    public record FieldError(String field, String message) {}
}