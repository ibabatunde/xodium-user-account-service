package com.xodium.useraccountservice.exceptions;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String ex) {
        super(ex);
    }
}
