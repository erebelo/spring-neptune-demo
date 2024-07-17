package com.erebelo.springneptunedemo.exception.model;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
