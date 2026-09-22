package com.anton.tasks.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequestDto(
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
