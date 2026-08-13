package com.anton.tasks.dto.task;

import jakarta.validation.constraints.NotBlank;

public record TaskCreateRequestDto(
        @NotBlank
        String title,
        String description
) {
}
