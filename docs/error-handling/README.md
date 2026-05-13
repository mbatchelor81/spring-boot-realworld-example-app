# FTGO Error Handling Patterns

## Overview

All FTGO microservices use a centralized error handling library (`ftgo-error-handling-lib`) that provides:

- **Standardized error response format** across all services
- **`@ControllerAdvice`-based `GlobalExceptionHandler`** for consistent exception mapping
- **Bean Validation** on all command/DTO classes
- **Trace ID propagation** in every error response via Micrometer Tracing

## Error Response Format

Every error response follows this JSON structure:

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {
      "field": "name",
      "rejectedValue": "",
      "message": "must not be blank"
    }
  ],
  "timestamp": "2024-06-15T10:30:00Z",
  "traceId": "64a1b2c3d4e5f678"
}
```

| Field       | Type            | Description                                         | Nullable |
|-------------|-----------------|-----------------------------------------------------|----------|
| `code`      | `String`        | Machine-readable error code (see table below)       | No       |
| `message`   | `String`        | Human-readable summary                              | No       |
| `details`   | `FieldError[]`  | Field-level validation errors                       | Yes      |
| `timestamp` | `Instant`       | ISO-8601 timestamp of when the error occurred       | No       |
| `traceId`   | `String`        | Distributed trace ID from Micrometer Tracing        | Yes      |

## Error Codes

| Error Code                 | HTTP Status | When Used                                            |
|----------------------------|-------------|------------------------------------------------------|
| `VALIDATION_ERROR`         | 400         | Bean Validation failures, malformed JSON, type mismatches |
| `RESOURCE_NOT_FOUND`       | 404         | Entity not found by ID, no handler for URL           |
| `STATE_CONFLICT`           | 409         | `UnsupportedStateTransitionException` — invalid state transition |
| `BUSINESS_RULE_VIOLATION`  | 422         | `OrderMinimumNotMetException` — business rule not satisfied |
| `NOT_IMPLEMENTED`          | 501         | `NotYetImplementedException` — feature not yet available |
| `AUTHENTICATION_REQUIRED`  | 401         | Missing or invalid authentication                    |
| `ACCESS_DENIED`            | 403         | Insufficient permissions                             |
| `METHOD_NOT_ALLOWED`       | 405         | HTTP method not supported for endpoint               |
| `UNSUPPORTED_MEDIA_TYPE`   | 415         | Content-Type not supported                           |
| `INTERNAL_ERROR`           | 500         | Catch-all for unhandled exceptions                   |

## Exception → HTTP Status Mapping

| Exception Class                        | HTTP Status              | Error Code                |
|----------------------------------------|--------------------------|---------------------------|
| `MethodArgumentNotValidException`      | 400 Bad Request          | `VALIDATION_ERROR`        |
| `ConstraintViolationException`         | 400 Bad Request          | `VALIDATION_ERROR`        |
| `HttpMessageNotReadableException`      | 400 Bad Request          | `VALIDATION_ERROR`        |
| `MethodArgumentTypeMismatchException`  | 400 Bad Request          | `VALIDATION_ERROR`        |
| `ResourceNotFoundException`            | 404 Not Found            | `RESOURCE_NOT_FOUND`      |
| `NoHandlerFoundException`              | 404 Not Found            | `RESOURCE_NOT_FOUND`      |
| `UnsupportedStateTransitionException`  | 409 Conflict             | `STATE_CONFLICT`          |
| `OrderMinimumNotMetException`          | 422 Unprocessable Entity | `BUSINESS_RULE_VIOLATION` |
| `NotYetImplementedException`           | 501 Not Implemented      | `NOT_IMPLEMENTED`         |
| `HttpRequestMethodNotSupportedException` | 405 Method Not Allowed | `METHOD_NOT_ALLOWED`      |
| `HttpMediaTypeNotSupportedException`   | 415 Unsupported Media    | `UNSUPPORTED_MEDIA_TYPE`  |
| `Exception` (catch-all)               | 500 Internal Error       | `INTERNAL_ERROR`          |

## Integration Guide

### 1. Add Dependency

In your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':services:ftgo-error-handling-lib')
}
```

### 2. Auto-Configuration

The library uses Spring Boot auto-configuration (`spring.factories`). No additional setup is needed — the `GlobalExceptionHandler` is automatically registered when the library is on the classpath.

### 3. Bean Validation on DTOs

All command/DTO classes should use `javax.validation` annotations:

```java
public class CreateOrderCommand {

  @Positive private final long consumerId;
  @Positive private final long restaurantId;
  @NotNull @Valid private final Address deliveryAddress;
  @NotEmpty @Valid private final List<OrderLineItemDto> lineItems;
  // ...
}
```

Controllers should use `@Valid` on `@RequestBody` parameters:

```java
@PostMapping
public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderCommand command) {
    // validation is automatic — errors are handled by GlobalExceptionHandler
}
```

### 4. Throwing Custom Exceptions

Use `ResourceNotFoundException` for entity-not-found scenarios:

```java
Order order = repository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Order", id));
```

Existing domain exceptions (`UnsupportedStateTransitionException`, `OrderMinimumNotMetException`, `NotYetImplementedException`) are automatically mapped to proper HTTP status codes.

### 5. Trace ID

The `traceId` field is automatically populated from the current Micrometer Tracing span. This enables correlation between error responses and distributed traces in Zipkin/Jaeger.

## Module Structure

```
services/ftgo-error-handling-lib/
├── build.gradle
└── src/
    ├── main/
    │   ├── java/com/ftgo/error/
    │   │   ├── ErrorCode.java                        # Error code constants
    │   │   ├── ErrorResponse.java                    # Standardized response DTO
    │   │   ├── FtgoErrorHandlingAutoConfiguration.java # Spring Boot auto-config
    │   │   ├── GlobalExceptionHandler.java           # @ControllerAdvice handler
    │   │   ├── ResourceNotFoundException.java        # 404 exception
    │   │   └── package-info.java
    │   └── resources/META-INF/
    │       └── spring.factories                      # Auto-configuration entry
    └── test/
        └── java/com/ftgo/error/
            ├── ErrorResponseTest.java                # DTO serialization tests
            └── GlobalExceptionHandlerTest.java       # Integration tests
```
