# FTGO REST API Standards

This document defines the REST API standards for all FTGO microservices.
Every new or migrated endpoint **must** follow these conventions.

---

## 1. URL Naming Conventions

### Base Path

All API endpoints use a versioned base path:

```
/api/v1/<resource>
```

### Rules

| Rule | Example | Anti-pattern |
|------|---------|--------------|
| Use **plural nouns** for collections | `/api/v1/orders` | `/api/v1/order` |
| Use **kebab-case** for multi-word resources | `/api/v1/menu-items` | `/api/v1/menuItems` |
| Use path parameters for single resources | `/api/v1/orders/{orderId}` | `/api/v1/orders?id=42` |
| Nest sub-resources one level deep (max) | `/api/v1/orders/{orderId}/line-items` | `/api/v1/orders/{id}/line-items/{itemId}/options` |
| Use query parameters for filtering | `/api/v1/orders?status=APPROVED` | `/api/v1/orders/approved` |
| No trailing slashes | `/api/v1/orders` | `/api/v1/orders/` |
| No verbs in URLs | `/api/v1/orders` (POST to create) | `/api/v1/createOrder` |

### Service URL Map

| Service | Port | Base Path |
|---------|------|-----------|
| Consumer Service | 8081 | `/api/v1/consumers` |
| Restaurant Service | 8082 | `/api/v1/restaurants` |
| Order Service | 8083 | `/api/v1/orders` |
| Courier Service | 8084 | `/api/v1/couriers` |

---

## 2. HTTP Methods

| Method | Purpose | Idempotent | Request Body | Success Code |
|--------|---------|------------|--------------|--------------|
| `GET` | Retrieve resource(s) | Yes | No | `200 OK` |
| `POST` | Create a resource | No | Yes | `201 Created` |
| `PUT` | Full replacement of a resource | Yes | Yes | `200 OK` |
| `PATCH` | Partial update of a resource | No | Yes | `200 OK` |
| `DELETE` | Remove a resource | Yes | No | `204 No Content` |

---

## 3. HTTP Status Codes

### Success

| Code | When to Use |
|------|-------------|
| `200 OK` | GET, PUT, PATCH returning the updated resource |
| `201 Created` | POST when a new resource is created; include `Location` header |
| `204 No Content` | DELETE, or PUT/PATCH when no body is returned |

### Client Errors

| Code | When to Use |
|------|-------------|
| `400 Bad Request` | Malformed JSON, validation failure, missing required fields |
| `401 Unauthorized` | Missing or invalid JWT token |
| `403 Forbidden` | Valid token but insufficient permissions |
| `404 Not Found` | Resource does not exist |
| `409 Conflict` | Business rule violation (e.g., duplicate email, invalid state transition) |
| `422 Unprocessable Entity` | Semantically invalid request (structurally valid JSON, but business logic rejects it) |

### Server Errors

| Code | When to Use |
|------|-------------|
| `500 Internal Server Error` | Unexpected failure (never expose stack traces) |
| `503 Service Unavailable` | Service is temporarily overloaded or in maintenance |

---

## 4. Error Response Format

All error responses use a consistent JSON structure defined by
`com.ftgo.openapi.model.ApiErrorResponse`:

```json
{
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Validation failed for 2 field(s)",
  "path": "/api/v1/orders",
  "timestamp": "2024-06-15T10:30:00Z",
  "fieldErrors": [
    {
      "field": "quantity",
      "rejectedValue": -1,
      "message": "must be greater than 0"
    },
    {
      "field": "restaurantId",
      "rejectedValue": null,
      "message": "must not be null"
    }
  ]
}
```

### Error Categories

| `error` Value | HTTP Status | Description |
|---------------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Bean-validation or request-parsing failure |
| `AUTHENTICATION_ERROR` | 401 | Missing or invalid credentials |
| `AUTHORIZATION_ERROR` | 403 | Insufficient permissions |
| `RESOURCE_NOT_FOUND` | 404 | Entity not found |
| `CONFLICT` | 409 | Business rule or uniqueness violation |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## 5. Pagination Format

All list endpoints returning unbounded collections **must** use pagination.
The standard format is defined by `com.ftgo.openapi.model.PagedResponse<T>`:

### Request Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | `int` | `0` | Page number (0-based) |
| `size` | `int` | `20` | Items per page (max 100) |
| `sort` | `string` | `createdAt,desc` | Sort field and direction |

### Response Shape

```json
{
  "content": [
    { "id": 1, "status": "APPROVED" },
    { "id": 2, "status": "PENDING" }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 42,
    "totalPages": 3
  }
}
```

---

## 6. API Versioning Strategy

FTGO uses **URL-path versioning**:

```
/api/v1/orders
/api/v2/orders   (future)
```

### Rules

1. The current version is **`v1`**.
2. Breaking changes (removed fields, changed types, removed endpoints) require a new version.
3. Additive changes (new optional fields, new endpoints) do **not** require a new version.
4. Deprecated versions are supported for a minimum of **6 months** after the next version is released.
5. Deprecation is signaled via the `Sunset` HTTP header and OpenAPI `@Deprecated` annotation.

### What Constitutes a Breaking Change

| Change | Breaking? |
|--------|-----------|
| Remove a response field | Yes |
| Rename a response field | Yes |
| Change a field's type | Yes |
| Add a new optional request field | No |
| Add a new response field | No |
| Add a new endpoint | No |
| Change default value of existing field | Yes |

---

## 7. Request / Response Conventions

### Content Type

- All requests and responses use `application/json`.
- Use `Content-Type: application/json` for request bodies.
- Use `Accept: application/json` for responses.

### Naming

- JSON field names use **camelCase**: `orderId`, `lineItems`, `createdAt`.
- Enum values use **UPPER_SNAKE_CASE**: `PENDING`, `APPROVED`, `CANCELLED`.

### Timestamps

- All timestamps are **ISO-8601** in **UTC**: `2024-06-15T10:30:00Z`.
- Use `Instant` in Java, serialized as a string.

### Identifiers

- Entity IDs are `Long` values.
- External-facing IDs may use UUIDs in future versions.

### Null Handling

- Omit `null` fields from responses (configure Jackson with `NON_NULL`).
- Clients must not rely on the absence of a field; treat missing fields as `null`.

---

## 8. Authentication & Security

All endpoints require JWT authentication unless explicitly documented as public.

### Authorization Header

```
Authorization: Bearer <jwt-token>
```

### Public Endpoints

Only the following endpoints are accessible without a token:

- `GET /api/v1/restaurants` (list restaurants)
- `POST /api/v1/consumers` (registration)
- `POST /api/v1/auth/login` (authentication)
- Health/readiness probes: `GET /actuator/health`

### OpenAPI Security Scheme

JWT is declared globally via a `bearerAuth` security scheme (see `FtgoOpenApiAutoConfiguration`).
Endpoints that do not require authentication should override with:

```java
@SecurityRequirements  // removes the global security requirement
```

---

## 9. OpenAPI Documentation Requirements

### Annotations

Every controller **must** have:
- `@Tag` on the class with `name` and `description`
- `@Operation` on each method with `summary`
- `@ApiStandardResponses` (from `ftgo-openapi-lib`) for common error responses
- `@ApiPageable` on paginated list endpoints

### Example

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order lifecycle management")
public class OrderController {

    @PostMapping
    @Operation(summary = "Create a new order")
    @ApiStandardResponses
    @ApiResponse(responseCode = "201", description = "Order created")
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        // ...
    }

    @GetMapping
    @Operation(summary = "List orders with pagination")
    @ApiPageable
    @ApiStandardResponses
    @ApiResponse(responseCode = "200", description = "Page of orders")
    public PagedResponse<OrderDTO> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        // ...
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID")
    @ApiStandardResponses
    @ApiResponse(responseCode = "200", description = "Order found")
    public OrderDTO getOrder(@PathVariable Long orderId) {
        // ...
    }
}
```

### Swagger UI Access

Each service exposes its own Swagger UI:

| Service | Swagger UI URL |
|---------|---------------|
| Consumer Service | `http://localhost:8081/swagger-ui.html` |
| Restaurant Service | `http://localhost:8082/swagger-ui.html` |
| Order Service | `http://localhost:8083/swagger-ui.html` |
| Courier Service | `http://localhost:8084/swagger-ui.html` |

The raw OpenAPI 3.0 spec is available at `/v3/api-docs` (JSON) for each service.
