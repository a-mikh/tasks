package com.anton.tasks.integration.error;

import com.anton.tasks.error.ErrorCode;
import com.anton.tasks.integration.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ErrorHandlingIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnResourceNotFoundStatusForUnknownResource() throws Exception {
        mockMvc.perform(get("/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value("Resource not found"))
                .andExpect(jsonPath("$.path").value("/unknown"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnMethodNotAllowedStatusForWrongMethod() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value(ErrorCode.METHOD_NOT_ALLOWED.name()))
                .andExpect(jsonPath("$.message").value("HTTP method is not supported for this endpoint"))
                .andExpect(jsonPath("$.path").value("/users"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnUnsupportedMediaTypeStatusForWrongMediaType() throws Exception {
        String username = "test";
        String request = """
                {
                  "username": "%s"
                }
                """.formatted(username);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(request))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.code").value(ErrorCode.UNSUPPORTED_MEDIA_TYPE.name()))
                .andExpect(jsonPath("$.message").value("Content type is not supported"))
                .andExpect(jsonPath("$.path").value("/users"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }
}
