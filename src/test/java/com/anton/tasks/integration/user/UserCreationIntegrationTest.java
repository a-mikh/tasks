package com.anton.tasks.integration.user;

import com.anton.tasks.integration.IntegrationTest;
import com.anton.tasks.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldReturn400ForUserWithoutUsername() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

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
                .andExpect(status().isBadRequest());

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
                .andExpect(status().isConflict());

        assertThat(userRepository.count()).isEqualTo(1);
    }
}
