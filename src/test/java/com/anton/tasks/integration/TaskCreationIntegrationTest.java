package com.anton.tasks.integration;

import com.anton.tasks.model.TaskStatus;
import com.anton.tasks.repository.TaskRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldCreateTask() throws Exception {
        String title = "test title";
        String userRequest = """
                {
                  "title": "%s"
                }
                """.formatted(title);

        MvcResult mvcResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.status").value(TaskStatus.TODO.name()))
                .andReturn();

        assertThat(taskRepository.count()).isEqualTo(1);

        String responseJson = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        Long id = JsonPath.parse(responseJson).read("$.id", Long.class);

        assertThat(taskRepository.findById(id))
                .hasValueSatisfying(task -> {
                    assertThat(task.getId()).isNotNull();
                    assertThat(task.getTitle()).isEqualTo(title);
                    assertThat(task.getDescription()).isNull();
                    assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
                });
    }

    @Test
    void shouldCreateTaskWithDescription() throws Exception {
        String title = "test title";
        String description = "test description";
        String userRequest = """
                {
                  "title": "%s",
                  "description": "%s"
                }
                """.formatted(title, description);

        MvcResult mvcResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.description").value(description))
                .andExpect(jsonPath("$.status").value(TaskStatus.TODO.name()))
                .andReturn();

        assertThat(taskRepository.count()).isEqualTo(1);

        String responseJson = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        Long id = JsonPath.parse(responseJson).read("$.id", Long.class);

        assertThat(taskRepository.findById(id))
                .hasValueSatisfying(task -> {
                    assertThat(task.getId()).isNotNull();
                    assertThat(task.getTitle()).isEqualTo(title);
                    assertThat(task.getDescription()).isEqualTo(description);
                    assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
                });
    }

    @Test
    void shouldReturn400ForEmptyJSON() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        assertThat(taskRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldReturn400ForBlankTitle() throws Exception {
        String userRequest = """
                {
                  "title": "    "
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isBadRequest());

        assertThat(taskRepository.count()).isEqualTo(0);
    }

    @Test
    void shouldIgnoreUnknownFields() throws Exception {
        String title = "test title";
        String userRequest = """
                {
                  "title": "%s",
                  "status": "DONE"
                }
                """.formatted(title);

        MvcResult mvcResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(title))
                .andExpect(jsonPath("$.description").isEmpty())
                .andExpect(jsonPath("$.status").value(TaskStatus.TODO.name()))
                .andReturn();

        assertThat(taskRepository.count()).isEqualTo(1);

        String responseJson = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        Long id = JsonPath.parse(responseJson).read("$.id", Long.class);

        assertThat(taskRepository.findById(id))
                .hasValueSatisfying(task -> {
                    assertThat(task.getId()).isNotNull();
                    assertThat(task.getTitle()).isEqualTo(title);
                    assertThat(task.getDescription()).isNull();
                    assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);
                });
    }
}
