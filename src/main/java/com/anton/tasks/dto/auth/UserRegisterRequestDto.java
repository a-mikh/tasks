package com.anton.tasks.dto.auth;

import com.anton.tasks.annotation.MaxBytes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequestDto(
        @NotBlank
        @Size(min = 3, max = 50)
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+$",
                message = "Username may contain only letters, digits, '.', '_' and '-'"
        )
        String username,
        @NotBlank
        @Size(min = 8)
        @MaxBytes(value = 72)
        String password
) {
}
