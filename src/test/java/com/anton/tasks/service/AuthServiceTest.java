package com.anton.tasks.service;

import com.anton.tasks.dto.auth.UserRegisterRequestDto;
import com.anton.tasks.exceptions.user.UserAlreadyExistsException;
import com.anton.tasks.model.UserEntity;
import com.anton.tasks.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.sql.SQLException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthServiceTest {
    private final String PASSWORD_HASH = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    private UserRepository userRepository;
    private AuthService authService;
    private PasswordEncoder passwordEncoder;
    private JwtEncoder jwtEncoder;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtEncoder = mock(JwtEncoder.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtEncoder, Duration.ofMinutes(15));
    }

    @Test
    void shouldTranslateUsernameConstraintViolationToUserAlreadyExistsException() {
        UserRegisterRequestDto userRegisterRequestDto =
                new UserRegisterRequestDto("duplicate", "test-password");

        when(passwordEncoder.encode(any())).thenReturn(PASSWORD_HASH);
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
            authService.registerUser(userRegisterRequestDto);
        });
    }

    @Test
    void shouldRethrowDataIntegrityViolationForDifferentConstraint() {
        UserRegisterRequestDto userRegisterRequestDto =
                new UserRegisterRequestDto("duplicate", "test-password");

        when(passwordEncoder.encode(any())).thenReturn(PASSWORD_HASH);
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
                () -> authService.registerUser(userRegisterRequestDto)
        );

        assertSame(dataIntegrityViolation, thrown);
    }
}
