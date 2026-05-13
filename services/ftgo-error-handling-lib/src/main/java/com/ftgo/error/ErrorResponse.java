package com.ftgo.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private final String code;
  private final String message;
  private final List<FieldError> details;
  private final Instant timestamp;
  private final String traceId;

  public ErrorResponse(
      String code, String message, List<FieldError> details, Instant timestamp, String traceId) {
    this.code = code;
    this.message = message;
    this.details = details;
    this.timestamp = timestamp;
    this.traceId = traceId;
  }

  public String getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public List<FieldError> getDetails() {
    return details;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public String getTraceId() {
    return traceId;
  }

  public static class FieldError {

    private final String field;
    private final Object rejectedValue;
    private final String message;

    public FieldError(String field, Object rejectedValue, String message) {
      this.field = field;
      this.rejectedValue = rejectedValue;
      this.message = message;
    }

    public String getField() {
      return field;
    }

    public Object getRejectedValue() {
      return rejectedValue;
    }

    public String getMessage() {
      return message;
    }
  }
}
