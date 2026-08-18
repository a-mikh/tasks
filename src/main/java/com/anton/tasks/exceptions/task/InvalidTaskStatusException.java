package com.anton.tasks.exceptions.task;

public class InvalidTaskStatusException extends RuntimeException {
    public InvalidTaskStatusException(String message) {
        super(message);
    }
}
