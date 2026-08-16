package com.anton.tasks.integration.task;

import com.anton.tasks.dto.task.TaskResponseDto;
import com.anton.tasks.model.TaskStatus;
import com.anton.tasks.repository.TaskRepository;
import com.anton.tasks.repository.UserRepository;
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
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllTasks() throws Exception {
        String task1Title = "Task1";
        String task2Title = "Task2";

        createTask(task1Title);
        assertThat(taskRepository.count()).isEqualTo(1);

        createTask(task2Title);
        assertThat(taskRepository.count()).isEqualTo(2);

        MvcResult mvcResult = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode root = objectMapper.readTree(jsonResponse);

        List<TaskResponseDto> tasks = objectMapper.convertValue(
                root.get("content"),
                new TypeReference<>() {
                }
        );

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

        JsonNode root = objectMapper.readTree(jsonResponse);

        List<TaskResponseDto> tasks = objectMapper.convertValue(
                root.get("content"),
                new TypeReference<>() {
                }
        );

        assertThat(tasks.size()).isEqualTo(0);
    }

    @Test
    void shouldReturnTasksFilteredByAssignee() throws Exception {
        MvcResult mvcResult = createTask("test-title");

        assertThat(taskRepository.count()).isEqualTo(1);

        String responseJson = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long taskId = JsonPath.parse(responseJson).read("$.id", Long.class);

        String username = createUser("test-username");

        assertThat(userRepository.count()).isEqualTo(1);

        String path = String.format("/tasks/%d/assign/%s", taskId, username);

        mockMvc.perform(put(path))
                .andExpect(status().isOk());

        assertThat(taskRepository.findById(taskId).isPresent()).isTrue();
        assertThat(taskRepository.findById(taskId).get().getAssignedUser().getUsername()).isEqualTo(username);

        mockMvc.perform(get(String.format("/tasks?assignee=%s", username)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(taskId));
    }

    @Test
    void shouldThrowExceptionWhenStatusIsInvalid() throws Exception {
        mockMvc.perform(get("/tasks?status=invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnTasksFilteredByStatusAndAssignee() throws Exception {
        MvcResult mvcResult1 = createTask("test-title1");
        assertThat(taskRepository.count()).isEqualTo(1);
        String responseJson1 = mvcResult1.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long taskId1 = JsonPath.parse(responseJson1).read("$.id", Long.class);

        MvcResult mvcResult2 = createTask("test-title2");
        assertThat(taskRepository.count()).isEqualTo(2);
        String responseJson2 = mvcResult2.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long taskId2 = JsonPath.parse(responseJson2).read("$.id", Long.class);

        String username1 = createUser("test-username1");
        assertThat(userRepository.count()).isEqualTo(1);

        String username2 = createUser("test-username2");
        assertThat(userRepository.count()).isEqualTo(2);

        String path = String.format("/tasks/%d/assign/%s", taskId1, username1);
        mockMvc.perform(put(path))
                .andExpect(status().isOk());

        path = String.format("/tasks/%d/assign/%s", taskId2, username2);
        mockMvc.perform(put(path))
                .andExpect(status().isOk());

        mockMvc.perform(get(String.format("/tasks?status=%s&assignee=%s", TaskStatus.TODO, username2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(taskId2));
    }

    @Test
    void shouldReturnTwoPagesOfTasks() throws Exception {
        createTask("test-title1");
        createTask("test-title2");
        createTask("test-title3");

        mockMvc.perform(get("/tasks?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.size").value(2));

        mockMvc.perform(get("/tasks?page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void shouldSortTasksInAscendingOrder() throws Exception {
        MvcResult mvcResult1 = createTask("test-title1");
        String responseJson = mvcResult1.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long taskId1 = JsonPath.parse(responseJson).read("$.id", Long.class);

        MvcResult mvcResult2 = createTask("test-title2");
        String responseJson2 = mvcResult2.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long taskId2 = JsonPath.parse(responseJson2).read("$.id", Long.class);

        MvcResult mvcResult3 = createTask("test-title3");
        String responseJson3 = mvcResult3.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Long taskId3 = JsonPath.parse(responseJson3).read("$.id", Long.class);

        mockMvc.perform(get("/tasks?page=0&size=3&sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(taskId1))
                .andExpect(jsonPath("$.content[1].id").value(taskId2))
                .andExpect(jsonPath("$.content[2].id").value(taskId3));
    }

    private MvcResult createTask(String title) throws Exception {
        String task1Request = """
                {
                  "title": "%s"
                }
                """.formatted(title);

        return mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(task1Request))
                .andExpect(status().isCreated())
                .andReturn();
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
}
