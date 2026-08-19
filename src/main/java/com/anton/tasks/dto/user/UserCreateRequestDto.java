package com.anton.tasks.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDto(
        @NotBlank
        @Size(max = 50)
        String username
) {
}
