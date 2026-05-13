# Per-Service Database Schema Design

**Jira:** EM-29  
**Status:** Accepted  
**Date:** 2026-05-13

---

## 1. Overview

This document defines the database-per-service schema strategy for the FTGO
microservices migration. The monolith currently uses a single shared MySQL
database (`ftgo`) with one Flyway migration set (`V1__create_ftgo_db.sql`)
that creates all seven tables. Each microservice will own an independent
database schema with its own Flyway migration history.

## 2. Current State (Monolith)

### Shared Database: `ftgo`

| Table                   | Owner Context | Cross-Service FKs                          |
|-------------------------|---------------|--------------------------------------------|
| `consumers`             | Consumer      | —                                          |
| `restaurants`           | Restaurant    | —                                          |
| `restaurant_menu_items` | Restaurant    | FK → `restaurants`                         |
| `orders`                | Order         | FK → `restaurants`, FK → `courier`         |
| `order_line_items`      | Order         | FK → `orders`                              |
| `courier`               | Courier       | —                                          |
| `courier_actions`       | Courier       | FK → `courier`, FK → `orders`              |

### Shared Sequence

All entities use `GenerationType.IDENTITY` (MySQL `AUTO_INCREMENT`).
In the monolith, `hibernate_sequence` is not used—each table has its own
auto-increment column. This carries forward cleanly to per-service databases.

## 3. Target State (Per-Service Databases)

Each service gets its own database. The naming convention is
`ftgo_<context>_db`:

| Service                 | Database               | Tables                                |
|-------------------------|------------------------|---------------------------------------|
| ftgo-consumer-service   | `ftgo_consumer_db`     | `consumers`                           |
| ftgo-restaurant-service | `ftgo_restaurant_db`   | `restaurants`, `restaurant_menu_items` |
| ftgo-order-service      | `ftgo_order_db`        | `orders`, `order_line_items`           |
| ftgo-courier-service    | `ftgo_courier_db`      | `courier`, `courier_actions`           |

### Per-Service Schema Diagrams

#### ftgo-consumer-service

```
consumers
├── id          BIGINT PK AUTO_INCREMENT
├── first_name  VARCHAR(255)
└── last_name   VARCHAR(255)
```

#### ftgo-restaurant-service

```
restaurants
├── id       BIGINT PK AUTO_INCREMENT
├── name     VARCHAR(255) NOT NULL
├── street1  VARCHAR(255)
├── street2  VARCHAR(255)
├── city     VARCHAR(255)
├── state    VARCHAR(255)
└── zip      VARCHAR(20)

restaurant_menu_items
├── restaurant_id  BIGINT FK → restaurants(id)
├── id             VARCHAR(255) NOT NULL
├── name           VARCHAR(255) NOT NULL
└── price          DECIMAL(19,2) NOT NULL
```

#### ftgo-order-service

```
orders
├── id                    BIGINT PK AUTO_INCREMENT
├── version               BIGINT
├── order_state           VARCHAR(30) NOT NULL
├── consumer_id           BIGINT NOT NULL          ← no FK (cross-service)
├── restaurant_id         BIGINT NOT NULL          ← no FK (cross-service)
├── delivery_address_street1 VARCHAR(255)
├── delivery_address_street2 VARCHAR(255)
├── delivery_address_city    VARCHAR(255)
├── delivery_address_state   VARCHAR(255)
├── delivery_address_zip     VARCHAR(20)
├── delivery_time         TIMESTAMP
├── payment_token         VARCHAR(255)
├── order_minimum         DECIMAL(19,2)
├── ready_by              TIMESTAMP
├── accept_time           TIMESTAMP
├── preparing_time        TIMESTAMP
├── ready_for_pickup_time TIMESTAMP
├── picked_up_time        TIMESTAMP
├── delivered_time        TIMESTAMP
└── assigned_courier_id   BIGINT                   ← no FK (cross-service)

order_line_items
├── order_id     BIGINT FK → orders(id)
├── menu_item_id VARCHAR(255) NOT NULL
├── name         VARCHAR(255) NOT NULL
├── price        DECIMAL(19,2) NOT NULL
└── quantity     INT NOT NULL
```

#### ftgo-courier-service

```
courier
├── id          BIGINT PK AUTO_INCREMENT
├── first_name  VARCHAR(255)
├── last_name   VARCHAR(255)
├── street1     VARCHAR(255)
├── street2     VARCHAR(255)
├── city        VARCHAR(255)
├── state       VARCHAR(255)
├── zip         VARCHAR(20)
└── available   BOOLEAN DEFAULT FALSE

courier_actions
├── courier_id  BIGINT FK → courier(id)
├── type        VARCHAR(30) NOT NULL
├── time        TIMESTAMP
└── order_id    BIGINT NOT NULL                    ← no FK (cross-service)
```

## 4. Cross-Service Foreign Key Removal Plan

### FKs to Remove

| Source Table      | Column               | Current FK Target      | Replacement Strategy          |
|-------------------|----------------------|------------------------|-------------------------------|
| `orders`          | `restaurant_id`      | `restaurants(id)`      | Plain BIGINT, validated via API call or event |
| `orders`          | `assigned_courier_id` | `courier(id)`         | Plain BIGINT, set via `ScheduleDeliveryCommand` |
| `courier_actions` | `order_id`           | `orders(id)`           | Plain BIGINT, set via `ScheduleDeliveryCommand` |
| `orders`          | `consumer_id`        | `consumers(id)`        | Plain BIGINT, validated via `ValidateOrderByConsumer` command |

### Validation Strategy

Cross-service references are validated at the application layer, not the
database layer:

1. **Order creation** — The order service calls the consumer service
   (`ValidateOrderByConsumer` command) and the restaurant service
   (`CreateRestaurantCommand` / menu lookup) before persisting.
2. **Courier assignment** — The order service publishes an
   `OrderCreatedEvent`; the courier service responds with a
   `ScheduleDeliveryCommand` containing the courier ID.
3. **Stale reference handling** — If a referenced entity is deleted or
   unavailable, the owning service handles it via compensating transactions
   (e.g., cancel the order if the restaurant no longer exists).

## 5. ID Generation Strategy

### Current Approach (Monolith)

All entities use `@GeneratedValue(strategy = GenerationType.IDENTITY)`,
which delegates to the database's `AUTO_INCREMENT`. There is no shared
`hibernate_sequence`.

### Per-Service Approach

Each service database maintains its own `AUTO_INCREMENT` sequence per table.
Since IDs are scoped to a single service database, there is no risk of
collision.

| Service            | ID Type  | Strategy                     | Notes                        |
|--------------------|----------|------------------------------|------------------------------|
| consumer-service   | `BIGINT` | `GenerationType.IDENTITY`    | MySQL AUTO_INCREMENT         |
| restaurant-service | `BIGINT` | `GenerationType.IDENTITY`    | MySQL AUTO_INCREMENT         |
| order-service      | `BIGINT` | `GenerationType.IDENTITY`    | MySQL AUTO_INCREMENT         |
| courier-service    | `BIGINT` | `GenerationType.IDENTITY`    | MySQL AUTO_INCREMENT         |

**Cross-service ID references** use the raw `BIGINT` value. Since each
service owns its ID space independently, a `consumer_id = 5` in the order
database refers unambiguously to consumer #5 in the consumer database.

**Future consideration:** If services are later scaled to multiple database
shards or merged into a polyglot persistence layer, IDs should migrate to
UUIDs. This is out of scope for the initial migration.

## 6. Flyway Migration Structure

### Directory Layout

```
services/
├── ftgo-consumer-service/src/main/resources/db/migration/
│   └── V1__create_consumer_schema.sql
├── ftgo-restaurant-service/src/main/resources/db/migration/
│   └── V1__create_restaurant_schema.sql
├── ftgo-order-service/src/main/resources/db/migration/
│   └── V1__create_order_schema.sql
└── ftgo-courier-service/src/main/resources/db/migration/
    └── V1__create_courier_schema.sql
```

### Naming Conventions

| Element           | Convention                                      | Example                           |
|-------------------|-------------------------------------------------|-----------------------------------|
| Version prefix    | `V<n>__` (Flyway default)                       | `V1__`, `V2__`                    |
| Description       | `snake_case`, action-first                      | `create_consumer_schema`          |
| Schema changes    | `V<n>__add_<column>_to_<table>.sql`             | `V2__add_email_to_consumers.sql`  |
| Data migrations   | `V<n>__seed_<context>_data.sql`                 | `V2__seed_consumer_data.sql`      |
| Repeatable        | `R__<description>.sql`                          | `R__consumer_views.sql`           |

### Flyway Configuration (per-service `application.yml`)

Each service already has `spring.flyway.enabled: true` and
`spring.jpa.hibernate.ddl-auto: validate` configured. Flyway runs
migrations from the classpath location `db/migration/` which maps to
each service's `src/main/resources/db/migration/` directory. Each service
maintains an independent `flyway_schema_history` table in its own database.

### Versioning Rules

1. **Never modify an existing versioned migration** once it has been applied
   to any environment (including local dev). Create a new versioned
   migration instead.
2. **Version numbers are per-service** — `V1` in consumer-service is
   independent of `V1` in order-service.
3. **Use sequential integers** (`V1`, `V2`, `V3`, …) rather than
   timestamps for readability.
4. **One logical change per migration** — do not bundle unrelated DDL
   changes into a single file.
