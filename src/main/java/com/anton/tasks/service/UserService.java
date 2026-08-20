package com.anton.tasks.service;

import com.anton.tasks.dto.user.UserCreateRequestDto;
import com.anton.tasks.dto.user.UserCreateResponseDto;
import com.anton.tasks.exceptions.user.UserAlreadyExistsException;
import com.anton.tasks.model.UserEntity;
import com.anton.tasks.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private static final String USERNAME_UNIQUE_CONSTRAINT = "uk_users_username";

    private final UserRepository usersRepository;

    public UserService(UserRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public UserCreateResponseDto createUser(UserCreateRequestDto dto) {
        boolean isAlreadyExists = usersRepository.existsByUsername(dto.username());

        if (isAlreadyExists) {
            throw new UserAlreadyExistsException("Username " + dto.username() + " already exists");
        }

        UserEntity savedUser;
        try {
            savedUser = usersRepository.saveAndFlush(new UserEntity(dto.username()));
        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException constraintViolationException) {
                String constraintName = constraintViolationException.getConstraintName();
                if (USERNAME_UNIQUE_CONSTRAINT.equals(constraintName)) {
                    throw new UserAlreadyExistsException("Username " + dto.username() + " already exists");
                }
            }
            throw exception;
        }

        return new UserCreateResponseDto(savedUser.getId(), savedUser.getUsername());
    }
}
