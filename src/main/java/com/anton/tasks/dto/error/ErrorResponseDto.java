package com.anton.tasks.dto.error;

import com.anton.tasks.error.ErrorCode;

import java.util.Map;

public record ErrorResponseDto(
        int status,
        ErrorCode code,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
