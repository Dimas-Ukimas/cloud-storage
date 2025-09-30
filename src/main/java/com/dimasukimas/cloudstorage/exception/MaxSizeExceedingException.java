package com.dimasukimas.cloudstorage.exception;

public class MaxSizeExceedingException extends RuntimeException {

    public MaxSizeExceedingException(String message) {
        super(message);
    }

    public MaxSizeExceedingException(String message, Throwable cause) {
        super(message, cause);
    }

}
