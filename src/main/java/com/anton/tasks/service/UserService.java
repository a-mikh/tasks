package com.anton.tasks.service;

import com.anton.tasks.dto.user.UserCreateRequestDto;
import com.anton.tasks.dto.user.UserCreateResponseDto;
import com.anton.tasks.exceptions.user.UserAlreadyExistsException;
import com.anton.tasks.model.UserEntity;
import com.anton.tasks.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository usersRepository;

    public UserService(UserRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public UserCreateResponseDto createUser(UserCreateRequestDto dto) {
        boolean isAlreadyExists = usersRepository.existsByUsername(dto.username());

        if (isAlreadyExists) {
            throw new UserAlreadyExistsException("Username " + dto.username() + " already exists");
        }

        UserEntity savedUser = usersRepository.save(new UserEntity(dto.username()));

        return new UserCreateResponseDto(savedUser.getId(), savedUser.getUsername());
    }
}
