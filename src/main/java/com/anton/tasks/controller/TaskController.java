package com.anton.tasks.controller;

import com.anton.tasks.dto.task.TaskCreateRequestDto;
import com.anton.tasks.dto.task.TaskResponseDto;
import com.anton.tasks.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto create(@Valid @RequestBody TaskCreateRequestDto dto) {
        return taskService.create(dto);
    }

    @PutMapping("/{taskId}/assign/{username}")
    public TaskResponseDto assignUser(@PathVariable Long taskId, @PathVariable String username) {
        return taskService.assignUser(taskId, username);
    }

    @GetMapping
    public List<TaskResponseDto> getAllTasks() {
        return taskService.getAllTasks();
    }
}
