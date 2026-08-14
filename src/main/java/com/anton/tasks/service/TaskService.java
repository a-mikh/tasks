package com.anton.tasks.service;

import com.anton.tasks.dto.task.TaskCreateRequestDto;
import com.anton.tasks.dto.task.TaskCreateResponseDto;
import com.anton.tasks.model.TaskEntity;
import com.anton.tasks.model.TaskStatus;
import com.anton.tasks.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskCreateResponseDto create(TaskCreateRequestDto dto) {
        TaskEntity newTask = new TaskEntity(
                dto.title(),
                dto.description(),
                TaskStatus.TODO
        );
        TaskEntity savedTask = taskRepository.save(newTask);

        return new TaskCreateResponseDto(savedTask);
    }
}
