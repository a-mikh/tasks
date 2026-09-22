package com.anton.tasks.exceptions.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {}

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
