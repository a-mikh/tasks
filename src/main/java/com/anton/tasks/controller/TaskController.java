package com.anton.tasks.controller;

import com.anton.tasks.dto.PageResponseDto;
import com.anton.tasks.dto.task.TaskCreateRequestDto;
import com.anton.tasks.dto.task.TaskResponseDto;
import com.anton.tasks.model.TaskStatus;
import com.anton.tasks.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public TaskResponseDto create(@Valid @RequestBody TaskCreateRequestDto dto) {
        return taskService.create(dto);
    }

    @PutMapping("/{taskId}/assign/{username}")
    public TaskResponseDto assignUser(@PathVariable Long taskId, @PathVariable String username) {
        return taskService.assignUser(taskId, username);
    }

    @GetMapping
    public PageResponseDto<TaskResponseDto> getAllTasks(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String assignee,
            @PageableDefault(size = 20) Pageable pageable) {
        return taskService.getAllTasks(status, assignee, pageable);
    }

    @PatchMapping("/{id}/status/next")
    public TaskResponseDto moveToNextStatus(@PathVariable Long id) {
        return taskService.moveToNextStatus(id);
    }
}
