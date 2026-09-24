package com.anton.tasks.integration.auth;

import com.anton.tasks.dto.auth.UserRegisterRequestDto;
import com.anton.tasks.dto.auth.UserRegisterResponseDto;
import com.anton.tasks.error.ErrorCode;
import com.anton.tasks.integration.IntegrationTest;
import com.anton.tasks.repository.UserRepository;
import com.anton.tasks.service.AuthService;
import com.jayway.jsonpath.JsonPath;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AuthIntegrationTest extends IntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Validator validator;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void shouldSaveUserWithHashedPassword() {
        String username = "test-user";
        String password = "test-password";
        UserRegisterRequestDto request =
                new UserRegisterRequestDto(username, password);

        UserRegisterResponseDto response = authService.registerUser(request);

        assertThat(userRepository.findByUsername(username))
                .hasValueSatisfying(userEntity -> {
                    assertThat(userEntity.getId()).isNotNull();
                    assertThat(userEntity.getId()).isEqualTo(response.id());
                    assertThat(userEntity.getUsername()).isEqualTo(username);
                    assertThat(userEntity.getPasswordHash()).isNotEqualTo(password);
                    assertThat(passwordEncoder.matches(password, userEntity.getPasswordHash())).isTrue();
                });


        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo(username);
    }

    @Test
    void shouldPassMaxByteValidation() {
        String username = "test-user";
        String password = "\uD83E\uDD2A".repeat(18);

        UserRegisterRequestDto request =
                new UserRegisterRequestDto(username, password);

        assertThat(validator.validate(request)).isEmpty();

        password = "\uD83E\uDD2A".repeat(19);

        request = new UserRegisterRequestDto(username, password);

        Set<ConstraintViolation<UserRegisterRequestDto>> violations = validator.validate(request);

        assertThat(violations)
                .hasSize(1)
                .first()
                .extracting(ConstraintViolation::getPropertyPath)
                .hasToString("password");
    }

    @Test
    void shouldRegisterUser() throws Exception {
        String username = "test-user";
        String password = "test-password";
        String request = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldReturn409ForAlreadyExistedUser() throws Exception {
        String username = "test";
        String password = "test-password";
        String userRequest = """
                            {
                              "username": "%s",
                              "password": "%s"
                            }
                """.formatted(username, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        assertThat(userRepository.count()).isEqualTo(1);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_EXISTS.name()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/auth/register"))
                .andExpect(jsonPath("$.fieldErrors").exists());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturn400ForPasswordShorterThan8Characters() throws Exception {
        String username = "test-user";
        String password = "t";
        String request = """
                        {
                              "username": "%s",
                              "password": "%s"
                        }
                """.formatted(username, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnJwtToken() throws Exception {
        String username = "test-user";
        String password = "test-password";
        String userRequest = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        MvcResult registrationResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String registrationResponseJson = registrationResult.getResponse().getContentAsString();
        Integer userId = JsonPath.read(registrationResponseJson, "$.id");

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresInSeconds").value(900))
                .andReturn();

        String loginResponseJson = loginResult.getResponse().getContentAsString();
        String accessToken = JsonPath.read(loginResponseJson, "$.accessToken");
        Jwt decodedJwt = jwtDecoder.decode(accessToken);
        String subject = decodedJwt.getSubject();

        assertThat(subject).isEqualTo(String.valueOf(userId));
        assertThat(Duration.between(decodedJwt.getIssuedAt(), decodedJwt.getExpiresAt()))
                .isEqualTo(Duration.ofMinutes(15));

        mockMvc.perform(get("/tasks")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn401ForNonExistingUser() throws Exception {
        String username = "test-user";
        String password = "test-password";
        String userRequest = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_FAILED.name()))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }

    @Test
    void shouldReturn401ForUserWithBadCredentials() throws Exception {
        String username = "test-user";
        String password = "test-password";
        String userRequest = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        userRequest = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, "wrong-password");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value(ErrorCode.AUTHENTICATION_FAILED.name()))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/auth/login"));
    }
}
