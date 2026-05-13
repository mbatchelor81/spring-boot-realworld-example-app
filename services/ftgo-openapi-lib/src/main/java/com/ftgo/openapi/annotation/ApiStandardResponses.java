package com.ftgo.openapi.annotation;

import com.ftgo.openapi.model.ApiErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation that documents the standard error responses returned by FTGO APIs.
 *
 * <p>Apply to controller methods alongside the success {@code @ApiResponse}:
 *
 * <pre>{@code
 * @GetMapping("/{id}")
 * @ApiStandardResponses
 * @ApiResponse(responseCode = "200", description = "Order found")
 * public OrderDTO getOrder(@PathVariable Long id) {
 *     // ...
 * }
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
  @ApiResponse(
      responseCode = "400",
      description = "Bad request — validation or parsing error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiErrorResponse.class))),
  @ApiResponse(
      responseCode = "401",
      description = "Unauthorized — missing or invalid JWT token",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiErrorResponse.class))),
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden — insufficient permissions",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiErrorResponse.class))),
  @ApiResponse(
      responseCode = "404",
      description = "Resource not found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiErrorResponse.class))),
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = ApiErrorResponse.class)))
})
public @interface ApiStandardResponses {}
