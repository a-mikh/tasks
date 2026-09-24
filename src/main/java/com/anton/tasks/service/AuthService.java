package com.anton.tasks.service;

import com.anton.tasks.dto.auth.UserLoginRequestDto;
import com.anton.tasks.dto.auth.UserLoginResponseDto;
import com.anton.tasks.dto.auth.UserRegisterRequestDto;
import com.anton.tasks.dto.auth.UserRegisterResponseDto;
import com.anton.tasks.exceptions.auth.InvalidCredentialsException;
import com.anton.tasks.exceptions.user.UserAlreadyExistsException;
import com.anton.tasks.model.UserEntity;
import com.anton.tasks.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class AuthService {
    private static final String USERNAME_UNIQUE_CONSTRAINT = "uk_users_username";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final Duration accessTokenTtl;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.access-token-ttl}") Duration accessTokenTtl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.accessTokenTtl = accessTokenTtl;
    }

    public UserRegisterResponseDto registerUser(UserRegisterRequestDto dto) {
        boolean isAlreadyExists = userRepository.existsByUsername(dto.username());

        if (isAlreadyExists) {
            throw new UserAlreadyExistsException("Username " + dto.username() + " already exists");
        }

        String encodedPassword = passwordEncoder.encode(dto.password());
        UserEntity newUser = new UserEntity(dto.username(), encodedPassword);
        UserEntity savedUser;

        try {
            savedUser = userRepository.saveAndFlush(newUser);
        } catch (DataIntegrityViolationException exception) {
            if (exception.getCause() instanceof ConstraintViolationException constraintViolationException) {
                String constraintName = constraintViolationException.getConstraintName();
                if (USERNAME_UNIQUE_CONSTRAINT.equals(constraintName)) {
                    throw new UserAlreadyExistsException("Username " + dto.username() + " already exists");
                }
            }
            throw exception;
        }

        return new UserRegisterResponseDto(savedUser.getId(), savedUser.getUsername());
    }

    public UserLoginResponseDto login(UserLoginRequestDto dto) {
        return getJwtToken(authenticateUser(dto));
    }

    private Long authenticateUser(UserLoginRequestDto dto) {
        UserEntity user = userRepository.findByUsername(dto.username())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getPasswordHash() == null || !passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user.getId();
    }

    private UserLoginResponseDto getJwtToken(Long userId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenTtl);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .issuer("tasks-api")
                .audience(List.of("tasks-api"))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtEncoderParameters parameters = JwtEncoderParameters.from(header, claims);
        String tokenValue = jwtEncoder.encode(parameters).getTokenValue();

        return new UserLoginResponseDto(tokenValue, accessTokenTtl.toSeconds());
    }
}
