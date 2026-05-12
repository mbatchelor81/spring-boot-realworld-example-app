# Shared Gradle Configuration for FTGO Microservices

## Overview

This document describes the shared Gradle build infrastructure that standardizes
dependency management, build settings, and plugin versions across all FTGO
microservices. The configuration uses two Gradle features:

1. **Version Catalog** (`gradle/libs.versions.toml`) — centralizes every
   dependency and plugin version in one TOML file.
2. **Convention Plugins** (`gradle/plugins/`) — encapsulate reusable build logic
   so each service's `build.gradle` contains only service-specific declarations.

### Compatibility

| Component | Java | Spring Boot | Gradle |
|-----------|------|-------------|--------|
| Monolith (root `build.gradle`) | 11 | 2.6.3 | 7.6.4 |
| Microservices (convention plugins) | 17+ | 3.2.5 | 7.6.4 |

The monolith does **not** use the version catalog or convention plugins.
Its `build.gradle` retains its original dependency declarations unchanged.

---

## Version Catalog

**Location:** `gradle/libs.versions.toml`

The catalog declares every version, library coordinate, convenience bundle, and
plugin used by the microservices. Service `build.gradle` files reference
entries via the `libs` accessor:

```groovy
dependencies {
    implementation libs.spring.boot.starter.web
    implementation libs.flyway.core
    runtimeOnly    libs.h2
    testImplementation libs.bundles.rest.assured
}
```

### Key Version Targets

| Dependency | Version | Catalog Key |
|------------|---------|-------------|
| Spring Boot | 3.2.5 | `spring-boot` |
| Micrometer | 1.12.5 | `micrometer` |
| JUnit 5 | 5.10.2 | `junit-bom` |
| Rest-Assured | 5.4.0 | `rest-assured` |
| Flyway | 9.22.3 | `flyway` |
| Jackson BOM | 2.16.2 | `jackson-bom` |
| Lombok | 1.18.32 | `lombok` |
| Testcontainers | 1.19.7 | `testcontainers` |

---

## Convention Plugins

**Location:** `gradle/plugins/`

The plugins are compiled as a Gradle _included build_ referenced by
`pluginManagement { includeBuild 'gradle/plugins' }` in `settings.gradle`.
Each plugin ID is prefixed with `ftgo.` and can be applied in any service's
`build.gradle` with a simple `plugins { id '...' }` block.

### Plugin Reference

| Plugin ID | Purpose | Inherits |
|-----------|---------|----------|
| `ftgo.java-conventions` | Java 17 toolchain, UTF-8 encoding, Spotless formatting | — |
| `ftgo.spring-boot-conventions` | Spring Boot 3.x + dependency management, Lombok, Actuator, fat-jar packaging | `ftgo.java-conventions` |
| `ftgo.testing-conventions` | JUnit 5 Platform, Rest-Assured, AssertJ, test logging | `java` |
| `ftgo.docker-conventions` | `prepareDocker`, `generateDockerfile`, `dockerBuild` tasks | `java` |
| `ftgo.publishing-conventions` | Maven publication with sources and Javadoc JARs | `java-library`, `maven-publish` |

### `ftgo.java-conventions`

Base plugin applied by every module. Configures:

- `sourceCompatibility` / `targetCompatibility` = Java 17
- Java toolchain targeting JDK 17
- UTF-8 encoding for compilation and Javadoc
- Compiler args: `-parameters`, `-Xlint:deprecation`
- Spotless with Google Java Format

### `ftgo.spring-boot-conventions`

For Spring Boot application modules. Extends `ftgo.java-conventions` and adds:

- `org.springframework.boot` plugin (3.2.5)
- `io.spring.dependency-management` plugin (1.1.4)
- Micrometer BOM import
- Lombok (compile-only + annotation processor)
- Actuator starter
- Boot fat-jar packaging (`bootJar` enabled, plain `jar` disabled)
- `buildInfo()` for `/actuator/info`

### `ftgo.testing-conventions`

Adds standard test dependencies and configuration:

- `spring-boot-starter-test` (includes JUnit 5, Mockito, AssertJ)
- `spring-security-test`
- Rest-Assured + Spring MockMvc integration
- JUnit Platform runner
- Verbose test logging (passed/skipped/failed)

### `ftgo.docker-conventions`

Provides three tasks for Docker image creation:

- `prepareDocker` — copies the boot JAR (as `app.jar`) and any project-level
  `docker/Dockerfile` into `build/docker/`.
- `generateDockerfile` — creates a default `Dockerfile` using
  `eclipse-temurin:17-jre-alpine` if none exists.
- `dockerBuild` — runs `docker build` to produce an image tagged
  `ftgo/<service-name>:<version>`.

### `ftgo.publishing-conventions`

For shared library modules (e.g., `ftgo-common`, API modules). Configures:

- `maven-publish` with a `mavenJava` publication
- Source and Javadoc JARs
- Publishes to a local Maven repository at `build/maven-repo`

---

## How to Create a New Microservice

### 1. Add the module to `settings.gradle`

```groovy
include 'services:ftgo-my-new-service'
```

### 2. Create `services/ftgo-my-new-service/build.gradle`

A typical Spring Boot service requires fewer than 30 lines:

```groovy
plugins {
    id 'ftgo.spring-boot-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.docker-conventions'
}

version = '0.0.1-SNAPSHOT'

dependencies {
    // Service-specific dependencies only
    implementation project(':services:ftgo-common')
    implementation project(':services:ftgo-my-new-service-api')

    implementation libs.spring.boot.starter.web
    implementation libs.spring.boot.starter.data.jpa
    implementation libs.flyway.core

    runtimeOnly libs.h2
}
```

### 3. Create a shared library module

```groovy
plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.publishing-conventions'
}

version = '0.0.1-SNAPSHOT'

dependencies {
    api project(':services:ftgo-common')
}
```

### 4. Build and test

```bash
# Build everything
./gradlew build

# Build a single service
./gradlew :services:ftgo-my-new-service:build

# Run tests for a service
./gradlew :services:ftgo-my-new-service:test

# Build Docker image
./gradlew :services:ftgo-my-new-service:dockerBuild

# Apply code formatting
./gradlew spotlessApply
```

---

## Architecture Decision

The convention plugins use an **included build** (`gradle/plugins/`) rather
than `buildSrc/` to avoid classpath conflicts between the monolith's
Spring Boot 2.6.3 and the microservices' Spring Boot 3.2.5. With an included
build, each project's plugin resolution is isolated — the root project
continues to use Spring Boot 2.x while services can opt in to 3.x through
the convention plugins.

See also: [ADR-001 — Microservices Repository Structure](adr/001-microservices-repo-structure.md)
