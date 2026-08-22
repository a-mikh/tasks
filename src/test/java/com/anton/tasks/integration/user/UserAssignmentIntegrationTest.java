package com.anton.tasks.integration.user;

import com.anton.tasks.error.ErrorCode;
import com.anton.tasks.exceptions.task.TaskNotFoundException;
import com.anton.tasks.exceptions.user.UserNotFoundException;
import com.anton.tasks.integration.IntegrationTest;
import com.anton.tasks.repository.TaskRepository;
import com.anton.tasks.repository.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class UserAssignmentIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldAssignExistingUserToExistingTask() throws Exception {
        String username = createUser("test-username");
        Long taskId = createTask("test-task");

        assertThat(userRepository.findByUsername(username)).isPresent();
        assertThat(taskRepository.findById(taskId)).isPresent();

        String formattedPath = String.format("/tasks/%d/assign/%s", taskId, username);

        mockMvc.perform(put(formattedPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.assignee").value(username));

        assertThat(taskRepository.findById(taskId).isPresent()).isTrue();
        assertThat(taskRepository.findById(taskId).get().getAssignedUser().getUsername()).isEqualTo(username);
    }

    @Test
    void shouldThrowUserNotFoundException() throws Exception {
        Long taskId = createTask("test-task");

        assertThat(taskRepository.findById(taskId)).isPresent();
        assertThat(userRepository.count()).isZero();

        String formattedPath = String.format("/tasks/%d/assign/%s", taskId, "username");

        MvcResult mvcResult = mockMvc.perform(put(formattedPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value(formattedPath))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andReturn();

        Throwable resolvedException = mvcResult.getResolvedException();
        assertNotNull(resolvedException);
        assertInstanceOf(UserNotFoundException.class, resolvedException);

        assertThat(taskRepository.findById(taskId).isPresent()).isTrue();
        assertThat(taskRepository.findById(taskId).get().getAssignedUser()).isNull();
    }

    @Test
    void shouldThrowTaskNotFoundException() throws Exception {
        String username = createUser("test-username");

        assertThat(userRepository.findByUsername(username)).isPresent();
        assertThat(taskRepository.count()).isZero();

        String formattedPath = String.format("/tasks/%d/assign/%s", -1, username);

        MvcResult mvcResult = mockMvc.perform(put(formattedPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value(ErrorCode.TASK_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value(formattedPath))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andReturn();

        Throwable resolvedException = mvcResult.getResolvedException();
        assertNotNull(resolvedException);
        assertInstanceOf(TaskNotFoundException.class, resolvedException);

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void shouldThrowTaskNotFoundExceptionWhenTaskAndUserAreNotValid() throws Exception {
        assertThat(userRepository.count()).isZero();
        assertThat(taskRepository.count()).isZero();

        String formattedPath = String.format("/tasks/%d/assign/%s", -1, "test");

        MvcResult mvcResult = mockMvc.perform(put(formattedPath))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value(ErrorCode.TASK_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value(formattedPath))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andReturn();

        Throwable resolvedException = mvcResult.getResolvedException();
        assertNotNull(resolvedException);
        assertInstanceOf(TaskNotFoundException.class, resolvedException);

        assertThat(taskRepository.count()).isZero();
    }

    @Test
    void shouldAssignUserWithUsernameWithAllAllowedSymbols() throws Exception {
        String username = createUser("t-e.s_t26");

        assertThat(userRepository.findByUsername(username)).hasValueSatisfying(user -> {
            assertThat(user.getUsername()).isEqualTo(username);
        });

        Long taskId = createTask("test-task");
        assertThat(taskRepository.findById(taskId)).hasValueSatisfying(task -> {
            assertThat(task.getTitle()).isEqualTo("test-task");
        });

        String formattedPath = String.format("/tasks/%d/assign/%s", taskId, username);
        mockMvc.perform(put(formattedPath))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.assignee").value(username));

        assertThat(taskRepository.findById(taskId)).hasValueSatisfying(task -> {
            assertThat(task.getTitle()).isEqualTo("test-task");
            assertThat(task.getAssignedUser().getUsername()).isEqualTo(username);
        });
    }

    private String createUser(String username) throws Exception {
        String createUserRequest = """
                {
                  "username": "%s"
                }
                """.formatted(username);

        MvcResult userMvcResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createUserRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String userResponseJson = userMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        return JsonPath.read(userResponseJson, "$.username");
    }

    private Long createTask(String title) throws Exception {
        String createTaskRequest = """
                {
                  "title": "%s"
                }
                """.formatted(title);

        MvcResult taskMvcResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTaskRequest))
                .andExpect(status().isCreated())
                .andReturn();

        String taskResponseJson = taskMvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        return JsonPath.parse(taskResponseJson).read("$.id", Long.class);
    }
}
