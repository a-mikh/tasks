package com.anton.tasks.integration.task;

import com.anton.tasks.exceptions.task.InvalidTaskStatusException;
import com.anton.tasks.exceptions.task.TaskNotFoundException;
import com.anton.tasks.integration.IntegrationTest;
import com.anton.tasks.model.TaskStatus;
import com.anton.tasks.repository.TaskRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TaskStatusIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void shouldReturnTaskWithInProgressStatus() throws Exception {
        Long taskId = createTask("test-title1");

        assertThat(taskRepository.findById(taskId)).isPresent();

        mockMvc.perform(patch("/tasks/" + taskId + "/status/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("test-title1"))
                .andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.name()));

        assertThat(taskRepository.findById(taskId))
                .hasValueSatisfying(task -> {
                    assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
                });
    }

    @Test
    void shouldReturnTaskWithDoneStatus() throws Exception {
        Long taskId = createTask("test-title1");

        assertThat(taskRepository.findById(taskId)).isPresent();

        mockMvc.perform(patch("/tasks/" + taskId + "/status/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("test-title1"))
                .andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.name()));

        mockMvc.perform(patch("/tasks/" + taskId + "/status/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("test-title1"))
                .andExpect(jsonPath("$.status").value(TaskStatus.DONE.name()));

        assertThat(taskRepository.findById(taskId))
                .hasValueSatisfying(task -> {
                    assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
                });
    }

    @Test
    void shouldThrowExceptionAndReturn409WhenSettingStatusAfterDone() throws Exception {
        Long taskId = createTask("test-title1");

        assertThat(taskRepository.findById(taskId)).isPresent();

        mockMvc.perform(patch("/tasks/" + taskId + "/status/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("test-title1"))
                .andExpect(jsonPath("$.status").value(TaskStatus.IN_PROGRESS.name()));

        mockMvc.perform(patch("/tasks/" + taskId + "/status/next"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("test-title1"))
                .andExpect(jsonPath("$.status").value(TaskStatus.DONE.name()));

        MvcResult mvcResult = mockMvc.perform(patch("/tasks/" + taskId + "/status/next"))
                .andExpect(status().isConflict())
                .andReturn();

        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(InvalidTaskStatusException.class);
    }

    @Test
    void shouldThrowExceptionAndReturn404WhenTaskNotFound() throws Exception {
        MvcResult mvcResult = mockMvc.perform(patch("/tasks/-999/status/next"))
                .andExpect(status().isNotFound())
                .andReturn();

        assertThat(mvcResult.getResolvedException())
                .isInstanceOf(TaskNotFoundException.class);
    }

    private Long createTask(String title) throws Exception {
        String taskRequest = """
                {
                  "title": "%s"
                }
                """.formatted(title);

        MvcResult mvcResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String responseJson = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonPath.parse(responseJson).read("$.id", Long.class);
    }
}
