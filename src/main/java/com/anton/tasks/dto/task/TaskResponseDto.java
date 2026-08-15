package com.anton.tasks.dto.task;

import com.anton.tasks.model.TaskEntity;
import com.anton.tasks.model.TaskStatus;

public record TaskResponseDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        String assignee
) {
    public TaskResponseDto(TaskEntity taskEntity) {
        this(
                taskEntity.getId(),
                taskEntity.getTitle(),
                taskEntity.getDescription(),
                taskEntity.getStatus(),
                taskEntity.getAssignedUser() != null
                        ? taskEntity.getAssignedUser().getUsername()
                        : null
        );
    }
}
