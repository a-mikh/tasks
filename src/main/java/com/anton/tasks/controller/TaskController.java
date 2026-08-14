package com.anton.tasks.controller;

import com.anton.tasks.dto.task.TaskCreateRequestDto;
import com.anton.tasks.dto.task.TaskCreateResponseDto;
import com.anton.tasks.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskCreateResponseDto create(@Valid @RequestBody TaskCreateRequestDto dto) {
        return taskService.create(dto);
    }
}
