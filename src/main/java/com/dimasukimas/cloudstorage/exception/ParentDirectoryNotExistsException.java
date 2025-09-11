package com.dimasukimas.cloudstorage.exception;

public class ParentDirectoryNotExistsException extends RuntimeException {

    public ParentDirectoryNotExistsException(String message) {
        super(message);
    }

    public ParentDirectoryNotExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
