# JWT Authentication — FTGO Platform

## Overview

FTGO microservices use JWT (JSON Web Token) based authentication for stateless auth across all services. Each service independently validates tokens issued by Keycloak (the identity provider) without maintaining session state.

## Architecture

```
┌─────────┐     ┌──────────┐     ┌──────────────────┐
│  Client  │────▶│ Keycloak │────▶│  JWT Token       │
│          │     │ (IdP)    │     │  (signed RS256)   │
└─────────┘     └──────────┘     └────────┬─────────┘
                                          │
                    ┌─────────────────────┼─────────────────────┐
                    │                     │                     │
              ┌─────▼─────┐        ┌─────▼─────┐        ┌─────▼─────┐
              │ Consumer   │        │   Order    │        │Restaurant │
              │  Service   │        │  Service   │        │  Service  │
              │            │        │            │        │           │
              │ Validates  │        │ Validates  │        │ Validates │
              │ JWT via    │        │ JWT via    │        │ JWT via   │
              │ JWK Set    │        │ JWK Set    │        │ JWK Set   │
              └────────────┘        └────────────┘        └───────────┘
```

## Token Structure

JWT tokens contain the following claims:

| Claim | Description | Example |
|-------|-------------|---------|
| `sub` | User ID (unique identifier) | `f47ac10b-58cc-4372-a567-0e02b2c3d479` |
| `preferred_username` | Username | `consumer1` |
| `realm_access.roles` | User roles | `["ftgo-consumer", "ftgo-admin"]` |
| `iss` | Token issuer (Keycloak realm URL) | `http://localhost:9080/realms/ftgo` |
| `aud` | Audience | `ftgo-services` |
| `exp` | Expiration timestamp | `1700000000` |
| `iat` | Issued-at timestamp | `1699999700` |

## Configuration

### Properties

All JWT settings live under `ftgo.security.jwt.*`:

```yaml
ftgo:
  security:
    jwt:
      enabled: true
      issuer-uri: http://localhost:9080/realms/ftgo
      jwk-set-uri: http://localhost:9080/realms/ftgo/protocol/openid-connect/certs
      audiences:
        - ftgo-services
      username-claim: preferred_username   # default
      user-id-claim: sub                   # default
      roles-claim: realm_access.roles      # default
      role-prefix: "ROLE_"                 # default
```

### Environment Variables

For Docker deployments, use environment variables (Spring Boot relaxed binding):

```bash
FTGO_SECURITY_JWT_ENABLED=true
FTGO_SECURITY_JWT_ISSUER_URI=http://keycloak:8080/realms/ftgo
FTGO_SECURITY_JWT_JWK_SET_URI=http://keycloak:8080/realms/ftgo/protocol/openid-connect/certs
```

## Authentication Flow

1. **Client authenticates** with Keycloak using username/password (or other grant type)
2. **Keycloak issues JWT** signed with RS256 (RSA + SHA-256)
3. **Client sends JWT** in the `Authorization` header: `Authorization: Bearer <token>`
4. **Service validates token** by:
   - Fetching Keycloak's public key from the JWK Set URI
   - Verifying the RS256 signature
   - Checking token expiration
   - Validating the issuer matches the configured `issuer-uri`
   - Optionally validating the audience
5. **Service extracts user context** from validated token claims
6. **User context is available** via `SecurityContextHolder` and `FtgoUserContext`

## Token Refresh Flow

1. Client receives both an access token (short-lived, 5 min default) and refresh token
2. When the access token expires, the client sends the refresh token to Keycloak's token endpoint
3. Keycloak issues a new access token + refresh token pair
4. Services only validate access tokens — they never see refresh tokens

## User Context Propagation

Use `FtgoUserContext` to access the authenticated user's information in any service:

```java
import com.ftgo.security.jwt.FtgoUserContext;

// Get the authenticated user's ID
Optional<String> userId = FtgoUserContext.getUserId();

// Get the username
Optional<String> username = FtgoUserContext.getUsername();

// Get all roles
List<String> roles = FtgoUserContext.getRoles();

// Check for a specific role
boolean isAdmin = FtgoUserContext.hasRole("ftgo-admin");

// Get a custom claim
Optional<String> email = FtgoUserContext.getClaim("email");

// Check if request is authenticated
boolean authenticated = FtgoUserContext.isAuthenticated();
```

## Keycloak Setup (Local Development)

Keycloak runs in Docker as part of `docker-compose.yml`:

```bash
docker-compose up keycloak
```

- **Admin Console**: http://localhost:9080
- **Admin Credentials**: `admin` / `admin`
- **Realm**: `ftgo`

### Pre-configured Users

| Username | Password | Roles |
|----------|----------|-------|
| admin | admin | `ftgo-admin` |
| consumer1 | password | `ftgo-consumer` |
| restaurant1 | password | `ftgo-restaurant` |
| courier1 | password | `ftgo-courier` |

### Pre-configured Clients

| Client ID | Type | Description |
|-----------|------|-------------|
| `ftgo-services` | Confidential | Backend service-to-service client |
| `ftgo-web` | Public | Web frontend (SPA) client |

### Obtaining a Token

```bash
# Using Resource Owner Password Credentials grant (dev only)
curl -X POST http://localhost:9080/realms/ftgo/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=ftgo-services" \
  -d "client_secret=ftgo-services-secret" \
  -d "username=consumer1" \
  -d "password=password"

# The response includes:
# - access_token (JWT, use this in Authorization header)
# - refresh_token (use to get new access tokens)
# - expires_in (token lifetime in seconds)
```

### Calling a Protected Service

```bash
TOKEN="<access_token from above>"
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/consumers
```

## Security Considerations

- **Token signing keys are never hardcoded** — Keycloak manages RSA key pairs
- **Tokens are validated by each service independently** using the JWK Set endpoint
- **Token expiration is enforced** — expired tokens return HTTP 401
- **CORS is configured** per-service via `ftgo.security.cors.*`
- **All sessions are stateless** — no server-side session storage
- **Public endpoints** (health checks, OpenAPI docs) are accessible without authentication

## Error Responses

| HTTP Status | Scenario |
|-------------|----------|
| 401 Unauthorized | Missing token, expired token, invalid signature, or invalid issuer |
| 403 Forbidden | Valid token but insufficient roles for the requested resource |

## Library Components

| Class | Description |
|-------|-------------|
| `FtgoSecurityAutoConfiguration` | Configures SecurityFilterChain with OAuth2 Resource Server |
| `FtgoJwtAutoConfiguration` | Configures JwtDecoder and JwtAuthenticationConverter |
| `FtgoJwtAuthenticationConverter` | Extracts roles and user info from JWT claims |
| `FtgoUserContext` | Static utility for accessing current user context |
| `JwtTokenProvider` | Token creation/validation (used in tests and internal tooling) |
| `AudienceValidator` | Custom OAuth2 token validator for audience claim |
| `FtgoSecurityProperties` | Configuration properties for security and JWT |
