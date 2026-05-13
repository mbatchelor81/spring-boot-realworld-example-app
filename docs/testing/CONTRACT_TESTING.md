# Contract Testing Approach

## Overview

Contract testing verifies that services can communicate correctly without requiring
full end-to-end deployments. It ensures that API producers honour their published
contracts and that consumers only rely on documented behaviour.

FTGO uses a **producer-driven** approach with **Spring Cloud Contract** as the primary
framework, with optional **Pact** support for consumer-driven scenarios.

---

## Why Contract Tests?

In a microservices architecture, services evolve independently. Without contract tests:

- A producer changes a response field name — consumers break at runtime
- A consumer starts relying on an undocumented field — producer removes it
- Breaking changes are only caught in expensive E2E or staging environments

Contract tests catch these issues **at build time** with fast, isolated verification.

---

## Terminology

| Term         | Definition                                                    |
|--------------|---------------------------------------------------------------|
| **Producer** | Service that exposes an API (e.g., Order Service REST API)    |
| **Consumer** | Service that calls another service's API                       |
| **Contract** | A formal specification of the request/response pair            |
| **Stub**     | Auto-generated mock server derived from contracts              |
| **Verifier** | Test that runs against the real producer to verify contracts   |

---

## Producer-Side: Spring Cloud Contract

### How It Works

1. Producer defines contracts in Groovy DSL or YAML under `src/test/resources/contracts/`
2. Spring Cloud Contract generates:
   - **Verifier tests** that run against the producer's real controllers
   - **Stub JARs** published to a shared artifact repository
3. Consumers use the stub JARs to test their HTTP clients

### Contract Definition (Groovy DSL)

```groovy
// src/test/resources/contracts/order/shouldReturnOrderById.groovy
Contract.make {
    description "should return order by ID"

    request {
        method GET()
        url "/orders/1"
        headers {
            contentType applicationJson()
            header 'Authorization': $(consumer(regex('Bearer .+')))
        }
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
            orderId: $(producer(regex('[0-9]+'))),
            state: "APPROVED",
            consumerId: $(producer(regex('[0-9]+'))),
            restaurantId: $(producer(regex('[0-9]+'))),
            orderLineItems: [
                [
                    menuItemId: $(producer(regex('.+'))),
                    name: $(producer(regex('.+'))),
                    price: $(producer(regex('[0-9]+\\.[0-9]{2}'))),
                    quantity: $(producer(regex('[1-9][0-9]*')))
                ]
            ]
        )
    }
}
```

### Contract Definition (YAML)

```yaml
# src/test/resources/contracts/order/shouldReturnOrderById.yaml
description: should return order by ID
request:
  method: GET
  url: /orders/1
  headers:
    Content-Type: application/json
    Authorization: "Bearer token"
response:
  status: 200
  headers:
    Content-Type: application/json;charset=UTF-8
  body:
    orderId: 1
    state: "APPROVED"
  matchers:
    body:
      - path: $.orderId
        type: by_regex
        value: "[0-9]+"
      - path: $.state
        type: by_regex
        value: "APPROVED|PENDING|CANCELLED"
```

### Producer Build Configuration

```groovy
// build.gradle (producer service)
plugins {
    id 'org.springframework.cloud.contract' version '4.1.1'
}

dependencies {
    testImplementation 'org.springframework.cloud:spring-cloud-starter-contract-verifier'
}

contracts {
    testFramework = TestFramework.JUNIT5
    baseClassForTests = 'com.ftgo.order.contract.OrderContractBase'
}
```

### Producer Base Test Class

```java
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
public abstract class OrderContractBase {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        when(orderService.findById(1L))
            .thenReturn(Optional.of(OrderFixtures.approvedOrder()));
    }
}
```

---

## Consumer-Side: Stub Runner

### How It Works

1. Consumer adds the producer's stub JAR as a test dependency
2. Spring Cloud Contract Stub Runner starts a WireMock server with the stubs
3. Consumer tests run against the stub server

### Consumer Test Example

```java
@SpringBootTest
@AutoConfigureStubRunner(
    ids = "com.ftgo:ftgo-order-service:+:stubs:8090",
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Tag("contract")
class OrderServiceClientContractTest {

    @Autowired
    private OrderServiceClient orderServiceClient;

    @Test
    void shouldFetchOrderById() {
        var order = orderServiceClient.getOrder(1L);

        assertThat(order).isNotNull();
        assertThat(order.getState()).isEqualTo("APPROVED");
    }
}
```

---

## Alternative: Pact (Consumer-Driven)

For scenarios where the consumer team owns the contract definition, Pact provides a
consumer-driven workflow.

### When to Use Pact

- External consumers (outside the FTGO monorepo) need contract guarantees
- Consumer team wants to define exactly which fields they depend on
- Gradual adoption alongside Spring Cloud Contract

### Consumer Pact Test

```java
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "OrderService", port = "8080")
@Tag("contract")
class OrderServicePactTest {

    @Pact(consumer = "ConsumerService")
    V4Pact orderExists(PactDslWithProvider builder) {
        return builder
            .given("order 1 exists")
            .uponReceiving("a request for order 1")
            .path("/orders/1")
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(new PactDslJsonBody()
                .integerType("orderId", 1)
                .stringType("state", "APPROVED"))
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "orderExists")
    void shouldGetOrder(MockServer mockServer) {
        var client = new OrderServiceClient(mockServer.getUrl());
        var order = client.getOrder(1L);
        assertThat(order.getState()).isEqualTo("APPROVED");
    }
}
```

---

## Contract Organisation

```
services/ftgo-order-service/
  src/test/resources/contracts/
    order/
      shouldReturnOrderById.groovy
      shouldCreateOrder.groovy
      shouldReturn404ForMissingOrder.groovy
    consumer/
      shouldValidateConsumerId.groovy
```

### Naming Conventions

- File names use camelCase starting with `should`: `shouldReturnOrderById.groovy`
- Organize by resource/entity in subdirectories
- Group related contracts together
- Include both happy-path and error scenarios

---

## CI Integration

| Stage        | What Runs                                | Outcome                        |
|--------------|------------------------------------------|--------------------------------|
| Producer PR  | Contract verifier tests                  | Fail if contracts are broken   |
| Producer merge | Publish stub JAR to artifact repo      | Stubs available to consumers   |
| Consumer PR  | Stub runner tests against latest stubs   | Fail if consumer expectations break |

### Backward Compatibility

- **Non-breaking changes:** Adding new fields to responses, adding new endpoints
- **Breaking changes:** Removing fields, renaming fields, changing types, removing endpoints
- Breaking changes require a versioned migration path:
  1. Add new field/endpoint alongside the old one
  2. Update consumers to use the new field/endpoint
  3. Remove the old field/endpoint after all consumers migrate

---

## Getting Started

1. Add `spring-cloud-starter-contract-verifier` to your producer service
2. Create a base test class for contract verification
3. Write contracts in `src/test/resources/contracts/`
4. Run `./gradlew contractTest` to generate and execute verifier tests
5. Publish stubs: `./gradlew publishStubs`
6. Consumer adds stub dependency and writes stub runner tests

See the [test templates](../../services/ftgo-test-lib/src/main/java/com/ftgo/testlib/)
for ready-to-use contract test base classes.
