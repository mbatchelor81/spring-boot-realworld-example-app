# FTGO API Gateway — API Versioning Strategy

## Overview

The FTGO API Gateway supports **URI path-based API versioning**. Clients can
target a specific API version by including the version prefix in the request
path. When no version prefix is present the gateway assumes the current (v1)
version.

## Supported Versions

| Version | Path Prefix      | Status  |
|---------|------------------|---------|
| v1      | `/api/v1/...`    | Current |
| —       | `/api/...`       | Alias for v1 (default) |

## Route Examples

| Request Path                 | Routed To              |
|------------------------------|------------------------|
| `GET /api/consumers`         | Consumer Service:8081  |
| `GET /api/v1/consumers`      | Consumer Service:8081  |
| `POST /api/orders`           | Order Service:8083     |
| `POST /api/v1/orders`        | Order Service:8083     |
| `GET /api/restaurants`       | Restaurant Service:8082|
| `PUT /api/couriers/{id}`     | Courier Service:8084   |

## Adding a New Version

To introduce a v2 endpoint for a service:

1. Add a new route entry in `application.yml` with `Path=/api/v2/...`
   pointing to the v2 service instance.
2. Maintain the existing v1 route for backwards compatibility.
3. Communicate deprecation timelines to consumers.

```yaml
# Example: adding v2 for consumer service
- id: consumer-service-v2
  uri: http://ftgo-consumer-service-v2:8091
  predicates:
    - Path=/api/v2/consumers/**
  filters:
    - StripPrefix=1
    - name: CircuitBreaker
      args:
        name: consumerServiceV2CB
        fallbackUri: forward:/fallback
```

## Headers

Clients should include the following headers when calling the gateway:

| Header            | Required | Description                        |
|-------------------|----------|------------------------------------|
| `Authorization`   | Yes      | `Bearer <JWT>` token from Keycloak |
| `X-API-Key`       | No       | Used for rate-limit bucketing      |
| `X-Correlation-Id`| No       | Propagated through all services; auto-generated if absent |

## Rate Limiting

Rate limiting is applied per route with Redis-backed token bucket:

- **Replenish rate**: 20 requests/second
- **Burst capacity**: 40 requests
- Rate limit key: `X-API-Key` header, falling back to client IP

Exceeding the limit returns `HTTP 429 Too Many Requests`.

## Circuit Breaker

Each downstream service has an independent Resilience4j circuit breaker:

- **Sliding window**: 10 calls
- **Failure threshold**: 50%
- **Wait in open state**: 30 seconds
- **Timeout**: 10 seconds per call

When the circuit is open, requests receive `HTTP 503 Service Unavailable`
with a JSON fallback body.
