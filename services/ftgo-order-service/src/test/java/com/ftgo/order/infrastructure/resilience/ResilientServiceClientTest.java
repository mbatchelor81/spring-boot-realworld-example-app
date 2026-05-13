package com.ftgo.order.infrastructure.resilience;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ftgo.order.infrastructure.discovery.ServiceDiscoveryProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:ftgo_resilience_test_db;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=false"
})
class ResilientServiceClientTest {

  @Autowired private ResilientServiceClient resilientServiceClient;

  @Autowired private CircuitBreakerRegistry circuitBreakerRegistry;

  @MockBean private RestTemplate serviceRestTemplate;

  @Autowired private ServiceDiscoveryProperties discoveryProperties;

  @BeforeEach
  void setUp() {
    circuitBreakerRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
  }

  @Test
  void validateConsumer_successfulCall_returnsTrue() {
    when(serviceRestTemplate.getForEntity(anyString(), eq(String.class)))
        .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

    boolean result = resilientServiceClient.validateOrderForConsumer(1L, 100L);

    assertThat(result).isTrue();
    verify(serviceRestTemplate, atLeast(1)).getForEntity(anyString(), eq(String.class));
  }

  @Test
  void validateConsumer_serviceUnavailable_fallbackReturnsTrue() {
    when(serviceRestTemplate.getForEntity(anyString(), eq(String.class)))
        .thenThrow(new ResourceAccessException("Connection refused"));

    boolean result = resilientServiceClient.validateOrderForConsumer(1L, 100L);

    assertThat(result).isTrue();
  }

  @Test
  void validateRestaurant_successfulCall_returnsTrue() {
    when(serviceRestTemplate.getForEntity(anyString(), eq(String.class)))
        .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

    boolean result = resilientServiceClient.validateRestaurant(1L);

    assertThat(result).isTrue();
  }

  @Test
  void validateRestaurant_serviceUnavailable_fallbackReturnsTrue() {
    when(serviceRestTemplate.getForEntity(anyString(), eq(String.class)))
        .thenThrow(new ResourceAccessException("Connection refused"));

    boolean result = resilientServiceClient.validateRestaurant(1L);

    assertThat(result).isTrue();
  }

  @Test
  void circuitBreaker_opensAfterRepeatedFailures() {
    when(serviceRestTemplate.getForEntity(anyString(), eq(String.class)))
        .thenThrow(new ResourceAccessException("Connection refused"));

    CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("consumerService");

    for (int i = 0; i < 10; i++) {
      resilientServiceClient.validateOrderForConsumer(1L, (long) i);
    }

    assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
  }

  @Test
  void circuitBreaker_fallbackReturnsTrueWhenOpen() {
    when(serviceRestTemplate.getForEntity(anyString(), eq(String.class)))
        .thenThrow(new ResourceAccessException("Connection refused"));

    for (int i = 0; i < 15; i++) {
      boolean result = resilientServiceClient.validateRestaurant(1L);
      assertThat(result).isTrue();
    }

    CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("restaurantService");
    assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
  }
}
