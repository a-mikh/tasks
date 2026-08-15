package com.anton.tasks.integration.task;

import com.anton.tasks.dto.task.TaskResponseDto;
import com.anton.tasks.model.TaskStatus;
import com.anton.tasks.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TaskRetrievalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllTasks() throws Exception {
        String task1Title = "Task1";
        String task2Title = "Task2";

        String task1Request = """
                {
                  "title": "%s"
                }
                """.formatted(task1Title);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(task1Request))
                .andExpect(status().isCreated());

        assertThat(taskRepository.count()).isEqualTo(1);

        String task2Request = """
                {
                  "title": "%s"
                }
                """.formatted(task2Title);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(task2Request))
                .andExpect(status().isCreated());

        assertThat(taskRepository.count()).isEqualTo(2);

        MvcResult mvcResult = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<TaskResponseDto> tasks = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertThat(tasks).hasSize(2);
        assertThat(tasks)
                .extracting(TaskResponseDto::title)
                .containsExactlyInAnyOrder(
                        task1Title,
                        task2Title
                );
        assertThat(tasks)
                .allSatisfy(task -> {
                    assertThat(task.id()).isNotNull();
                    assertThat(task.status()).isEqualTo(TaskStatus.TODO);
                });
    }

    @Test
    void shouldReturnEmptyListWhenNoTasks() throws Exception {
        assertThat(taskRepository.count()).isEqualTo(0);

        MvcResult mvcResult = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<TaskResponseDto> tasks = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertThat(tasks.size()).isEqualTo(0);
    }
}
