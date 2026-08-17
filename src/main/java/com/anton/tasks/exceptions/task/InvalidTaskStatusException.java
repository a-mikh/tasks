package com.anton.tasks.exceptions.task;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class InvalidTaskStatusException extends RuntimeException {
}
