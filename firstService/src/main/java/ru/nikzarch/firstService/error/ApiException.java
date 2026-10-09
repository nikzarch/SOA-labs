package ru.nikzarch.firstService.error;

import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.nikzarch.firstService.dto.ErrorResponse;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {
    private final int status;
    private final ErrorResponse errorResponse;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
        this.errorResponse = ErrorResponse.of(message);
    }

    public ApiException(int status, String message, List<Map<String, String>> details) {
        super(message);
        this.status = status;
        this.errorResponse = new ErrorResponse(message, details);
    }

    public static ApiException badRequest(String message) {
        return new ApiException(Response.Status.BAD_REQUEST.getStatusCode(), message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(Response.Status.NOT_FOUND.getStatusCode(), message);
    }
}
