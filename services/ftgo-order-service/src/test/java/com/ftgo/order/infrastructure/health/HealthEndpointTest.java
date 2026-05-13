package com.ftgo.order.infrastructure.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HealthEndpointTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate testRestTemplate;

  @MockBean private RestTemplate serviceRestTemplate;

  @BeforeEach
  void setUp() {
    when(serviceRestTemplate.getForEntity(anyString(), eq(String.class)))
        .thenReturn(new ResponseEntity<>("{\"status\":\"UP\"}", HttpStatus.OK));
  }

  @Test
  void healthEndpoint_returnsOk() {
    ResponseEntity<String> response =
        testRestTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void livenessProbe_returnsOk() {
    ResponseEntity<String> response =
        testRestTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health/liveness", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void readinessProbe_returnsOk() {
    ResponseEntity<String> response =
        testRestTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health/readiness", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void healthEndpoint_containsServiceDetails() {
    ResponseEntity<String> response =
        testRestTemplate.getForEntity(
            "http://localhost:" + port + "/actuator/health", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("UP");
  }
}
