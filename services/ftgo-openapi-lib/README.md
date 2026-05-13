# ftgo-openapi-lib

Shared OpenAPI 3.0 library for FTGO microservices. Replaces the deprecated
Springfox / Swagger 2.x module (`common-swagger`).

## What It Provides

| Package | Purpose |
|---------|---------|
| `com.ftgo.openapi.config` | Auto-configured `OpenAPI` bean with JWT security scheme, Swagger UI defaults |
| `com.ftgo.openapi.model` | Standardized `ApiErrorResponse` and `PagedResponse<T>` DTOs |
| `com.ftgo.openapi.annotation` | `@ApiPageable`, `@ApiStandardResponses` composite annotations |

## Usage

### 1. Add the dependency

```groovy
dependencies {
    implementation project(':services:ftgo-openapi-lib')
}
```

### 2. Configure service metadata

In each service's `application.yml`:

```yaml
ftgo:
  openapi:
    title: Order Service API
    description: Manages order lifecycle
    version: 1.0.0
    server-url: http://localhost:8082

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

### 3. Access Swagger UI

Start the service and navigate to:

- **Swagger UI:** `http://localhost:<port>/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:<port>/v3/api-docs`

### 4. Annotate controllers

```java
@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Order management")
public class OrderController {

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    @ApiStandardResponses
    public OrderDTO getOrder(@PathVariable Long id) { /* ... */ }

    @GetMapping
    @Operation(summary = "List orders")
    @ApiPageable
    public PagedResponse<OrderDTO> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) { /* ... */ }
}
```

## Spring Boot 3.x Migration

When services migrate to Spring Boot 3.x, switch to the `springdoc-openapi-starter-webmvc-ui`
dependency declared in `gradle/libs.versions.toml`:

```groovy
dependencies {
    implementation libs.springdoc.openapi.starter.webmvc.ui
}
```

The `spring.factories` auto-configuration will be replaced by
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
