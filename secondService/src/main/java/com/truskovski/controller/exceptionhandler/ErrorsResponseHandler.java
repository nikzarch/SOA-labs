package com.truskovski.controller.exceptionhandler;

import com.truskovski.controller.exceptions.CitesEmptyException;
import com.truskovski.controller.exceptions.UnreachableServiceException;
import com.truskovski.model.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ErrorsResponseHandler {

    @ExceptionHandler(
            CitesEmptyException.class
    )
    ResponseEntity<ErrorResponse> handleCitiesEmpty(CitesEmptyException exception) {
        return error(404, "Список городов пуст" ,exception.getMessage());
    }

    @ExceptionHandler(
            UnreachableServiceException.class
    )
    ResponseEntity<ErrorResponse> handleUnreachableService(UnreachableServiceException exception) {
        return error(503, "Сервис городов не доступен" ,exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> error(int status, String message, String trace) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                                message,
                                List.of(
                                        Map.of("trace", trace)
                                ),
                                OffsetDateTime.now().toString())
                );
    }
}
