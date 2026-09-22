package com.anton.tasks.controller;

import com.anton.tasks.dto.auth.UserLoginRequestDto;
import com.anton.tasks.dto.auth.UserLoginResponseDto;
import com.anton.tasks.dto.auth.UserRegisterRequestDto;
import com.anton.tasks.dto.auth.UserRegisterResponseDto;
import com.anton.tasks.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserRegisterResponseDto registerUser(@Valid @RequestBody UserRegisterRequestDto userRegisterRequestDto) {
        return authService.registerUser(userRegisterRequestDto);
    }

    @PostMapping("/login")
    public UserLoginResponseDto login(@Valid @RequestBody UserLoginRequestDto userLoginRequestDto) {
        return authService.login(userLoginRequestDto);
    }
}
