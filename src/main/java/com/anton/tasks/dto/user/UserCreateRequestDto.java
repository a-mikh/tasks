package com.anton.tasks.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequestDto(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+$",
                message = "Username may contain only letters, digits, '.', '_' and '-'"
        )
        String username
) {
}
