# ADR-001: Microservices Repository Structure and Naming Conventions

**Status:** Accepted  
**Date:** 2026-05-12  
**Deciders:** Platform Engineering Team  
**Jira:** EM-30

## Context

The current monolith (`spring-boot-realworld-example-app`) is a single-project Gradle build with all code under one `src/` tree using the `io.spring` package root. As part of the microservices migration initiative, we need to decompose the application into independently deployable services aligned with four bounded contexts: **Consumer**, **Restaurant**, **Order**, and **Courier**.

The FTGO reference monolith uses 14 modules in a flat Gradle multi-project layout. We need a clear repository structure that:
- Supports independent service development and deployment
- Enforces bounded context isolation at the package level
- Provides shared libraries for cross-cutting concerns
- Includes deployment artifacts (Docker, Kubernetes) alongside service code
- Scales to additional services without structural changes

## Decision

### 1. Repository Strategy: Mono-repo with Service Folders

We adopt a **mono-repo** approach with all microservices housed under a `services/` top-level directory. This is preferred over multi-repo for the following reasons:

- **Atomic refactoring:** Cross-service changes (shared library updates, API contract changes) can be done in a single commit/PR
- **Simplified CI:** A single pipeline can build and test all affected services on each commit
- **Shared tooling:** Build plugins, code style configuration (Spotless), and Gradle wrapper are shared
- **Easier onboarding:** New developers clone one repo to get the full system
- **Gradual extraction:** Individual services can be extracted to separate repos later if needed

### 2. Directory Structure

```
spring-boot-realworld-example-app/        # Repository root
├── settings.gradle                        # Multi-project build definition
├── build.gradle                           # Root project (existing monolith)
├── src/                                   # Existing monolith source (unchanged)
├── services/                              # All microservice modules
│   ├── ftgo-common/                       # Shared library
│   │   ├── build.gradle
│   │   └── src/
│   │       ├── main/java/com/ftgo/common/
│   │       └── test/java/com/ftgo/common/
│   ├── ftgo-common-jpa/                   # Shared JPA utilities
│   │   ├── build.gradle
│   │   └── src/
│   │       ├── main/java/com/ftgo/common/jpa/
│   │       └── test/java/com/ftgo/common/jpa/
│   ├── ftgo-consumer-service/             # Consumer bounded context
│   │   ├── build.gradle
│   │   ├── docker/
│   │   │   └── Dockerfile
│   │   ├── k8s/
│   │   │   ├── deployment.yaml
│   │   │   └── service.yaml
│   │   └── src/
│   │       ├── main/
│   │       │   ├── java/com/ftgo/consumer/
│   │       │   │   ├── ConsumerServiceApplication.java
│   │       │   │   ├── api/               # REST controllers, DTOs
│   │       │   │   ├── domain/            # Entities, aggregates, repositories
│   │       │   │   └── infrastructure/    # JPA repos, messaging adapters
│   │       │   └── resources/
│   │       │       ├── application.yml
│   │       │       └── db/migration/      # Flyway migrations
│   │       └── test/java/com/ftgo/consumer/
│   │           ├── api/                   # Controller tests
│   │           └── domain/               # Domain unit tests
│   ├── ftgo-consumer-service-api/         # Consumer API contracts
│   │   ├── build.gradle
│   │   └── src/main/java/com/ftgo/consumer/api/
│   │       ├── events/                    # Domain events
│   │       └── commands/                  # Service commands
│   ├── ftgo-restaurant-service/           # (same structure as consumer)
│   ├── ftgo-restaurant-service-api/
│   ├── ftgo-order-service/                # (same structure as consumer)
│   ├── ftgo-order-service-api/
│   ├── ftgo-courier-service/              # (same structure as consumer)
│   └── ftgo-courier-service-api/
└── docs/
    └── adr/                               # Architecture Decision Records
        └── 001-microservices-repo-structure.md
```

### 3. Package Naming Convention

All microservice code uses the `com.ftgo` root package, organized by bounded context and architectural layer:

| Module Type | Package Pattern | Example |
|-------------|----------------|---------|
| Service implementation | `com.ftgo.<context>` | `com.ftgo.consumer` |
| REST controllers | `com.ftgo.<context>.api` | `com.ftgo.consumer.api` |
| Domain entities | `com.ftgo.<context>.domain` | `com.ftgo.consumer.domain` |
| Infrastructure | `com.ftgo.<context>.infrastructure` | `com.ftgo.consumer.infrastructure` |
| API events | `com.ftgo.<context>.api.events` | `com.ftgo.consumer.api.events` |
| API commands | `com.ftgo.<context>.api.commands` | `com.ftgo.consumer.api.commands` |
| Shared library | `com.ftgo.common` | `com.ftgo.common` |
| Shared JPA | `com.ftgo.common.jpa` | `com.ftgo.common.jpa` |

**Rules:**
- Context names are **singular** and lowercase: `consumer`, `restaurant`, `order`, `courier`
- Layer packages mirror DDD tactical patterns: `api`, `domain`, `infrastructure`
- The existing monolith retains its `io.spring` package root; new services use `com.ftgo`

### 4. Module Naming Convention

Gradle subproject names follow the pattern `ftgo-<context>-service` for implementations and `ftgo-<context>-service-api` for API contract modules:

| Module Name | Type | Gradle Path |
|-------------|------|-------------|
| `ftgo-common` | Shared library | `:services:ftgo-common` |
| `ftgo-common-jpa` | Shared JPA library | `:services:ftgo-common-jpa` |
| `ftgo-consumer-service` | Spring Boot app | `:services:ftgo-consumer-service` |
| `ftgo-consumer-service-api` | API contracts | `:services:ftgo-consumer-service-api` |
| `ftgo-restaurant-service` | Spring Boot app | `:services:ftgo-restaurant-service` |
| `ftgo-restaurant-service-api` | API contracts | `:services:ftgo-restaurant-service-api` |
| `ftgo-order-service` | Spring Boot app | `:services:ftgo-order-service` |
| `ftgo-order-service-api` | API contracts | `:services:ftgo-order-service-api` |
| `ftgo-courier-service` | Spring Boot app | `:services:ftgo-courier-service` |
| `ftgo-courier-service-api` | API contracts | `:services:ftgo-courier-service-api` |

**Naming rules:**
- Service implementations use `org.springframework.boot` and `io.spring.dependency-management` plugins
- API modules use `java-library` plugin (no Spring Boot dependency)
- Shared libraries use `java-library` plugin with `api` dependency scope

### 5. Service Port Assignments

Each service has a designated default port to avoid conflicts during local development:

| Service | Port |
|---------|------|
| Monolith (existing) | 8080 |
| Consumer Service | 8081 |
| Restaurant Service | 8082 |
| Order Service | 8083 |
| Courier Service | 8084 |

### 6. Dependency Rules

```
ftgo-common          ← no project dependencies (foundation)
ftgo-common-jpa      ← ftgo-common
ftgo-*-service-api   ← ftgo-common (events & commands only)
ftgo-*-service       ← ftgo-common, own -api module
ftgo-order-service   ← additionally depends on consumer-api, restaurant-api
```

**Inter-service communication** is via API modules only. Services must **never** directly depend on another service's implementation module.

## Consequences

### Positive
- Clear bounded context isolation enforced by package structure
- API modules define explicit contracts between services
- Shared code is centralized in `ftgo-common` / `ftgo-common-jpa`
- Each service is independently buildable: `./gradlew :services:ftgo-consumer-service:build`
- Deployment artifacts (Docker/K8s) co-located with service code
- New services follow the same template structure

### Negative
- Mono-repo requires disciplined dependency management to avoid coupling
- Build times increase as more services are added (mitigated by Gradle build cache)
- All teams share the same CI pipeline (may need per-service triggers later)

### Risks
- Shared library changes may require coordinated releases across services
- Mono-repo may need to be split if team autonomy becomes a priority

## Compliance

This ADR satisfies the acceptance criteria for EM-30:
- [x] Repository structure documented with directory layout diagram
- [x] Package naming conventions defined and documented
- [x] Template repository structure created (each service follows the standard layout)
- [x] All 4 bounded contexts have designated service directories
- [x] ADR written for team review
