package com.ftgo.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

/** Standardized error response for all FTGO REST APIs. */
@Schema(description = "Standardized error response")
public class ApiErrorResponse {

  @Schema(description = "HTTP status code", example = "400")
  private final int status;

  @Schema(description = "Error category", example = "VALIDATION_ERROR")
  private final String error;

  @Schema(description = "Human-readable message", example = "Validation failed")
  private final String message;

  @Schema(description = "Request path", example = "/api/v1/orders")
  private final String path;

  @Schema(description = "ISO-8601 timestamp", example = "2024-06-15T10:30:00Z")
  private final Instant timestamp;

  @Schema(description = "Field-level validation errors (if applicable)")
  private final List<FieldError> fieldErrors;

  public ApiErrorResponse(
      int status,
      String error,
      String message,
      String path,
      Instant timestamp,
      List<FieldError> fieldErrors) {
    this.status = status;
    this.error = error;
    this.message = message;
    this.path = path;
    this.timestamp = timestamp;
    this.fieldErrors = fieldErrors;
  }

  public int getStatus() {
    return status;
  }

  public String getError() {
    return error;
  }

  public String getMessage() {
    return message;
  }

  public String getPath() {
    return path;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public List<FieldError> getFieldErrors() {
    return fieldErrors;
  }

  /** Individual field validation error. */
  @Schema(description = "Field validation error detail")
  public static class FieldError {

    @Schema(description = "Field name", example = "email")
    private final String field;

    @Schema(description = "Rejected value", example = "not-an-email")
    private final Object rejectedValue;

    @Schema(description = "Error message", example = "must be a valid email address")
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
