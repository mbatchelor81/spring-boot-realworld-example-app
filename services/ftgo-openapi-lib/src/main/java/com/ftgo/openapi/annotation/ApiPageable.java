package com.ftgo.openapi.annotation;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Composite annotation for paginated endpoints.
 *
 * <p>Apply to controller methods that accept pagination parameters:
 *
 * <pre>{@code
 * @GetMapping
 * @ApiPageable
 * public PagedResponse<OrderDTO> listOrders(
 *     @RequestParam(defaultValue = "0") int page,
 *     @RequestParam(defaultValue = "20") int size,
 *     @RequestParam(defaultValue = "createdAt,desc") String sort) {
 *     // ...
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(
    name = "page",
    in = ParameterIn.QUERY,
    description = "Page number (0-based)",
    schema = @Schema(type = "integer", defaultValue = "0"))
@Parameter(
    name = "size",
    in = ParameterIn.QUERY,
    description = "Page size",
    schema = @Schema(type = "integer", defaultValue = "20"))
@Parameter(
    name = "sort",
    in = ParameterIn.QUERY,
    description = "Sort criteria (e.g. createdAt,desc)",
    schema = @Schema(type = "string", defaultValue = "createdAt,desc"))
public @interface ApiPageable {}
