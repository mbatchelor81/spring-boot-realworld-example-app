package com.ftgo.error;

import com.ftgo.common.NotYetImplementedException;
import com.ftgo.common.UnsupportedStateTransitionException;
import com.ftgo.domain.OrderMinimumNotMetException;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final Tracer tracer;

  public GlobalExceptionHandler(Tracer tracer) {
    this.tracer = tracer;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(
      MethodArgumentNotValidException ex) {
    List<ErrorResponse.FieldError> fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fe ->
                    new ErrorResponse.FieldError(
                        fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()))
            .collect(Collectors.toList());

    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.VALIDATION_ERROR,
            "Validation failed",
            fieldErrors,
            Instant.now(),
            currentTraceId());

    log.warn("Validation failed: {}", fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      ConstraintViolationException ex) {
    List<ErrorResponse.FieldError> fieldErrors =
        ex.getConstraintViolations().stream()
            .map(
                cv ->
                    new ErrorResponse.FieldError(
                        cv.getPropertyPath().toString(),
                        cv.getInvalidValue(),
                        cv.getMessage()))
            .collect(Collectors.toList());

    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.VALIDATION_ERROR,
            "Constraint violation",
            fieldErrors,
            Instant.now(),
            currentTraceId());

    log.warn("Constraint violation: {}", fieldErrors);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex) {
    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.VALIDATION_ERROR,
            "Malformed request body",
            null,
            Instant.now(),
            currentTraceId());

    log.warn("Malformed request body: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    String detail =
        String.format(
            "Parameter '%s' must be of type %s",
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.VALIDATION_ERROR, detail, null, Instant.now(), currentTraceId());

    log.warn("Type mismatch: {}", detail);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.RESOURCE_NOT_FOUND,
            ex.getMessage(),
            null,
            Instant.now(),
            currentTraceId());

    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
    String detail =
        String.format("No endpoint %s %s", ex.getHttpMethod(), ex.getRequestURL());

    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.RESOURCE_NOT_FOUND, detail, null, Instant.now(), currentTraceId());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
  }

  @ExceptionHandler(UnsupportedStateTransitionException.class)
  public ResponseEntity<ErrorResponse> handleStateConflict(
      UnsupportedStateTransitionException ex) {
    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.STATE_CONFLICT,
            ex.getMessage(),
            null,
            Instant.now(),
            currentTraceId());

    log.warn("State conflict: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
  }

  @ExceptionHandler(OrderMinimumNotMetException.class)
  public ResponseEntity<ErrorResponse> handleOrderMinimumNotMet(
      OrderMinimumNotMetException ex) {
    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.BUSINESS_RULE_VIOLATION,
            ex.getMessage(),
            null,
            Instant.now(),
            currentTraceId());

    log.warn("Business rule violation: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
  }

  @ExceptionHandler(NotYetImplementedException.class)
  public ResponseEntity<ErrorResponse> handleNotYetImplemented(NotYetImplementedException ex) {
    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.NOT_IMPLEMENTED,
            "This operation is not yet implemented",
            null,
            Instant.now(),
            currentTraceId());

    log.info("Not yet implemented endpoint called");
    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(body);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex) {
    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.METHOD_NOT_ALLOWED,
            ex.getMessage(),
            null,
            Instant.now(),
            currentTraceId());

    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleUnsupportedMediaType(
      HttpMediaTypeNotSupportedException ex) {
    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.UNSUPPORTED_MEDIA_TYPE,
            ex.getMessage(),
            null,
            Instant.now(),
            currentTraceId());

    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
    log.error("Unhandled exception", ex);

    ErrorResponse body =
        new ErrorResponse(
            ErrorCode.INTERNAL_ERROR,
            "An unexpected error occurred",
            null,
            Instant.now(),
            currentTraceId());

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  private String currentTraceId() {
    if (tracer != null) {
      Span span = tracer.currentSpan();
      if (span != null) {
        return span.context().traceId();
      }
    }
    return null;
  }
}
