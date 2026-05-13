# Entity-to-Service Ownership Mapping

This document defines which microservice owns each domain entity. The **owner** is
the single source of truth for that entity's lifecycle (create, update, delete).
Other services reference the entity via its ID or consume events/DTOs through the
corresponding API module.

## Ownership Table

| Entity | JPA Type | Owner Service | DB Table | Dependent Services |
|---|---|---|---|---|
| `Consumer` | `@Entity` | ftgo-consumer-service | `consumers` | ftgo-order-service (consumerId FK) |
| `Restaurant` | `@Entity` | ftgo-restaurant-service | `restaurants` | ftgo-order-service (restaurant FK) |
| `MenuItem` | `@Embeddable` | ftgo-restaurant-service | `restaurant_menu_items` | ftgo-order-service (via OrderLineItem) |
| `RestaurantMenu` | `@Embeddable` | ftgo-restaurant-service | — | ftgo-order-service (construction) |
| `Order` | `@Entity` | ftgo-order-service | `orders` | ftgo-courier-service (via assignedCourier) |
| `OrderLineItem` | `@Embeddable` | ftgo-order-service | `order_line_items` | — |
| `OrderLineItems` | `@Embeddable` | ftgo-order-service | — | — |
| `DeliveryInformation` | `@Embeddable` | ftgo-order-service | — | ftgo-courier-service |
| `PaymentInformation` | — | ftgo-order-service | — | — |
| `OrderRevision` | POJO | ftgo-order-service | — | — |
| `LineItemQuantityChange` | POJO | ftgo-order-service | — | — |
| `Courier` | `@Entity` | ftgo-courier-service | `courier` | ftgo-order-service (assignedCourier FK) |
| `Plan` | Embedded | ftgo-courier-service | — | — |
| `Action` | `@Embeddable` | ftgo-courier-service | `courier_actions` | — |

## Enums

| Enum | Used By |
|---|---|
| `OrderState` | ftgo-order-service, ftgo-order-service-api (events) |
| `ActionType` | ftgo-courier-service |

## Cross-Service Communication Contracts

Services communicate via the API modules (events and commands), **not** by sharing
JPA entities directly.

| API Module | Commands | Events |
|---|---|---|
| ftgo-consumer-service-api | `ValidateOrderByConsumer` | `ConsumerCreatedEvent` |
| ftgo-order-service-api | `CreateOrderCommand`, `OrderLineItemDto` | `OrderCreatedEvent`, `OrderStateChangedEvent` |
| ftgo-restaurant-service-api | `CreateRestaurantCommand`, `MenuItemDto` | `RestaurantCreatedEvent` |
| ftgo-courier-service-api | `ScheduleDeliveryCommand` | `CourierAvailableEvent` |

## Migration Notes

During the transition from monolith to microservices:

1. **Shared database phase** — All services share the monolith's database. Entities
   live in `ftgo-domain` and are imported by all services. This is the current state.
2. **Database-per-service phase** — Each service owns its tables. Cross-service
   references use IDs only. The `ftgo-domain` module will be decomposed into
   per-service domain modules (e.g., `ftgo-order-service` gets `Order`,
   `OrderLineItem`, etc.).
3. **Event-driven phase** — Services communicate exclusively via events defined in
   the API modules. Direct entity references across service boundaries are eliminated.
