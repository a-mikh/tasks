package com.anton.tasks.dto.auth;

public record UserLoginResponseDto(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {

    public UserLoginResponseDto(String accessToken, long expiresInSeconds) {
        this(accessToken, "Bearer", expiresInSeconds);
    }
}
