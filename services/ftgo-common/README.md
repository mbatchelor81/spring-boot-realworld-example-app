# ftgo-common

Shared library containing cross-cutting value objects, utilities, and exceptions used across all FTGO microservices.

## Version

`1.0.0`

## Package Structure

All classes reside under `com.ftgo.common`.

## Components

### Value Objects (JPA `@Embeddable`)

| Class | Description |
|-------|-------------|
| `Money` | `BigDecimal` wrapper with arithmetic (`add`, `multiply`), comparison (`isGreaterThanOrEqual`), and string conversion. Includes Jackson serialization support via `MoneyModule`. |
| `Address` | Five-field address: `street1`, `street2`, `city`, `state`, `zip`. |
| `PersonName` | Two-field name: `firstName`, `lastName`. |

### Jackson Serialization

| Class | Description |
|-------|-------------|
| `MoneyModule` | Jackson `SimpleModule` that serializes `Money` as a plain decimal string (e.g., `"12.34"`) and deserializes strings back to `Money`. |

### Spring Configuration

| Class | Description |
|-------|-------------|
| `CommonConfiguration` | Spring `@Configuration` that registers an `ObjectMapper` bean and the `CommonJsonMapperInitializer`. |
| `CommonJsonMapperInitializer` | Registers `MoneyModule` and `JavaTimeModule` on the shared `ObjectMapper`; disables `WRITE_DATES_AS_TIMESTAMPS`. |

### Exceptions

| Class | Description |
|-------|-------------|
| `UnsupportedStateTransitionException` | Thrown when a domain entity receives a command that is invalid for its current state. Accepts an `Enum` representing the current state. |
| `NotYetImplementedException` | Placeholder for unimplemented features. |

## Dependencies

- `javax.persistence-api` 2.2 — JPA annotations for `@Embeddable`
- `commons-lang` 2.6 — `EqualsBuilder`, `HashCodeBuilder`, `ToStringBuilder`
- `jackson-databind` 2.13.1 — JSON serialization
- `jackson-datatype-jsr310` 2.13.1 — Java 8 date/time support
- `spring-context` / `spring-boot-autoconfigure` — Spring configuration support

## Usage

Add the dependency in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':services:ftgo-common')
}
```

### Money Example

```java
import com.ftgo.common.Money;

Money price = new Money("19.99");
Money tax = new Money("1.60");
Money total = price.add(tax);          // 21.59
boolean ok = total.isGreaterThanOrEqual(Money.ZERO); // true
String s = total.asString();           // "21.59"
```

### Address Example

```java
import com.ftgo.common.Address;

Address addr = new Address("123 Main St", "Apt 4", "Springfield", "IL", "62701");
```

### Jackson Serialization

Register the module on your `ObjectMapper`:

```java
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new MoneyModule());
```

Or import `CommonConfiguration` to auto-configure the mapper with `MoneyModule` and `JavaTimeModule`.

## Building

```bash
./gradlew :services:ftgo-common:build
```

## Testing

```bash
./gradlew :services:ftgo-common:test
```

## Publishing

```bash
./gradlew :services:ftgo-common:publishMavenPublicationToLocalRepository
```

Artifacts are published to `build/repo/`.
