package com.truskovski.controller.exceptionhandler;

import com.truskovski.controller.exceptions.CitesEmptyException;
import com.truskovski.controller.exceptions.UnreachableServiceException;
import com.truskovski.model.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class ErrorsResponseHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(
            CitesEmptyException.class
    )
    ResponseEntity<ErrorResponse> handleCitiesEmpty(CitesEmptyException exception) {
        return error(404, "error.cities.empty.title", exception.getMessage());
    }

    @ExceptionHandler(
            UnreachableServiceException.class
    )
    ResponseEntity<ErrorResponse> handleUnreachableService(UnreachableServiceException exception) {
        return error(503, "error.cities.unavailable.title", exception.getMessage());
    }

    private ResponseEntity<ErrorResponse> error(int status, String messageCode, String traceCode) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                                messageSource.getMessage(messageCode, null, LocaleContextHolder.getLocale()),
                                List.of(
                                        Map.of("trace", messageSource.getMessage(traceCode, null, LocaleContextHolder.getLocale()))
                                ),
                                OffsetDateTime.now().toString())
                );
    }
}
