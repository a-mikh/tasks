package com.anton.tasks.dto.user;

import com.anton.tasks.model.TaskStatus;

public record UserCreateResponseDto(
        Long id,
        String username
) {
}
