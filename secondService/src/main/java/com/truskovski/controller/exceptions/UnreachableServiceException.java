package com.truskovski.controller.exceptions;

public class UnreachableServiceException extends RuntimeException {
    public UnreachableServiceException(String message) {
        super(message);
    }
}
