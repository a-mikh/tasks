package com.anton.tasks.service;

import com.anton.tasks.dto.user.UserCreateRequestDto;
import com.anton.tasks.exceptions.user.UserAlreadyExistsException;
import com.anton.tasks.model.UserEntity;
import com.anton.tasks.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void shouldTranslateUsernameConstraintViolationToUserAlreadyExistsException() {
        UserCreateRequestDto userCreateRequestDto = new UserCreateRequestDto("duplicate");

        when(userRepository.existsByUsername("duplicate")).thenReturn(false);

        ConstraintViolationException constraintViolation =
                new ConstraintViolationException(
                        "duplicate username",
                        new SQLException(),
                        "uk_users_username"
                );

        DataIntegrityViolationException dataIntegrityViolation =
                new DataIntegrityViolationException(
                        "constraint violation",
                        constraintViolation
                );

        when(userRepository.saveAndFlush(any(UserEntity.class))).thenThrow(dataIntegrityViolation);

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.createUser(userCreateRequestDto);
        });
    }

    @Test
    void shouldRethrowDataIntegrityViolationForDifferentConstraint() {
        UserCreateRequestDto userCreateRequestDto = new UserCreateRequestDto("duplicate");

        when(userRepository.existsByUsername("duplicate")).thenReturn(false);

        ConstraintViolationException constraintViolation =
                new ConstraintViolationException(
                        "different constraint",
                        new SQLException(),
                        "some_other_constraint"
                );

        DataIntegrityViolationException dataIntegrityViolation =
                new DataIntegrityViolationException(
                        "constraint violation",
                        constraintViolation
                );

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(dataIntegrityViolation);

        DataIntegrityViolationException thrown = assertThrows(
                DataIntegrityViolationException.class,
                () -> userService.createUser(userCreateRequestDto)
        );

        assertSame(dataIntegrityViolation, thrown);
    }
}
