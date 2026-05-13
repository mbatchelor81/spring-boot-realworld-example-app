# RBAC Authorization Model

## Overview

FTGO implements Role-Based Access Control (RBAC) using Spring Security method-level security annotations. Roles are carried in JWT claims and enforced via `@PreAuthorize` on service methods and controller endpoints.

## Roles

| Role | Constant | Description |
|------|----------|-------------|
| CUSTOMER | `ROLE_CUSTOMER` | End users who place orders and view resources |
| RESTAURANT_OWNER | `ROLE_RESTAURANT_OWNER` | Restaurant operators who manage menus and accept orders |
| COURIER | `ROLE_COURIER` | Delivery personnel who pick up and deliver orders |
| ADMIN | `ROLE_ADMIN` | System administrators with full access |

## Role Hierarchy

```
ADMIN
├── RESTAURANT_OWNER
│   └── CUSTOMER
└── COURIER
    └── CUSTOMER
```

- `ADMIN` inherits all permissions from `RESTAURANT_OWNER`, `COURIER`, and `CUSTOMER`
- `RESTAURANT_OWNER` inherits all `CUSTOMER` permissions
- `COURIER` inherits all `CUSTOMER` permissions
- `CUSTOMER` is the base role with read and order-creation permissions

Configured in `FtgoAuthorizationAutoConfiguration` using Spring's `RoleHierarchyImpl`.

## Permission Matrix

### Order Service (`/api/orders`)

| Operation | Endpoint | Required Role |
|-----------|----------|---------------|
| Create order | `POST /api/orders` | CUSTOMER |
| View order | `GET /api/orders/{id}` | CUSTOMER |
| Cancel order | `DELETE /api/orders/{id}` | CUSTOMER (+ owner when DB layer added) |
| Accept order | `PUT /api/orders/{id}/accept` | RESTAURANT_OWNER |
| Mark preparing | `PUT /api/orders/{id}/preparing` | RESTAURANT_OWNER |
| Mark ready | `PUT /api/orders/{id}/ready` | RESTAURANT_OWNER |
| Mark picked up | `PUT /api/orders/{id}/picked-up` | COURIER |
| Mark delivered | `PUT /api/orders/{id}/delivered` | COURIER |

### Consumer Service (`/api/consumers`)

| Operation | Endpoint | Required Role |
|-----------|----------|---------------|
| Create consumer | `POST /api/consumers` | ADMIN |
| View consumer | `GET /api/consumers/{id}` | CUSTOMER |
| List consumers | `GET /api/consumers` | ADMIN |

### Restaurant Service (`/api/restaurants`)

| Operation | Endpoint | Required Role |
|-----------|----------|---------------|
| Create restaurant | `POST /api/restaurants` | RESTAURANT_OWNER |
| View restaurant | `GET /api/restaurants/{id}` | CUSTOMER |
| Revise menu | `PUT /api/restaurants/{id}/menu` | RESTAURANT_OWNER |

### Courier Service (`/api/couriers`)

| Operation | Endpoint | Required Role |
|-----------|----------|---------------|
| Create courier | `POST /api/couriers` | ADMIN |
| View courier | `GET /api/couriers/{id}` | CUSTOMER |
| Plan delivery | `PUT /api/couriers/{id}/delivery` | COURIER |
| Update availability | `PUT /api/couriers/{id}/availability` | COURIER |

## Resource Ownership

The `ResourceOwnershipEvaluator` bean provides resource-level authorization:

- `isOwner(authentication, resourceOwnerId)` — checks if the authenticated user owns the resource
- `isOwnerOrAdmin(authentication, resourceOwnerId)` — owner check with admin override
- `isAdmin(authentication)` — checks for ADMIN role

Used in `@PreAuthorize` expressions:
```java
@PreAuthorize("hasRole('CUSTOMER') and @resourceOwnershipEvaluator.isOwnerOrAdmin(authentication, #ownerFromDb)")
public void cancelOrder(Long orderId, String ownerFromDb) { ... }
```

> **Note:** Ownership validation requires a database layer to look up the actual resource owner.
> Stub service methods currently use role-based checks only. Each method has a TODO
> comment documenting the pattern for adding ownership validation when the DB layer is implemented.

## JWT Claims

Roles are extracted from the `realm_access.roles` JWT claim (configurable via `ftgo.security.jwt.roles-claim`). The `JwtTokenProvider` includes roles in this nested claim structure when issuing tokens:

```json
{
  "sub": "user-123",
  "preferred_username": "john",
  "realm_access": {
    "roles": ["CUSTOMER"]
  }
}
```

## Configuration

The authorization framework auto-configures via `FtgoAuthorizationAutoConfiguration`, registered in `spring.factories`. It provides:

- `@EnableGlobalMethodSecurity(prePostEnabled = true)` — enables `@PreAuthorize`/`@PostAuthorize`
- `RoleHierarchy` bean — defines the role inheritance tree
- `FtgoPermissionEvaluator` — custom `PermissionEvaluator` for `hasPermission()` expressions
- `ResourceOwnershipEvaluator` — bean for resource ownership checks in SpEL
- `MethodSecurityExpressionHandler` — wires hierarchy and evaluator into method security

## Adding New Roles or Permissions

1. Add the role constant to `FtgoRole.java`
2. Add any new permission constants to `FtgoPermission.java`
3. Update the role hierarchy in `FtgoAuthorizationAutoConfiguration.roleHierarchy()`
4. Annotate service methods/controllers with `@PreAuthorize`
5. Add authorization tests to `RbacAuthorizationTest`
6. Update this document
