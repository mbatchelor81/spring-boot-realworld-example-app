# Data Synchronization Strategy

**Jira:** EM-29  
**Status:** Accepted  
**Date:** 2026-05-13

---

## 1. Problem Statement

In the monolith, cross-service data access is a simple SQL JOIN. Once each
service owns its own database, JOINs across service boundaries are no longer
possible. Services must synchronize the data they need through asynchronous
events and local read-model caches.

## 2. Synchronization Patterns

### 2.1 Event-Carried State Transfer

Each service publishes domain events when its owned data changes. Other
services subscribe to these events and maintain a **local read-only copy**
of the data they need.

| Producer Service  | Event                      | Consumer Service   | Local Cache Table              |
|-------------------|----------------------------|--------------------|--------------------------------|
| consumer-service  | `ConsumerCreatedEvent`     | order-service      | `consumer_cache(id, name)`     |
| restaurant-service| `RestaurantCreatedEvent`   | order-service      | `restaurant_cache(id, name)`   |
| order-service     | `OrderCreatedEvent`        | courier-service    | (used to create delivery plan) |
| order-service     | `OrderStateChangedEvent`   | courier-service    | (update action status)         |
| courier-service   | `CourierAvailableEvent`    | order-service      | (used for courier assignment)  |

### 2.2 API Composition (Query)

For read operations that span multiple services (e.g., "show order details
with restaurant name and courier info"), an API Gateway or BFF (Backend for
Frontend) composes responses by calling multiple service APIs.

```
Client → API Gateway
              ├── GET /orders/{id}           → order-service
              ├── GET /restaurants/{id}       → restaurant-service
              └── GET /couriers/{id}          → courier-service
              ← composed response
```

### 2.3 Command-Side Validation

Before creating or modifying data that references another service, the
owning service validates the reference via a synchronous API call or
command:

| Operation         | Validation                                 | Mechanism                     |
|-------------------|--------------------------------------------|-------------------------------|
| Create Order      | Consumer exists and is valid               | `ValidateOrderByConsumer`     |
| Create Order      | Restaurant exists, menu items valid        | REST call to restaurant-service |
| Schedule Delivery | Courier exists and is available            | `CourierAvailableEvent`       |

## 3. Eventual Consistency Guarantees

### 3.1 At-Least-Once Delivery

Events are published with at-least-once semantics. Consumer services must
be **idempotent** — processing the same event twice must not corrupt data.

Implementation:
- Each event carries a unique `eventId`.
- Consumer services maintain a `processed_events` table to deduplicate.

### 3.2 Ordering Guarantees

Events for the same aggregate (e.g., all events for order #42) are
published to the same partition/queue, ensuring in-order processing.
Events across different aggregates may arrive out of order.

### 3.3 Failure Handling

| Failure Scenario                  | Response                                              |
|-----------------------------------|-------------------------------------------------------|
| Event consumer is down            | Events queue up; processed on recovery                |
| Stale cache (event not yet received) | Query falls back to synchronous API call           |
| Referenced entity deleted         | Compensating transaction (e.g., cancel affected orders) |
| Event processing fails            | Retry with exponential backoff; dead-letter after N retries |

## 4. Transition Plan

### Phase 1: Shared Database (Current)

All services share the monolith database. Cross-service references use
database FKs. No synchronization needed.

### Phase 2: Dual-Write

Services begin publishing events alongside database writes. Event consumers
start building local caches. FKs are still present but not relied upon by
application code.

### Phase 3: Database Split

Each service points to its own database. FKs are dropped. Application
validates references via events and API calls. Local caches serve read
operations.

### Phase 4: Event-Only

Direct synchronous API calls for validation are replaced by saga
orchestration. All cross-service communication flows through events.

## 5. Local Cache Schema (Future Migrations)

When services transition to Phase 3, each service that needs cross-service
data will add local cache tables. These will be created as future Flyway
migrations (e.g., `V2__create_consumer_cache.sql` in order-service).

Example cache table for order-service:

```sql
-- V2__create_consumer_cache.sql (order-service, Phase 3)
CREATE TABLE consumer_cache (
    consumer_id  BIGINT PRIMARY KEY,
    first_name   VARCHAR(255),
    last_name    VARCHAR(255),
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- V3__create_restaurant_cache.sql (order-service, Phase 3)
CREATE TABLE restaurant_cache (
    restaurant_id  BIGINT PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    last_updated   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
