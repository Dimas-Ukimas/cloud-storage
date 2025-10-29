package com.dimasukimas.cloudstorage.exception;

public class ForbiddenMovementException extends RuntimeException {

    public ForbiddenMovementException(String message) {
        super(message);
    }

    public ForbiddenMovementException(String message, Throwable cause) {
        super(message, cause);
    }
}
