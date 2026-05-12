---
name: test-before-pr
description: >
  Standardized verification checklist to run before creating a pull request.
  Covers code formatting (Spotless), unit/integration tests (JUnit 5),
  JaCoCo coverage, Selenium E2E tests, and frontend build verification.
---

# Pre-PR Verification Skill

This skill defines the mandatory verification steps to execute **before**
creating a pull request in the Spring Boot RealWorld Example App.

## Prerequisites

- **Java 11** — `JAVA_HOME` must point to an OpenJDK 11 installation.
- **Node 16** — required for the Next.js frontend (`nvm use 16`).
- The Gradle wrapper (`./gradlew`) is committed to the repo; no global Gradle install is needed.

```bash
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export PATH="$JAVA_HOME/bin:$PATH"
```

---

## Phase 1 — Code Formatting (Spotless)

The project enforces **Google Java Format** via the Spotless Gradle plugin.
CI will fail if formatting is off.

```bash
# Auto-fix formatting issues
./gradlew spotlessJavaApply

# Verify — exits non-zero if anything is still wrong
./gradlew spotlessJavaCheck
```

After running `spotlessJavaApply`, check `git status` for modified files and
stage them before committing.

---

## Phase 2 — Unit & Integration Tests (JUnit 5)

The project contains ~68 tests across three layers:

| Layer | Base class / annotation | Scope |
|---|---|---|
| **Domain unit** | Plain JUnit 5 | Business logic, slug generation, entity behavior |
| **API controller** | `@WebMvcTest` + `TestWithCurrentUser` | REST endpoints via MockMvc + REST Assured, mocked services |
| **Repository / DB** | `DbTestBase` (`@MybatisTest`, `@ActiveProfiles("test")`) | MyBatis mappers against in-memory SQLite |
| **Application query** | `@MybatisTest` | CQRS read-side query services |

Run the full suite (excluding Selenium and JaCoCo coverage enforcement):

```bash
./gradlew clean test -x jacocoTestCoverageVerification
```

### What to check

1. **All tests pass** — zero failures, zero errors.
2. **No new test is skipped** — compare the skip count against `main`.
3. **Review the HTML report** at `build/reports/tests/test/index.html` for
   failures or unusually slow tests.

### Key conventions

- API tests extend `TestWithCurrentUser`, which mocks `UserRepository`,
  `UserReadService`, and `JwtService` with a default "johnjacob" user fixture.
- Repository tests extend `DbTestBase`, which activates the `test` profile
  (in-memory SQLite, Flyway runs only `V1__create_tables.sql`; seed data in
  `V2__seed_data.sql` is skipped).
- Test data is created via `TestHelper` factory methods — do not rely on
  seed data in tests.

---

## Phase 3 — Code Coverage (JaCoCo)

JaCoCo is configured with an **80 % minimum line-coverage** rule.

```bash
# Generate report + enforce threshold
./gradlew jacocoTestReport jacocoTestCoverageVerification
```

### What to check

1. **Coverage threshold met** — the task fails if overall line coverage < 80 %.
2. Review `build/reports/jacoco/test/html/index.html` to identify any
   newly-added code that is not covered.

> **Tip:** If you are only adding non-Java files (docs, config, skills), you
> can skip this phase because coverage will not change.

---

## Phase 4 — Selenium E2E Tests (optional, manual gate)

Selenium tests live under `io.spring.selenium` and use **TestNG** (not JUnit).
They are excluded from the default `test` task and run via a dedicated Gradle
task. These require a **running backend + frontend** so they are typically
run manually or in a dedicated CI stage.

### Start the application

```bash
# Terminal 1 — backend (port 8080, auto-seeds dev.db)
./gradlew bootRun &

# Terminal 2 — frontend (port 3000)
cd frontend && nvm use 16 && npm run dev &
```

### Run the Selenium smoke suite

```bash
./gradlew seleniumTest
```

The suite is defined in `src/test/resources/selenium/testng-smoke.xml`.
Reports land in `build/reports/selenium/ExtentReport.html`.

### When to run

- **Always** when changing API endpoints consumed by the frontend.
- **Always** when modifying Selenium page objects or test infrastructure.
- **Skip** for backend-only or documentation changes that do not affect the UI.

---

## Phase 5 — Frontend Build Verification

The Next.js frontend must compile without errors.

```bash
cd frontend
nvm use 16
npm run build
```

### When to run

- **Always** when touching files under `frontend/`.
- **Skip** when changes are backend-only.

---

## Quick-Reference Checklist

Copy this into your PR workflow and tick each box:

```
- [ ] `./gradlew spotlessJavaApply` — formatting fixed & changes staged
- [ ] `./gradlew spotlessJavaCheck` — formatting passes
- [ ] `./gradlew clean test -x jacocoTestCoverageVerification` — all tests green
- [ ] `./gradlew jacocoTestReport jacocoTestCoverageVerification` — coverage >= 80 %
- [ ] (if UI-affecting) `./gradlew seleniumTest` — E2E smoke passes
- [ ] (if frontend touched) `cd frontend && npm run build` — frontend compiles
- [ ] `git diff --stat` reviewed — no unintended files in the commit
```

---

## CI Reference

GitHub Actions (`.github/workflows/gradle.yml`) runs on every push and PR:

```
./gradlew clean test -x jacocoTestCoverageVerification
```

This covers Phases 1–2 automatically. Coverage enforcement (Phase 3) and
Selenium (Phase 4) are **not** part of the default CI pipeline and must be
verified locally when appropriate.
