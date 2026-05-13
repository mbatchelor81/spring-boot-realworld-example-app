# OpenAPI Annotations Guide

This guide shows how to annotate FTGO microservice controllers using
SpringDoc OpenAPI 3.0 and the shared annotations from `ftgo-openapi-lib`.

---

## Quick Reference

| Annotation | Source | Purpose |
|------------|--------|---------|
| `@Tag` | `io.swagger.v3.oas.annotations.tags` | Group endpoints on a controller |
| `@Operation` | `io.swagger.v3.oas.annotations` | Describe a single endpoint |
| `@ApiResponse` | `io.swagger.v3.oas.annotations.responses` | Document a response code |
| `@Schema` | `io.swagger.v3.oas.annotations.media` | Describe a model or field |
| `@Parameter` | `io.swagger.v3.oas.annotations.parameters` | Describe a request parameter |
| `@ApiStandardResponses` | `com.ftgo.openapi.annotation` | Standard 400/401/403/404/500 errors |
| `@ApiPageable` | `com.ftgo.openapi.annotation` | Standard page/size/sort parameters |
| `@SecurityRequirements` | `io.swagger.v3.oas.annotations.security` | Override/remove security on an endpoint |

---

## Example: Full Controller

```java
package com.ftgo.restaurant.api;

import com.ftgo.openapi.annotation.ApiPageable;
import com.ftgo.openapi.annotation.ApiStandardResponses;
import com.ftgo.openapi.model.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/restaurants")
@Tag(name = "Restaurants", description = "Restaurant onboarding and menu management")
public class RestaurantController {

    // -- Public endpoint (no JWT required) ----------------------------------

    @GetMapping
    @Operation(summary = "List restaurants", description = "Returns a paginated list of restaurants. Public endpoint.")
    @SecurityRequirements  // overrides global bearerAuth
    @ApiPageable
    @ApiResponse(responseCode = "200", description = "Page of restaurants")
    public PagedResponse<RestaurantDTO> listRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        // ...
    }

    // -- Authenticated endpoints --------------------------------------------

    @PostMapping
    @Operation(summary = "Create a restaurant")
    @ApiStandardResponses
    @ApiResponse(responseCode = "201", description = "Restaurant created")
    public ResponseEntity<RestaurantDTO> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {
        // return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/{restaurantId}")
    @Operation(summary = "Get restaurant by ID")
    @ApiStandardResponses
    @ApiResponse(responseCode = "200", description = "Restaurant found")
    public RestaurantDTO getRestaurant(
            @Parameter(description = "Restaurant ID", example = "42")
            @PathVariable Long restaurantId) {
        // ...
    }

    @PutMapping("/{restaurantId}/menu")
    @Operation(summary = "Replace restaurant menu")
    @ApiStandardResponses
    @ApiResponse(responseCode = "200", description = "Menu updated")
    public RestaurantDTO updateMenu(
            @PathVariable Long restaurantId,
            @Valid @RequestBody UpdateMenuRequest request) {
        // ...
    }

    @DeleteMapping("/{restaurantId}")
    @Operation(summary = "Delete a restaurant")
    @ApiStandardResponses
    @ApiResponse(responseCode = "204", description = "Restaurant deleted")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long restaurantId) {
        // return ResponseEntity.noContent().build();
    }
}
```

---

## Example: DTO with Schema Annotations

```java
package com.ftgo.restaurant.api;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Schema(description = "Request to create a new restaurant")
public class CreateRestaurantRequest {

    @Schema(description = "Restaurant name", example = "Ajanta", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(max = 100)
    private String name;

    @Schema(description = "Street address", example = "1888 Solano Ave, Berkeley, CA")
    @NotBlank
    private String address;

    // getters/setters
}
```

---

## Example: Enum Documentation

```java
@Schema(description = "Order lifecycle state")
public enum OrderState {
    @Schema(description = "Order submitted, awaiting restaurant approval")
    PENDING,

    @Schema(description = "Restaurant accepted the order")
    APPROVED,

    @Schema(description = "Courier assigned for pickup")
    PICKED_UP,

    @Schema(description = "Order delivered to consumer")
    DELIVERED,

    @Schema(description = "Order was cancelled")
    CANCELLED
}
```

---

## Common Patterns

### Hiding Internal Endpoints

```java
@Hidden  // endpoint will not appear in Swagger UI
@GetMapping("/internal/health-check")
public String internalHealth() { return "ok"; }
```

### Deprecating an Endpoint

```java
@Deprecated
@Operation(summary = "Get order (deprecated)", deprecated = true)
@GetMapping("/orders/{id}")
public OrderDTO getOrderLegacy(@PathVariable Long id) { /* ... */ }
```

### Multiple Response Types

```java
@GetMapping("/{id}")
@Operation(summary = "Get order by ID")
@ApiResponse(responseCode = "200", description = "Order found",
    content = @Content(schema = @Schema(implementation = OrderDTO.class)))
@ApiResponse(responseCode = "404", description = "Order not found",
    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
public OrderDTO getOrder(@PathVariable Long id) { /* ... */ }
```
