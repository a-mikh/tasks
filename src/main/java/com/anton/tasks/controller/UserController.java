package com.anton.tasks.controller;

import com.anton.tasks.dto.user.UserCreateRequestDto;
import com.anton.tasks.dto.user.UserCreateResponseDto;
import com.anton.tasks.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService usersService;

    public UserController(UserService usersService) {
        this.usersService = usersService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserCreateResponseDto createUser(@Valid @RequestBody UserCreateRequestDto dto) {
        return usersService.createUser(dto);
    }
}
