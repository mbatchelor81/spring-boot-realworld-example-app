# Testing Guidelines for New Services

## Overview

This guide provides step-by-step instructions for setting up tests in a new FTGO
microservice. Follow these conventions to ensure consistency across all services.

---

## Quick Start

### 1. Apply the Testing Convention Plugin

Every service should apply the `ftgo.testing-conventions` plugin in its `build.gradle`:

```groovy
plugins {
    id 'ftgo.java-conventions'
    id 'ftgo.testing-conventions'
    id 'ftgo.spring-boot-conventions'
}
```

This automatically provides:
- JUnit 5 platform configuration
- JaCoCo coverage with 70% line minimum
- Parallel test execution
- REST Assured and Mockito dependencies
- Structured test reporting for CI

### 2. Add ftgo-test-lib Dependency

```groovy
dependencies {
    testImplementation project(':services:ftgo-test-lib')
}
```

This gives you access to:
- Test data builders (`ConsumerBuilder`, `OrderBuilder`, etc.)
- Test fixtures with pre-built objects (`ConsumerFixtures`, `OrderFixtures`)
- Shared assertions (`FtgoAssertions`)
- Mock configurations (`MockSecurityConfig`)
- Testcontainers helpers (`FtgoContainers`)

### 3. Create the Test Directory Structure

```
src/test/java/com/ftgo/{servicename}/
├── domain/                    # Unit tests for domain entities/services
│   ├── {Entity}Test.java
│   └── {Service}Test.java
├── api/                       # Controller/API tests
│   ├── {Controller}Test.java
│   └── {Controller}IntegrationTest.java
├── repository/                # Repository integration tests
│   └── {Repository}IntegrationTest.java
├── contract/                  # Contract test base classes
│   └── {Service}ContractBase.java
└── fixtures/                  # Service-specific test fixtures (if needed)
    └── {Service}TestFixtures.java
```

---

## Writing Unit Tests

### Do

```java
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentGateway paymentGateway;
    @Mock private OrderRepository orderRepository;
    @InjectMocks private PaymentService paymentService;

    @Test
    void processPayment_withValidOrder_chargesCorrectAmount() {
        var order = OrderBuilder.anOrder()
            .withTotal(Money.of("25.99"))
            .build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentGateway.charge(any())).thenReturn(ChargeResult.success());

        var result = paymentService.processPayment(1L);

        assertThat(result.isSuccessful()).isTrue();
        verify(paymentGateway).charge(argThat(charge ->
            charge.getAmount().equals(Money.of("25.99"))
        ));
    }

    @Test
    void processPayment_withMissingOrder_throwsNotFoundException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(999L))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining("999");
    }
}
```

### Don't

```java
// BAD: Loading Spring context for a unit test
@SpringBootTest
class PaymentServiceTest {
    @Autowired private PaymentService paymentService;
    // ... slow, unnecessary Spring context loading
}

// BAD: Testing framework internals
@Test
void testSpringInjection() {
    assertNotNull(paymentService); // tests Spring, not your code
}

// BAD: Shared mutable state
static Order testOrder = new Order(); // shared across tests
```

---

## Writing Integration Tests

### Controller Tests with @WebMvcTest

```java
@WebMvcTest(OrderController.class)
@Import(MockSecurityConfig.class)
@Tag("integration")
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private OrderService orderService;

    @Test
    void getOrder_existingId_returns200() throws Exception {
        when(orderService.findById(1L))
            .thenReturn(Optional.of(OrderFixtures.approvedOrder()));

        mockMvc.perform(get("/orders/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("APPROVED"));
    }

    @Test
    void getOrder_missingId_returns404() throws Exception {
        when(orderService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/orders/999"))
            .andExpect(status().isNotFound());
    }
}
```

### Repository Tests with Testcontainers

```java
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Tag("integration")
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = FtgoContainers.mysql();

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry registry) {
        FtgoContainers.configureMysql(registry, mysql);
    }

    @Autowired private OrderRepository orderRepository;

    @Test
    void save_validOrder_persistsAndReturns() {
        var order = OrderBuilder.anOrder().build();

        var saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(orderRepository.findById(saved.getId())).isPresent();
    }
}
```

---

## Writing Contract Tests

See [CONTRACT_TESTING.md](./CONTRACT_TESTING.md) for the full approach.

### Producer Side (Your Service Exposes an API)

1. Create contracts in `src/test/resources/contracts/`
2. Create a base test class:

```java
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class OrderContractBase {
    @Autowired private MockMvc mockMvc;
    @MockBean private OrderService orderService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        // Set up stubs for contract scenarios
    }
}
```

3. Run: `./gradlew contractTest`

### Consumer Side (Your Service Calls Another API)

1. Add stub dependency
2. Write stub runner tests
3. Run: `./gradlew test --tests '*ContractTest'`

---

## Test Naming Conventions

| Pattern                              | When to Use           |
|--------------------------------------|-----------------------|
| `{ClassName}Test`                    | Unit tests            |
| `{ClassName}IntegrationTest`         | Integration tests     |
| `{Service}ContractBase`             | Contract base classes |
| `{Service}ContractTest`             | Consumer contract tests |
| `{Flow}E2ETest`                     | End-to-end tests      |

### Test Method Naming

```
methodName_condition_expectedResult
```

Examples:
- `createOrder_withValidRequest_returnsCreatedOrder`
- `createOrder_withNullConsumerId_throwsValidationException`
- `findById_nonExistentId_returnsEmpty`

---

## Assertions Best Practices

### Use AssertJ (not JUnit assertions)

```java
// Good — AssertJ
assertThat(order.getState()).isEqualTo(OrderState.APPROVED);
assertThat(orders).hasSize(3).extracting("state").containsOnly(OrderState.APPROVED);
assertThatThrownBy(() -> service.cancel(lockedOrder))
    .isInstanceOf(IllegalStateException.class);

// Bad — JUnit assertions
assertEquals(OrderState.APPROVED, order.getState());
assertTrue(orders.size() == 3);
```

### Use Custom Assertions from ftgo-test-lib

```java
// Domain-specific fluent assertions
FtgoAssertions.assertThat(order)
    .isInState(OrderState.APPROVED)
    .hasTotalGreaterThan(Money.ZERO);

FtgoAssertions.assertThat(money)
    .isPositive()
    .isEqualTo(Money.of("25.99"));
```

---

## Test Configuration

### Disable Security in Tests (When Not Testing Security)

```java
@Import(MockSecurityConfig.class) // from ftgo-test-lib
```

### Database Test Configuration

Use Testcontainers via `FtgoContainers` helper:

```java
@Container
static MySQLContainer<?> mysql = FtgoContainers.mysql();

@DynamicPropertySource
static void props(DynamicPropertyRegistry registry) {
    FtgoContainers.configureMysql(registry, mysql);
}
```

---

## Checklist for New Service Tests

- [ ] `ftgo.testing-conventions` plugin applied
- [ ] `ftgo-test-lib` added as test dependency
- [ ] Unit tests for all domain entities and services
- [ ] Integration tests for all REST endpoints
- [ ] Integration tests for repository layer with Testcontainers
- [ ] Contract base class if service exposes APIs consumed by others
- [ ] Tests tagged appropriately (`@Tag("integration")`, `@Tag("contract")`)
- [ ] JaCoCo coverage meets 70% threshold
- [ ] All tests pass in CI
- [ ] Test data uses builders, not raw constructors
