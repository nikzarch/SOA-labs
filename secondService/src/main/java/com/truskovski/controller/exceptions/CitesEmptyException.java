package com.truskovski.controller.exceptions;

public class CitesEmptyException extends RuntimeException {
    public CitesEmptyException(String message) {
        super(message);
    }
}
