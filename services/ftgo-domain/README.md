# ftgo-domain

Shared domain model library containing all JPA entities and Spring Data repositories for the FTGO platform.

## Contents

### Entities

| Entity | Type | Table | Description |
|---|---|---|---|
| `Order` | `@Entity` | `orders` | Food delivery order with state machine lifecycle |
| `Consumer` | `@Entity` | `consumers` | Customer who places orders |
| `Restaurant` | `@Entity` | `restaurants` | Restaurant with menu items |
| `Courier` | `@Entity` | — | Delivery courier with plan and availability |
| `OrderLineItem` | `@Embeddable` | `order_line_items` | Single line item in an order |
| `OrderLineItems` | `@Embeddable` | — | Collection wrapper for order line items |
| `MenuItem` | `@Embeddable` | `restaurant_menu_items` | Menu item with price |
| `RestaurantMenu` | `@Embeddable` | — | Collection wrapper for menu items |
| `DeliveryInformation` | `@Embeddable` | — | Delivery address and time |
| `PaymentInformation` | — | — | Payment token holder |
| `Action` | `@Embeddable` | — | Pickup/dropoff action in a courier plan |
| `Plan` | — | — | Courier's delivery plan (list of actions) |

### Enums & Exceptions

| Type | Description |
|---|---|
| `OrderState` | Order lifecycle states: APPROVED → ACCEPTED → PREPARING → READY_FOR_PICKUP → PICKED_UP → DELIVERED / CANCELLED |
| `ActionType` | PICKUP, DROPOFF |
| `OrderMinimumNotMetException` | Thrown when revised order total is below minimum |

### Repositories

| Repository | Entity | Key Methods |
|---|---|---|
| `OrderRepository` | `Order` | `findAllByConsumerId(Long)` |
| `ConsumerRepository` | `Consumer` | CRUD |
| `RestaurantRepository` | `Restaurant` | CRUD |
| `CourierRepository` | `Courier` | `findAllAvailable()` |

### Configuration

`DomainConfiguration` — Spring `@Configuration` that enables JPA auto-configuration, entity scanning, and repository scanning for this module.

## Entity-to-Service Ownership Mapping

| Entity | Primary Owner Service | Dependent Services |
|---|---|---|
| `Consumer` | ftgo-consumer-service | ftgo-order-service (consumerId FK) |
| `Restaurant` | ftgo-restaurant-service | ftgo-order-service (restaurant FK) |
| `Order` | ftgo-order-service | ftgo-courier-service (via assignedCourier) |
| `Courier` | ftgo-courier-service | ftgo-order-service (assignedCourier FK) |
| `MenuItem` | ftgo-restaurant-service | ftgo-order-service (via OrderLineItem) |

## Usage

```groovy
dependencies {
    implementation project(':services:ftgo-domain')
}
```

## Version

Current version: **1.0.0**
