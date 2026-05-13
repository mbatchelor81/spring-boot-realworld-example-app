# FTGO Testing Strategy

## Overview

This document defines the comprehensive testing strategy for the FTGO microservices platform.
It establishes a layered approach following the **Testing Pyramid**, ensuring fast feedback
loops, high confidence in deployments, and sustainable test maintenance as services evolve.

---

## Testing Pyramid

```
            ╱  E2E   ╲           ~5%  — Critical user journeys only
           ╱───────────╲
          ╱  Contract   ╲        ~10% — API compatibility between services
         ╱───────────────╲
        ╱  Integration    ╲      ~15% — Component boundaries, DB, messaging
       ╱───────────────────╲
      ╱    Unit Tests       ╲    ~70% — Business logic, domain rules
     ╱───────────────────────╲
```

| Level         | Coverage Target | Speed    | Scope                              |
|---------------|-----------------|----------|-------------------------------------|
| **Unit**      | 70% line min    | < 5 ms   | Single class/method in isolation    |
| **Integration** | Key paths     | < 5 s    | Service + DB, REST API endpoints    |
| **Contract**  | All public APIs | < 10 s   | Producer/consumer API compatibility |
| **E2E**       | Critical paths  | < 60 s   | Full order lifecycle                |

---

## Test Types

### 1. Unit Tests

**Purpose:** Validate business logic, domain rules, and data transformations in isolation.

**Framework:** JUnit 5 + Mockito + AssertJ

**Scope:**
- Domain entities (e.g., `Order`, `Restaurant`, `Consumer`)
- Value objects (e.g., `Money`, `Address`, `PersonName`)
- Service-layer business logic with mocked dependencies
- Input validation and edge cases
- State transitions and conditional behavior

**Conventions:**
- Located in `src/test/java` mirroring the main source tree
- Naming: `{ClassName}Test.java`
- Test methods: `methodName_condition_expectedResult`
- Use Arrange-Act-Assert pattern
- Use `@ExtendWith(MockitoExtension.class)` for mocked dependencies
- Prefer AssertJ fluent assertions (`assertThat(...)`)
- No Spring context loading — pure unit tests

**Example:**
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private OrderService orderService;

    @Test
    void createOrder_withValidDetails_returnsApprovedOrder() {
        // Arrange
        var request = OrderFixtures.validCreateRequest();
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        var result = orderService.createOrder(request);

        // Assert
        assertThat(result.getState()).isEqualTo(OrderState.APPROVED);
        verify(orderRepository).save(any(Order.class));
    }
}
```

### 2. Integration Tests

**Purpose:** Verify component interactions — REST endpoints, database operations, and
Spring wiring.

**Framework:** JUnit 5 + Spring Boot Test + Testcontainers + REST Assured

**Scope:**
- REST API request/response contracts (via `@WebMvcTest`)
- Repository layer with real database (via Testcontainers)
- Full service slice tests (via `@SpringBootTest`)
- Flyway migration verification
- Security filter chain and authentication enforcement

**Conventions:**
- Located in `src/test/java` alongside unit tests
- Naming: `{ClassName}IntegrationTest.java`
- Use `@Testcontainers` with `MySQLContainer` for database tests
- Use `@WebMvcTest` for controller-only tests (no full context)
- Use `@SpringBootTest` sparingly — only for full-slice verification
- Annotate with `@Tag("integration")` for selective execution

**Example:**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@Tag("integration")
class OrderApiIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = FtgoContainers.mysql();

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry registry) {
        FtgoContainers.configureMysql(registry, mysql);
    }

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void createOrder_returns201() {
        var request = OrderFixtures.validCreateRequest();
        var response = restTemplate.postForEntity("/orders", request, OrderResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
```

### 3. Contract Tests

**Purpose:** Ensure API compatibility between service producers and consumers
without requiring full E2E tests.

**Framework:** Spring Cloud Contract (producer-side) + Pact (consumer-side, optional)

**Scope:**
- REST API contracts between services
- Event/message contracts for async communication
- Backward compatibility verification on schema changes

**Approach:** See [CONTRACT_TESTING.md](./CONTRACT_TESTING.md) for detailed strategy.

### 4. End-to-End Tests

**Purpose:** Validate critical user journeys across the full system.

**Framework:** JUnit 5 + REST Assured + Testcontainers (Docker Compose)

**Scope:**
- Order lifecycle: create -> accept -> prepare -> pickup -> deliver
- Consumer registration and order placement
- Restaurant menu management and order acceptance
- Cross-service data consistency

**Conventions:**
- Located in a dedicated `ftgo-e2e-tests` module (future)
- Run against containerized services via Docker Compose
- Annotate with `@Tag("e2e")`
- Limit to 10-15 critical path scenarios
- Run on merge to main or nightly — not on every push

---

## Coverage Requirements

| Module Type          | Line Coverage | Branch Coverage | Enforcement      |
|----------------------|---------------|-----------------|------------------|
| Domain / Core        | 80%           | 70%             | JaCoCo + CI gate |
| Service layer        | 70%           | 60%             | JaCoCo + CI gate |
| API / Controller     | 60%           | 50%             | JaCoCo + CI gate |
| Shared libraries     | 70%           | 60%             | JaCoCo + CI gate |
| Infrastructure / Config | 40%       | N/A             | Advisory only    |

Coverage is enforced via the `ftgo.testing-conventions` Gradle plugin with a 70% minimum
line coverage threshold. Services may raise their thresholds above the baseline.

---

## Test Execution Strategy

### Local Development
```bash
# Run unit tests only (fast feedback)
./gradlew :services:ftgo-order-service:test

# Run integration tests (requires Docker)
./gradlew :services:ftgo-order-service:test --tests '*IntegrationTest'

# Run all tests for a service
./gradlew :services:ftgo-order-service:test
```

### CI Pipeline
| Stage          | Tests Run                  | Trigger             | Max Duration |
|----------------|----------------------------|----------------------|-------------|
| PR Check       | Unit + Integration         | Every push           | 5 min       |
| Merge Gate     | Unit + Integration + Contract | PR merge to main  | 10 min      |
| Nightly        | All (incl. E2E)            | Scheduled            | 30 min      |

### Selective Execution with Tags
```bash
# Unit tests only
./gradlew test -PincludeTags=unit

# Integration tests only
./gradlew test -PincludeTags=integration

# Contract tests only
./gradlew test -PincludeTags=contract
```

---

## Test Data Management

### Principles
1. **Isolation:** Each test creates its own data — no shared mutable state
2. **Builders:** Use `ftgo-test-lib` builder classes instead of raw constructors
3. **Minimal data:** Only include fields relevant to the specific test
4. **Descriptive names:** Variable names explain why data matters

### Test Data Builders (ftgo-test-lib)
```java
// Good — clear intent
var frozenConsumer = ConsumerBuilder.aConsumer()
    .withName("Jane", "Doe")
    .build();

var order = OrderBuilder.anOrder()
    .forConsumer(frozenConsumer)
    .withLineItems(OrderFixtures.chickenTikkaMasala())
    .build();

// Bad — opaque shared fixtures
var consumer = testData.getConsumer(0);
```

See [ftgo-test-lib README](../../services/ftgo-test-lib/README.md) for full API.

---

## Quality Gates

All PRs must pass these gates before merge:

1. **Compilation:** `./gradlew assemble` succeeds
2. **Unit Tests:** All tests pass, coverage >= 70%
3. **Static Analysis:** Checkstyle + SpotBugs clean (via `ftgo.quality-conventions`)
4. **Code Format:** Spotless check passes
5. **Integration Tests:** All `@Tag("integration")` tests pass (when applicable)
6. **Contract Tests:** No backward-incompatible API changes (when applicable)

---

## Tools & Libraries

| Tool              | Version  | Purpose                                |
|-------------------|----------|----------------------------------------|
| JUnit 5           | 5.10.2   | Test framework                         |
| Mockito           | 5.11.0   | Mocking framework                      |
| AssertJ           | 3.25.3   | Fluent assertions                      |
| REST Assured      | 4.5.1    | HTTP API testing                       |
| Testcontainers    | 1.19.7   | Containerized test dependencies        |
| Spring Boot Test  | 2.6.3    | Spring integration testing             |
| Spring Cloud Contract | 4.1.x | Contract testing (producer-side)      |
| JaCoCo            | 0.8.12   | Code coverage                          |

---

## Related Documents

- [Contract Testing Approach](./CONTRACT_TESTING.md)
- [Testing Guidelines for New Services](./TESTING_GUIDELINES.md)
- [Test Utility Library (ftgo-test-lib)](../../services/ftgo-test-lib/README.md)
