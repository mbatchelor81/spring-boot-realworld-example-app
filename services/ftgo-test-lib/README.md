# ftgo-test-lib

Shared test utility library for FTGO microservices. Provides reusable test data builders,
fixtures, custom assertions, mock configurations, and Testcontainers helpers.

## Usage

Add as a test dependency in your service's `build.gradle`:

```groovy
dependencies {
    testImplementation project(':services:ftgo-test-lib')
}
```

## Modules

### Test Data Builders

Builder-pattern classes for constructing domain objects with sensible defaults:

```java
import com.ftgo.testlib.builders.*;

Consumer consumer = ConsumerBuilder.aConsumer()
    .withName("Jane", "Doe")
    .build();

Restaurant restaurant = RestaurantBuilder.aRestaurant()
    .withName("Ajanta")
    .withMenuItem("1", "Chicken Tikka Masala", new Money("12.99"))
    .build();

Order order = OrderBuilder.anOrder()
    .forConsumerId(1L)
    .withRestaurant(restaurant)
    .withLineItem("1", "Chicken Tikka Masala", new Money("12.99"), 2)
    .build();

MenuItem item = MenuItemBuilder.aMenuItem()
    .withId("tikka-1")
    .withName("Chicken Tikka Masala")
    .withPrice("12.99")
    .build();
```

### Test Fixtures

Pre-built objects for common test scenarios:

```java
import com.ftgo.testlib.fixtures.*;

Consumer john = ConsumerFixtures.johndoe();
Restaurant ajanta = RestaurantFixtures.ajantaRestaurant();
Order approved = OrderFixtures.approvedOrder();
OrderLineItem tikka = OrderFixtures.chickenTikkaMasala();
```

### Custom Assertions

Domain-specific AssertJ assertions:

```java
import static com.ftgo.testlib.assertions.FtgoAssertions.assertThat;

assertThat(order)
    .isApproved()
    .hasConsumerId(1L)
    .hasTotalGreaterThan(Money.ZERO)
    .hasLineItemCount(2);

assertThat(money)
    .isPositive()
    .isEqualTo("25.99")
    .isGreaterThanOrEqualTo(Money.ZERO);
```

### Mock Configurations

Spring test configurations for common scenarios:

```java
import com.ftgo.testlib.mocks.MockSecurityConfig;

@WebMvcTest(OrderController.class)
@Import(MockSecurityConfig.class)
class OrderControllerTest {
    // Security is disabled — test controller logic only
}
```

### Testcontainers Helpers

Pre-configured containers for integration tests:

```java
import com.ftgo.testlib.containers.FtgoContainers;

@Testcontainers
class OrderRepositoryIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = FtgoContainers.mysql();

    @DynamicPropertySource
    static void dbProperties(DynamicPropertyRegistry registry) {
        FtgoContainers.configureMysql(registry, mysql);
    }
}
```

## Dependencies

This library transitively provides:

| Library             | Version | Purpose                          |
|---------------------|---------|----------------------------------|
| JUnit 5             | 5.10.2  | Test framework                   |
| Mockito             | 5.11.0  | Mocking                         |
| AssertJ             | 3.25.3  | Fluent assertions                |
| REST Assured        | 4.5.1   | HTTP API testing                 |
| Testcontainers      | 1.19.7  | Containerized dependencies       |
| Spring Boot Test    | 2.6.3   | Spring integration testing       |
| Spring Security Test| 5.6.1   | Security test utilities          |
