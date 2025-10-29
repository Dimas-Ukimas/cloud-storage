package com.dimasukimas.cloudstorage.service;

public enum ResourceType {

    FILE("FILE"),
    DIRECTORY("DIRECTORY");

    private final String type;

    ResourceType(String type) {
        this.type = type;
    }

    @Override
    public String toString(){
        return type;
    }
}
