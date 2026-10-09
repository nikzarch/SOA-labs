package com.truskovski.controller.exceptionhandler;

import com.truskovski.controller.exceptions.CitesEmptyException;
import com.truskovski.controller.exceptions.UnreachableServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorsResponseHandler {

    @ExceptionHandler(
            CitesEmptyException.class
    )
    ResponseEntity<?> handleCtitesEmpty() {
        return null;
    }

    @ExceptionHandler(
            UnreachableServiceException.class
    )
    ResponseEntity<?> handleUnreachableService() {
        return null;
    }

}
