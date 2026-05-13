package com.ftgo.error;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ftgo.common.NotYetImplementedException;
import com.ftgo.common.UnsupportedStateTransitionException;
import com.ftgo.domain.OrderMinimumNotMetException;
import io.micrometer.tracing.Tracer;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest
@ContextConfiguration(classes = {
    GlobalExceptionHandlerTest.TestController.class,
    GlobalExceptionHandlerTest.TestConfig.class,
    GlobalExceptionHandler.class
})
class GlobalExceptionHandlerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void validationError_returns400() throws Exception {
    mockMvc
        .perform(
            post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"quantity\":-1}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR))
        .andExpect(jsonPath("$.message").value("Validation failed"))
        .andExpect(jsonPath("$.details").isArray())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void malformedJson_returns400() throws Exception {
    mockMvc
        .perform(
            post("/test/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR))
        .andExpect(jsonPath("$.message").value("Malformed request body"));
  }

  @Test
  void resourceNotFound_returns404() throws Exception {
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND))
        .andExpect(jsonPath("$.message").value("Order not found with id: 42"));
  }

  @Test
  void unsupportedStateTransition_returns409() throws Exception {
    mockMvc
        .perform(get("/test/state-conflict"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value(ErrorCode.STATE_CONFLICT));
  }

  @Test
  void orderMinimumNotMet_returns422() throws Exception {
    mockMvc
        .perform(get("/test/order-minimum"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value(ErrorCode.BUSINESS_RULE_VIOLATION));
  }

  @Test
  void notYetImplemented_returns501() throws Exception {
    mockMvc
        .perform(get("/test/not-implemented"))
        .andExpect(status().isNotImplemented())
        .andExpect(jsonPath("$.code").value(ErrorCode.NOT_IMPLEMENTED));
  }

  @Test
  void unexpectedException_returns500() throws Exception {
    mockMvc
        .perform(get("/test/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_ERROR))
        .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
  }

  @Test
  void errorResponse_includesTimestamp() throws Exception {
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void errorResponse_nullDetailsOmitted() throws Exception {
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(jsonPath("$.details").doesNotExist());
  }

  enum OrderState {
    APPROVED
  }

  static class TestRequest {

    @NotBlank private String name;

    @Positive private int quantity;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public int getQuantity() {
      return quantity;
    }

    public void setQuantity(int quantity) {
      this.quantity = quantity;
    }
  }

  @RestController
  static class TestController {

    @PostMapping("/test/validate")
    public String validate(@Valid @RequestBody TestRequest request) {
      return "ok";
    }

    @GetMapping("/test/not-found")
    public String notFound() {
      throw new ResourceNotFoundException("Order", 42L);
    }

    @GetMapping("/test/state-conflict")
    public String stateConflict() {
      throw new UnsupportedStateTransitionException(OrderState.APPROVED);
    }

    @GetMapping("/test/order-minimum")
    public String orderMinimum() {
      throw new OrderMinimumNotMetException();
    }

    @GetMapping("/test/not-implemented")
    public String notImplemented() {
      throw new NotYetImplementedException();
    }

    @GetMapping("/test/unexpected")
    public String unexpected() {
      throw new RuntimeException("boom");
    }
  }

  @Configuration
  static class TestConfig {

    @Bean
    public Tracer tracer() {
      return mock(Tracer.class);
    }
  }
}
