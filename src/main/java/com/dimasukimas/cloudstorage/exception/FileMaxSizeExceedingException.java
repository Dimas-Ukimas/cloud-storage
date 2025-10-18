package com.dimasukimas.cloudstorage.exception;

public class FileMaxSizeExceedingException extends RuntimeException {

    public FileMaxSizeExceedingException(String message) {
        super(message);
    }

    public FileMaxSizeExceedingException(String message, Throwable cause) {
        super(message, cause);
    }

}
