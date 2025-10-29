package com.dimasukimas.cloudstorage.exception;

public class UnauthorizedUserException extends RuntimeException {

    public UnauthorizedUserException(String message){
        super(message);
    }

    public UnauthorizedUserException(String message, Throwable cause){
        super(message, cause);
    }

}
