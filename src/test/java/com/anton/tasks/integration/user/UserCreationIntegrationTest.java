package com.anton.tasks.integration.user;

import com.anton.tasks.error.ErrorCode;
import com.anton.tasks.integration.IntegrationTest;
import com.anton.tasks.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


public class UserCreationIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldCreateUser() throws Exception {
        String username = "test";
        String userRequest = """
                {
                  "username": "%s"
                }
                """.formatted(username);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(username));

        assertThat(userRepository.count()).isEqualTo(1);

        assertThat(userRepository.findByUsername(username))
                .hasValueSatisfying(userEntity -> {
                    assertThat(userEntity.getId()).isNotNull();
                    assertThat(userEntity.getUsername()).isEqualTo(username);
                });
    }

    @Test
    void shouldReturn400ForUserWithEmptyBody() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/users"))
                .andExpect(jsonPath("$.fieldErrors").exists());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldReturn400ForUserWithoutUsername() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/users"))
                .andExpect(jsonPath("$.fieldErrors").exists());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldReturn400ForUserWithBlankUsername() throws Exception {
        String userRequest = """
                        {
                          "username": "     "
                        }
                """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/users"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.username").exists());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldReturn409ForUserWithDuplicateUsername() throws Exception {
        String username = "test";
        String userRequest = """
                            {
                              "username": "%s"
                            }
                """.formatted(username);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated());

        assertThat(userRepository.count()).isEqualTo(1);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_ALREADY_EXISTS.name()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/users"))
                .andExpect(jsonPath("$.fieldErrors").exists());

        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldReturn400ForUsernameLongerThan50Symbols() throws Exception {
        String username = "a".repeat(51);
        String request = """
                {
                  "username": "%s"
                }
                """.formatted(username);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.fieldErrors.username").exists());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldCreateUserWithUsernameWithAllowedSymbols() throws Exception {
        String username = "t-e.s_t26";
        String request = """
                {
                  "username": "%s"
                }
                """.formatted(username);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        String responseJson = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long userId = JsonPath.parse(responseJson).read("$.id", Long.class);

        assertThat(userRepository.count()).isEqualTo(1);
        assertThat(userRepository.findById(userId)).hasValueSatisfying(u -> {
            assertThat(u.getUsername()).isEqualTo(username);
        });
    }

    @Test
    void shouldReturn400ForUsernameWithSlashes() throws Exception {
        String username = "a/b";
        checkUsername(username);
    }

    @Test
    void shouldReturn400ForShortUsername() throws Exception {
        String username = "ab";
        checkUsername(username);
    }

    @Test
    void shouldReturn400ForUsernameWithSpaces() throws Exception {
        String username = "test user";
        checkUsername(username);

        username = " test user ";
        checkUsername(username);
    }

    @Test
    void shouldReturn400ForUsernameWithAtSymbol() throws Exception {
        String username = "test-user@";
        checkUsername(username);
    }

    private void checkUsername(String username) throws Exception {
        String request = """
                {
                  "username": "%s"
                }
                """.formatted(username);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()))
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andReturn();

        assertThat(userRepository.count()).isEqualTo(0);
    }
}
