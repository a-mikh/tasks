package com.anton.tasks.service;

import com.anton.tasks.dto.task.TaskCreateRequestDto;
import com.anton.tasks.dto.task.TaskResponseDto;
import com.anton.tasks.exceptions.task.TaskNotFoundException;
import com.anton.tasks.exceptions.user.UserNotFoundException;
import com.anton.tasks.model.TaskEntity;
import com.anton.tasks.model.TaskStatus;
import com.anton.tasks.model.UserEntity;
import com.anton.tasks.repository.TaskRepository;
import com.anton.tasks.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskResponseDto create(TaskCreateRequestDto dto) {
        TaskEntity newTask = new TaskEntity(
                dto.title(),
                dto.description(),
                TaskStatus.TODO,
                null
        );
        TaskEntity savedTask = taskRepository.save(newTask);

        return new TaskResponseDto(savedTask);
    }

    @Transactional
    public TaskResponseDto assignUser(Long taskId, String username) {
        TaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(TaskNotFoundException::new);

        UserEntity assignee = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        task.setAssignedUser(assignee);
        TaskEntity savedTask = taskRepository.save(task);

        return new TaskResponseDto(savedTask);
    }

    public Page<TaskResponseDto> getAllTasks(TaskStatus status, String assignee, Pageable pageable) {
        Pageable effectivePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id")
        );

        if (status != null && assignee != null) {
            return taskRepository
                    .findByStatusAndAssignedUser_Username(status, assignee, effectivePageable)
                    .map(TaskResponseDto::new);
        }

        if (status != null) {
            return taskRepository
                    .findByStatus(status, effectivePageable)
                    .map(TaskResponseDto::new);
        }

        if (assignee != null) {
            return taskRepository
                    .findByAssignedUser_Username(assignee, effectivePageable)
                    .map(TaskResponseDto::new);
        }

        return taskRepository
                .findAll(effectivePageable)
                .map(TaskResponseDto::new);
    }
}
