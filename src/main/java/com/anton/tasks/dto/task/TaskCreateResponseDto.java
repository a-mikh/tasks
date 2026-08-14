package com.anton.tasks.dto.task;

import com.anton.tasks.model.TaskEntity;
import com.anton.tasks.model.TaskStatus;

public record TaskCreateResponseDto(
        Long id,
        String title,
        String description,
        TaskStatus status
) {
    public TaskCreateResponseDto(TaskEntity taskEntity) {
        this(taskEntity.getId(), taskEntity.getTitle(), taskEntity.getDescription(), taskEntity.getStatus());
    }
}
