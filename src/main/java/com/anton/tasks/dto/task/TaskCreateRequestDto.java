package com.anton.tasks.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateRequestDto(
        @NotBlank
        @Size(max = 255)
        String title,
        @Size(max = 1000)
        String description
) {
}
