# ftgo-common-jpa

Shared JPA utility library for all FTGO microservices.

## Contents

| Class | Description |
|---|---|
| `BaseEntity` | `@MappedSuperclass` providing auto-generated `Long id` with consistent `equals`/`hashCode` |
| `AuditableEntity` | Extends `BaseEntity` with `createdAt`/`updatedAt` audit timestamps via JPA lifecycle callbacks |
| `MoneyConverter` | JPA `AttributeConverter<Money, BigDecimal>` for persisting `Money` values as single columns |
| `JpaConfiguration` | Spring `@Configuration` that imports `CommonConfiguration` |

## Usage

Add a dependency on this module in your service's `build.gradle`:

```groovy
dependencies {
    implementation project(':services:ftgo-common-jpa')
}
```

Extend `BaseEntity` or `AuditableEntity` in your JPA entities:

```java
@Entity
public class MyEntity extends AuditableEntity {
    private String name;
    // ...
}
```

## Version

Current version: **1.0.0**
