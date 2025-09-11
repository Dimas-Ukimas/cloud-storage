package com.dimasukimas.cloudstorage.exception;

public class DirectoryAlreadyExists  extends RuntimeException{

    public DirectoryAlreadyExists(String message){
        super(message);
    }

    public DirectoryAlreadyExists(String message, Throwable cause){
        super(message, cause);
    }

}
