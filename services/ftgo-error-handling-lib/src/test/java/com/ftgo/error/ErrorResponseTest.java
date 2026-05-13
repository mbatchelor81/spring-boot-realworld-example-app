package com.ftgo.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorResponseTest {

  private final ObjectMapper mapper =
      new ObjectMapper().registerModule(new JavaTimeModule());

  @Test
  void serialization_includesAllFields() throws Exception {
    Instant now = Instant.parse("2024-06-15T10:30:00Z");
    List<ErrorResponse.FieldError> details =
        List.of(new ErrorResponse.FieldError("email", "bad", "must be valid"));

    ErrorResponse response =
        new ErrorResponse(ErrorCode.VALIDATION_ERROR, "Validation failed", details, now, "abc123");

    String json = mapper.writeValueAsString(response);
    assertThat(json).contains("\"code\":\"VALIDATION_ERROR\"");
    assertThat(json).contains("\"message\":\"Validation failed\"");
    assertThat(json).contains("\"traceId\":\"abc123\"");
    assertThat(json).contains("\"details\"");
  }

  @Test
  void serialization_nullDetailsOmitted() throws Exception {
    ErrorResponse response =
        new ErrorResponse(
            ErrorCode.INTERNAL_ERROR, "error", null, Instant.now(), "trace-1");

    String json = mapper.writeValueAsString(response);
    assertThat(json).doesNotContain("\"details\"");
  }

  @Test
  void fieldError_getters() {
    ErrorResponse.FieldError fe = new ErrorResponse.FieldError("name", "", "must not be blank");
    assertThat(fe.getField()).isEqualTo("name");
    assertThat(fe.getRejectedValue()).isEqualTo("");
    assertThat(fe.getMessage()).isEqualTo("must not be blank");
  }
}
