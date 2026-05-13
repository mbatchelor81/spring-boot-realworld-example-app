package com.ftgo.SERVICENAME.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ftgo.testlib.containers.FtgoContainers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Template: Integration test with Testcontainers MySQL and full Spring context.
 *
 * <p>Instructions:
 * <ol>
 *   <li>Replace SERVICENAME with your service name</li>
 *   <li>Add service-specific Flyway migrations to src/main/resources/db/migration/</li>
 *   <li>Add real endpoint paths and request bodies</li>
 *   <li>Verify response status codes, headers, and body content</li>
 * </ol>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@Tag("integration")
class YourControllerIntegrationTest {

  @Container static MySQLContainer<?> mysql = FtgoContainers.mysql();

  @DynamicPropertySource
  static void dbProperties(DynamicPropertyRegistry registry) {
    FtgoContainers.configureMysql(registry, mysql);
  }

  @Autowired private MockMvc mockMvc;

  @Test
  void getEntity_existingId_returns200() throws Exception {
    // mockMvc.perform(get("/your-entities/1")
    //         .contentType(MediaType.APPLICATION_JSON))
    //     .andExpect(status().isOk())
    //     .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void createEntity_validRequest_returns201() throws Exception {
    // String requestBody = """
    //     {
    //       "name": "Test Entity",
    //       "description": "A test entity"
    //     }
    //     """;
    //
    // mockMvc.perform(post("/your-entities")
    //         .contentType(MediaType.APPLICATION_JSON)
    //         .content(requestBody))
    //     .andExpect(status().isCreated())
    //     .andExpect(jsonPath("$.name").value("Test Entity"));
  }

  @Test
  void getEntity_nonExistentId_returns404() throws Exception {
    // mockMvc.perform(get("/your-entities/999")
    //         .contentType(MediaType.APPLICATION_JSON))
    //     .andExpect(status().isNotFound());
  }
}
