# Contributing to FTGO / Spring Boot RealWorld

Thank you for contributing! This document covers the guidelines and standards
for submitting code to this project.

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Branch Strategy](#branch-strategy)
3. [Code Style](#code-style)
4. [Static Analysis](#static-analysis)
5. [Testing Requirements](#testing-requirements)
6. [Pull Request Process](#pull-request-process)
7. [Code Review Guidelines](#code-review-guidelines)

---

## Getting Started

### Prerequisites

| Tool   | Version | Notes                         |
|--------|---------|-------------------------------|
| Java   | 17      | Microservices (Spring Boot 3) |
| Java   | 11      | Monolith (Spring Boot 2.6)    |
| Gradle | Wrapper | Always use `./gradlew`        |
| Node   | 16      | Frontend (Next.js)            |

### Build & Test

```bash
# Microservice module
./gradlew :services:ftgo-consumer-service:clean :services:ftgo-consumer-service:build

# Monolith
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
./gradlew clean build test -x jacocoTestCoverageVerification -x spotlessCheck
```

---

## Branch Strategy

| Branch                        | Purpose                               |
|-------------------------------|---------------------------------------|
| `master`                      | Stable monolith releases              |
| `feat/microservices-migration`| Active microservices migration trunk   |
| `devin/<timestamp>-<topic>`   | Feature / fix branches                |

- Branch from `feat/microservices-migration` for all microservices work.
- Branch from `master` only for monolith-only fixes.
- Use descriptive branch names: `devin/<timestamp>-add-order-validation`.

---

## Code Style

### Formatting

Code formatting is enforced by **Spotless** using **Google Java Format**.

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply
```

### EditorConfig

The project includes an `.editorconfig` file. Ensure your editor respects it:
- UTF-8 encoding
- LF line endings
- 2-space indent for Java, YAML
- 4-space indent for Gradle, XML
- Trailing whitespace trimmed

### Naming Conventions

| Element       | Convention   | Example                     |
|---------------|--------------|-----------------------------|
| Classes       | PascalCase   | `OrderService`              |
| Methods       | camelCase    | `createOrder()`             |
| Constants     | UPPER_SNAKE  | `MAX_RETRY_COUNT`           |
| Packages      | lowercase    | `com.ftgo.order.domain`     |
| Test classes  | `*Test`      | `OrderServiceTest`          |
| Test methods  | descriptive  | `createOrder_validInput_returnsCreated()` |

---

## Static Analysis

The following tools run automatically via the `ftgo.quality-conventions` plugin:

### Checkstyle

- Based on Google Java Style with project adjustments
- Config: `gradle/plugins/src/main/resources/config/checkstyle/checkstyle.xml`
- Zero warnings policy (`maxWarnings = 0`)

### PMD

- Rule sets: bestpractices, codestyle, design, errorprone, performance
- Console output enabled for fast feedback

### SpotBugs

- Effort: `max`, Report level: `medium`
- Finds common bug patterns (null dereference, resource leaks, etc.)

### JaCoCo

- Minimum **70% line coverage** enforced
- Reports: HTML + XML (for SonarQube integration)
- Generated code is excluded from coverage

### Running Analysis Locally

```bash
# Run all checks (includes Checkstyle, PMD, SpotBugs, tests, JaCoCo)
./gradlew :services:ftgo-consumer-service:check

# Individual tools
./gradlew :services:ftgo-consumer-service:checkstyleMain
./gradlew :services:ftgo-consumer-service:pmdMain
./gradlew :services:ftgo-consumer-service:spotbugsMain
./gradlew :services:ftgo-consumer-service:jacocoTestReport
```

---

## Testing Requirements

### Test Pyramid

| Level       | Framework            | When to Use                      |
|-------------|----------------------|----------------------------------|
| Unit        | JUnit 5 + Mockito    | Business logic, domain rules     |
| Integration | `@SpringBootTest`    | API contracts, DB queries        |
| E2E         | Selenium + TestNG    | Critical user flows              |

### Coverage

- **Minimum 70% line coverage** per module (enforced by JaCoCo).
- Focus on business logic and service layers.
- Generated code and DTOs are excluded from coverage.

### Running Tests

```bash
# All tests
./gradlew test

# Single module
./gradlew :services:ftgo-order-service:test

# With coverage report
./gradlew :services:ftgo-order-service:jacocoTestReport
# Report at: services/ftgo-order-service/build/reports/jacoco/test/html/index.html
```

---

## Pull Request Process

1. **Create a branch** from `feat/microservices-migration`.
2. **Make focused changes** — one concern per PR.
3. **Run checks locally** before pushing:
   ```bash
   ./gradlew spotlessApply
   ./gradlew check
   ```
4. **Open a PR** using the [PR template](.github/PULL_REQUEST_TEMPLATE.md).
5. **Complete the checklist** in the PR template.
6. **Address review feedback** promptly.
7. **Squash and merge** once approved.

### PR Size Guidelines

| Size          | Lines Changed | Expected Review Time |
|---------------|---------------|----------------------|
| Small         | < 100         | Same day             |
| Medium        | 100–400       | 1–2 days             |
| Large         | 400+          | Split if possible    |

---

## Code Review Guidelines

### For Authors

- Write a clear PR description explaining **what** and **why**.
- Self-review your diff before requesting review.
- Keep PRs small and focused.
- Respond to all review comments.
- Do not force-push after review has started.

### For Reviewers

Review each PR for:

1. **Correctness** — Does the code do what it claims?
2. **Design** — Is the approach appropriate? Does it follow DDD / CQRS patterns?
3. **Readability** — Is the code clear without excessive comments?
4. **Testing** — Are there adequate tests? Are edge cases covered?
5. **Security** — No secrets committed, input validation present, auth enforced.
6. **Performance** — No obvious N+1 queries, unnecessary allocations, or blocking calls.
7. **API contract** — Are REST/GraphQL contracts backward-compatible?

### Review Etiquette

- Be constructive and specific.
- Distinguish between blocking issues and suggestions (prefix with `nit:` or `suggestion:`).
- Approve once all blocking issues are resolved.
- Use GitHub's "Request Changes" for blocking issues, "Comment" for suggestions.

---

## SonarQube

Quality gates are enforced via SonarQube in CI:

- **Coverage** ≥ 70%
- **Duplicated lines** < 3%
- **Maintainability rating** ≥ A
- **Reliability rating** ≥ A
- **Security rating** ≥ A
- New code must not introduce **bugs**, **vulnerabilities**, or **code smells** above thresholds.

View the dashboard at: [SonarCloud](https://sonarcloud.io) (project key: `mbatchelor81_spring-boot-realworld-example-app`)
