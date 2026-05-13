# FTGO Logging Standards

This document defines the logging conventions, configuration, and best practices
for all FTGO microservices.

---

## 1. Log Levels

| Level   | Usage                                                                                              |
|---------|----------------------------------------------------------------------------------------------------|
| `ERROR` | Unrecoverable failures requiring immediate attention (failed transactions, data corruption, outages). |
| `WARN`  | Unexpected conditions the service can recover from (retries, fallback logic, degraded state).       |
| `INFO`  | Significant business events (order created, payment processed, service started/stopped).           |
| `DEBUG` | Diagnostic detail for troubleshooting (method parameters, intermediate state, query results).      |
| `TRACE` | Fine-grained diagnostics (loop iterations, wire-level payloads). Never enable in production.       |

### Rules

- Default production level: `INFO`.
- Never log at `INFO` inside tight loops or high-frequency paths.
- Use `DEBUG` for development diagnostics; guard expensive string building:
  ```java
  if (log.isDebugEnabled()) {
      log.debug("Order details: {}", order);
  }
  ```
- Reserve `ERROR` for situations that need on-call attention. A handled retry
  is `WARN`, not `ERROR`.

---

## 2. What to Log / What NOT to Log

### DO Log

- Service lifecycle events (startup, shutdown, health-check failures).
- Inbound request summary (method, path, status, duration) at `INFO`.
- Business-domain events (entity created, state transitions) at `INFO`.
- External service call results (status, latency) at `INFO` or `DEBUG`.
- Exception stack traces at `ERROR` or `WARN`.

### DO NOT Log

| Category                 | Examples                                            |
|--------------------------|-----------------------------------------------------|
| **Credentials**          | Passwords, API keys, tokens, secrets                |
| **PII**                  | Full credit-card numbers, SSNs, dates of birth      |
| **Session identifiers**  | Raw session cookies, JWT contents                   |
| **Full request bodies**  | May contain PII; log only safe, selected fields     |
| **Health-check noise**   | Successful health-check pings (use `TRACE` at most) |

If any of the above accidentally reach log output, the sensitive-data masking
layer (see Section 5) provides a safety net, but prevention is always preferred.

---

## 3. Structured Log Format

All deployed environments use **JSON structured logging** via
[LogstashEncoder](https://github.com/logfellow/logstash-logback-encoder).

### Standard Fields

Every log line includes:

| Field           | Source                   | Description                        |
|-----------------|--------------------------|------------------------------------|
| `@timestamp`    | Logback                  | ISO-8601 UTC timestamp             |
| `level`         | Logback                  | Log level (INFO, WARN, etc.)       |
| `logger_name`   | Logback                  | Logger class name (shortened)      |
| `thread_name`   | Logback                  | Thread name                        |
| `message`       | Application              | Log message                        |
| `traceId`       | MDC (ftgo-tracing-lib)   | Distributed trace identifier       |
| `spanId`        | MDC (ftgo-tracing-lib)   | Current span identifier            |
| `correlationId` | MDC (ftgo-logging-lib)   | Request correlation / causation ID |
| `serviceName`   | MDC (ftgo-logging-lib)   | `spring.application.name`          |
| `userId`        | MDC (application code)   | Authenticated user identifier      |
| `requestId`     | MDC (ftgo-logging-lib)   | Unique per-request identifier      |

### Local Development Format

Local dev uses human-readable pattern:
```
2024-01-15 10:30:45.123 [main] [abc123,def456] INFO  c.f.order.OrderService - Order created
```

Pattern: `%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] [%X{traceId:-},%X{spanId:-}] %-5level %logger{36} - %msg%n`

---

## 4. MDC (Mapped Diagnostic Context) Fields

The following MDC keys are set automatically by shared filters and should be
present in every log line:

| MDC Key         | Set By                          | Lifecycle           |
|-----------------|---------------------------------|---------------------|
| `traceId`       | `ftgo-tracing-lib`              | Per-request          |
| `spanId`        | `ftgo-tracing-lib`              | Per-span             |
| `correlationId` | `CorrelationIdFilter`           | Per-request          |
| `serviceName`   | `CorrelationIdFilter`           | Per-request          |
| `userId`        | Application / Security filter   | Per-request          |
| `requestId`     | `CorrelationIdFilter`           | Per-request          |

### Setting MDC in Application Code

Use `LogContext` to add or override MDC values:

```java
import com.ftgo.logging.LogContext;

// Set userId after authentication
LogContext.setUserId(authenticatedUser.getId());

// Add custom context for a business operation
LogContext.put("orderId", order.getId());

// Clear when done (typically handled by filter)
LogContext.clear();
```

---

## 5. Sensitive Data Masking

The logging library includes a `MaskingConverter` that automatically redacts
sensitive patterns in log output:

| Pattern                      | Example Input             | Masked Output           |
|------------------------------|---------------------------|-------------------------|
| Credit card numbers          | `4111111111111111`        | `4111********1111`      |
| Passwords in key=value       | `password=secret123`      | `password=********`     |
| Bearer tokens                | `Bearer eyJhbGciOi...`   | `Bearer [REDACTED]`     |
| Authorization headers        | `Authorization: Basic...` | `Authorization: [REDACTED]` |

The masking converter is applied in both JSON and console output formats.

> **Limitation:** Masking applies to the formatted log message only. Exception
> stack traces and MDC values are not masked. Always prevent sensitive data
> from reaching log statements rather than relying solely on masking.

---

## 6. Per-Environment Log Level Configuration

### Spring Profile Mapping

| Environment       | Spring Profile(s)        | App Log Level | Framework Level |
|-------------------|--------------------------|---------------|-----------------|
| Local development | `default` (no profile)   | `DEBUG`       | `INFO`          |
| Docker / K8s dev  | `docker`, `k8s`          | `INFO`        | `WARN`          |
| Staging           | `staging`                | `INFO`        | `WARN`          |
| Production        | `prod`                   | `INFO`        | `ERROR`         |

### Async Logging

Deployed environments (`docker`, `k8s`, `prod`, `staging`) use an async
appender to avoid blocking application threads on log I/O. Configuration:

- Queue size: `512` (configurable via `ftgo.logging.async-queue-size`)
- Discarding threshold: `0` (no level-based discarding; all levels are retained
  when queue capacity is available)
- Never block: `true` (events are silently dropped when queue is completely
  full, rather than blocking the application thread)
- Caller data: disabled (performance)

### File Rotation (Local Dev)

Local development includes a rolling file appender:

- Log file: `logs/${serviceName}.log`
- Max file size: `10MB`
- Max history: `7` days
- Total cap: `100MB`

---

## 7. Logging Aspect

The `LoggingAspect` automatically logs method entry and exit for service-layer
classes annotated with `@Service`:

```
DEBUG c.f.logging.LoggingAspect - --> OrderService.createOrder(CreateOrderRequest)
DEBUG c.f.logging.LoggingAspect - <-- OrderService.createOrder returned in 45ms
```

- Entry/exit logged at `DEBUG` level.
- Exceptions logged at `ERROR` level with full stack trace.
- Only active when `DEBUG` is enabled for `com.ftgo.logging.LoggingAspect`.

---

## 8. Integration with Existing Libraries

| Library              | Purpose                        | MDC Keys Provided         |
|----------------------|--------------------------------|---------------------------|
| `ftgo-tracing-lib`   | Distributed tracing (Brave)    | `traceId`, `spanId`       |
| `ftgo-logging-lib`   | Correlation, MDC, masking      | `correlationId`, `serviceName`, `userId`, `requestId` |
| EFK Stack            | Log aggregation & search       | Consumes JSON log output  |

---

## 9. Quick Start for New Services

1. Add `ftgo-logging-lib` dependency in `build.gradle`:
   ```groovy
   implementation project(':services:ftgo-logging-lib')
   ```

2. Include the shared logback config in `src/main/resources/logback-spring.xml`:
   ```xml
   <include resource="com/ftgo/logging/logback-shared.xml"/>
   ```

3. Set `spring.application.name` in `application.properties`:
   ```properties
   spring.application.name=ftgo-my-service
   ```

4. Use SLF4J for all logging:
   ```java
   private static final Logger log = LoggerFactory.getLogger(MyClass.class);
   log.info("Order {} created for user {}", orderId, userId);
   ```
