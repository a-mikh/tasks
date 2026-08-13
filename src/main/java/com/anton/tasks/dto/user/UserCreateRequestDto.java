package com.anton.tasks.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequestDto(
        @NotBlank
        String username
) {
}
