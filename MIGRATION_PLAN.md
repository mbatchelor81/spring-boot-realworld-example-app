# Java 11 to Java 21 Migration Plan

## Executive Summary

This document outlines the compatibility assessment and migration strategy for upgrading the Spring Boot RealWorld application from Java 11 to Java 21. This is a **major migration** that requires upgrading Spring Boot from 2.6.3 to 3.x, which introduces breaking changes including the transition from `javax.*` to `jakarta.*` namespace.

**Key Finding**: A direct migration to Java 21 is not possible with current dependencies. An intermediate upgrade to Java 17 with Spring Boot 3.x is required before moving to Java 21.

## Migration Strategy

The migration will follow a phased approach:

1. **Phase 1** (Current): Preparation & Compatibility Assessment ✓
2. **Phase 2**: Upgrade to Java 17 + Spring Boot 3.x + dependency updates
3. **Phase 3**: Migrate from `javax.*` to `jakarta.*` namespace
4. **Phase 4**: Replace Joda-Time with `java.time` API
5. **Phase 5**: Upgrade to Java 21 and validate
6. **Phase 6**: Testing and validation

## Dependency Compatibility Assessment

### Core Framework Dependencies

| Dependency | Current Version | Minimum Java 21 Compatible Version | Breaking Changes | Migration Notes |
|------------|----------------|-----------------------------------|------------------|-----------------|
| **Spring Boot** | 2.6.3 | 3.2.0+ | **CRITICAL**: `javax.*` → `jakarta.*` namespace change | Spring Boot 3.x requires Java 17 minimum. Must upgrade to 3.2.0 or later for Java 21 support. |
| **Spring Framework** | 5.3.x (via Spring Boot) | 6.1.0+ (via Spring Boot 3.2+) | `javax.*` → `jakarta.*`, API changes | Automatically upgraded with Spring Boot 3.x |
| **Netflix DGS** | 4.9.21 | 8.0.0+ | API changes, Spring Boot 3 compatibility | DGS 8.x+ required for Spring Boot 3.x. Significant API changes in GraphQL schema generation. |
| **MyBatis Spring Boot** | 2.2.2 | 3.0.3+ | Configuration changes, `javax.*` → `jakarta.*` | MyBatis 3.x required for Jakarta EE 9+ support |

### Data Access & Persistence

| Dependency | Current Version | Minimum Java 21 Compatible Version | Breaking Changes | Migration Notes |
|------------|----------------|-----------------------------------|------------------|-----------------|
| **Flyway** | (managed by Spring Boot) | 9.0.0+ | Configuration property changes | Flyway 9.x+ supports Java 21. Check for deprecated configuration properties. |
| **SQLite JDBC** | 3.36.0.3 | 3.43.0.0+ | None expected | Update to latest version (3.43+) for Java 21 compatibility and bug fixes. |

### Security & Authentication

| Dependency | Current Version | Minimum Java 21 Compatible Version | Breaking Changes | Migration Notes |
|------------|----------------|-----------------------------------|------------------|-----------------|
| **JJWT** | 0.11.2 | 0.11.5+ | None | JJWT 0.11.2+ supports Java 11-21. Recommend upgrading to 0.11.5 or 0.12.x for latest security fixes. |

### Date/Time Libraries

| Dependency | Current Version | Minimum Java 21 Compatible Version | Breaking Changes | Migration Notes |
|------------|----------------|-----------------------------------|------------------|-----------------|
| **Joda-Time** | 2.10.13 | **DEPRECATED** - Remove | **Complete replacement required** | Must migrate to `java.time` API (available since Java 8). Joda-Time is in maintenance mode and should not be used in new Java versions. |

### Build & Testing Tools

| Dependency | Current Version | Minimum Java 21 Compatible Version | Breaking Changes | Migration Notes |
|------------|----------------|-----------------------------------|------------------|-----------------|
| **Gradle** | (wrapper version TBD) | 8.5+ | None | Gradle 8.5+ required for Java 21 support |
| **Spotless** | 6.2.1 | 6.23.0+ | None | Update for Java 21 language feature support |
| **JaCoCo** | 0.8.7 | 0.8.11+ | None | Update for Java 21 bytecode support |
| **Lombok** | (managed by Spring Boot) | 1.18.30+ | None | Update for Java 21 compatibility |
| **Mockito** | 4.0.0 | 5.7.0+ | API improvements | Update for Java 21 support |

## Critical Areas Requiring Special Attention

### 1. JWT Token Generation (`DefaultJwtService.java`)

**Location**: `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java`

**Current State**: Uses JJWT 0.11.2 with `javax.crypto` imports

**Migration Impact**: 
- JJWT library itself is compatible with Java 21
- No `javax.*` to `jakarta.*` changes needed (uses `javax.crypto` which remains unchanged)
- Verify that JWT token generation and validation work correctly with Java 21
- Test token expiration logic thoroughly

**Action Items**:
- Update JJWT to 0.11.5 or 0.12.x for latest security patches
- Add comprehensive integration tests for token generation/validation
- Verify `SecretKeySpec` and `SignatureAlgorithm` behavior on Java 21

### 2. Database Migrations with Flyway

**Location**: `src/main/resources/db/migration/`

**Current State**: Using Flyway managed by Spring Boot 2.6.3

**Migration Impact**:
- Flyway 9.x+ changes configuration property names
- Migration scripts should remain compatible
- SQLite database compatibility needs verification

**Action Items**:
- Update Flyway configuration properties for version 9.x+
- Test all migration scripts with new Flyway version
- Verify SQLite JDBC driver compatibility with Java 21
- Ensure database connection pooling works correctly

### 3. GraphQL Schema Generation with Netflix DGS

**Location**: 
- `src/main/resources/schema/` (GraphQL schemas)
- `src/main/java/io/spring/graphql/` (Data fetchers and mutations)

**Current State**: Using Netflix DGS 4.9.21 (Spring Boot 2.x compatible)

**Migration Impact**: **HIGH RISK**
- DGS 8.x introduces significant API changes
- Code generation plugin configuration changes
- Data fetcher and mutation signatures may change
- GraphQL schema processing changes

**Action Items**:
- Update DGS codegen plugin to 8.x compatible version
- Review and update all `@DgsComponent`, `@DgsQuery`, `@DgsMutation` annotations
- Regenerate GraphQL types and verify compatibility
- Update data fetcher method signatures if required
- Comprehensive testing of all GraphQL queries and mutations

### 4. Security Configuration (`WebSecurityConfig.java`)

**Location**: `src/main/java/io/spring/api/security/WebSecurityConfig.java`

**Current State**: Using Spring Security 5.x with `javax.servlet` imports

**Migration Impact**: **CRITICAL**
- Spring Security 6.x (with Spring Boot 3.x) requires `jakarta.servlet` imports
- Security configuration API changes significantly
- `WebSecurityConfigurerAdapter` is deprecated and removed
- New component-based security configuration required

**Action Items**:
- Replace all `javax.servlet.*` imports with `jakarta.servlet.*`
- Refactor security configuration to use `SecurityFilterChain` bean approach
- Update CORS configuration for new API
- Update JWT filter to use Jakarta servlet API
- Verify authentication and authorization flows work correctly

### 5. MyBatis Type Handlers and Mappers

**Location**: 
- `src/main/java/io/spring/infrastructure/mybatis/DateTimeHandler.java`
- `src/main/resources/mapper/` (MyBatis XML mappers)

**Current State**: Using MyBatis 2.2.2 with Joda-Time DateTime type handler

**Migration Impact**: **HIGH RISK**
- MyBatis 3.x required for Jakarta EE support
- DateTimeHandler must be completely rewritten for `java.time` API
- All XML mappers using DateTime must be updated
- Type handler registration configuration changes

**Action Items**:
- Rewrite `DateTimeHandler` to use `java.time.Instant` or `java.time.ZonedDateTime`
- Update MyBatis configuration for new type handler
- Review all XML mappers for DateTime usage
- Test all database read/write operations thoroughly

## Joda-Time Migration to java.time API

### Overview

Joda-Time is used extensively throughout the application for date/time handling. It must be completely replaced with the `java.time` API (JSR-310) which has been part of Java since version 8.

### Files Using Joda-Time

#### Core Domain Entities (16 files total)

**Production Code (11 files)**:

1. **`src/main/java/io/spring/core/article/Article.java`**
   - Uses: `org.joda.time.DateTime` for `createdAt` and `updatedAt` fields
   - Instances: `new DateTime()` called in constructor and update methods
   - Migration: Replace with `java.time.Instant` or `java.time.ZonedDateTime`

2. **`src/main/java/io/spring/core/comment/Comment.java`**
   - Uses: `org.joda.time.DateTime` for `createdAt` field
   - Instances: `new DateTime()` called in constructor
   - Migration: Replace with `java.time.Instant`

3. **`src/main/java/io/spring/application/data/ArticleData.java`**
   - Uses: `org.joda.time.DateTime` for timestamp fields
   - Migration: Replace with `java.time.Instant`

4. **`src/main/java/io/spring/application/data/CommentData.java`**
   - Uses: `org.joda.time.DateTime` for timestamp fields
   - Migration: Replace with `java.time.Instant`

5. **`src/main/java/io/spring/application/ArticleQueryService.java`**
   - Uses: `org.joda.time.DateTime` in query methods
   - Migration: Update method signatures and implementations

6. **`src/main/java/io/spring/application/CommentQueryService.java`**
   - Uses: `org.joda.time.DateTime` in query methods
   - Migration: Update method signatures and implementations

7. **`src/main/java/io/spring/application/DateTimeCursor.java`**
   - Uses: `org.joda.time.DateTime` for cursor-based pagination
   - Migration: Replace with `java.time.Instant`, update cursor serialization

8. **`src/main/java/io/spring/infrastructure/mybatis/DateTimeHandler.java`** ⚠️ **CRITICAL**
   - Uses: Custom MyBatis TypeHandler for Joda-Time DateTime
   - Migration: Complete rewrite required for `java.time.Instant`
   - Impact: Affects all database read/write operations with timestamps

9. **`src/main/java/io/spring/infrastructure/mybatis/readservice/CommentReadService.java`**
   - Uses: `org.joda.time.DateTime` in query results
   - Migration: Update return types and result mappings

10. **`src/main/java/io/spring/JacksonCustomizations.java`** ⚠️ **CRITICAL**
    - Uses: Custom Jackson serializer for Joda-Time DateTime
    - Current: Serializes DateTime to ISO-8601 format with UTC timezone
    - Migration: Replace with `java.time` serializer or use Jackson's built-in support
    - Impact: Affects all JSON API responses with timestamps

11. **`src/main/java/io/spring/graphql/ArticleDatafetcher.java`**
    - Uses: `org.joda.time.DateTime` in GraphQL data fetchers
    - Migration: Update data fetcher return types

12. **`src/main/java/io/spring/graphql/CommentDatafetcher.java`**
    - Uses: `org.joda.time.DateTime` in GraphQL data fetchers
    - Migration: Update data fetcher return types

**Test Code (5 files)**:

13. **`src/test/java/io/spring/TestHelper.java`**
    - Uses: `org.joda.time.DateTime` for test data creation
    - Migration: Replace with `java.time.Instant`

14. **`src/test/java/io/spring/application/article/ArticleQueryServiceTest.java`**
    - Uses: `org.joda.time.DateTime` in test assertions
    - Migration: Update test data and assertions

15. **`src/test/java/io/spring/api/ArticleApiTest.java`**
    - Uses: `org.joda.time.DateTime` in API tests
    - Migration: Update test expectations

16. **`src/test/java/io/spring/api/ArticlesApiTest.java`**
    - Uses: `org.joda.time.DateTime` in API tests
    - Migration: Update test expectations

### Migration Strategy for Joda-Time

**Recommended Replacement**: `java.time.Instant`

**Rationale**:
- `Instant` represents a point in time in UTC (similar to Joda-Time DateTime with UTC)
- Simpler than `ZonedDateTime` for timestamp storage
- Direct database mapping support in modern frameworks
- ISO-8601 serialization support in Jackson

**Migration Steps**:

1. **Replace DateTime fields**:
   ```java
   // Before
   private DateTime createdAt;
   
   // After
   private Instant createdAt;
   ```

2. **Replace DateTime instantiation**:
   ```java
   // Before
   new DateTime()
   
   // After
   Instant.now()
   ```

3. **Update DateTimeHandler**:
   ```java
   // Before: DateTimeHandler implements TypeHandler<DateTime>
   // After: InstantTypeHandler implements TypeHandler<Instant>
   ```

4. **Update Jackson serialization**:
   - Remove custom DateTimeSerializer
   - Use Jackson's built-in `JavaTimeModule` for `java.time` support
   - Configure ISO-8601 format with UTC timezone

5. **Update all imports**:
   ```java
   // Remove
   import org.joda.time.DateTime;
   
   // Add
   import java.time.Instant;
   ```

### Testing Requirements for Joda-Time Migration

- Verify timestamp precision (milliseconds vs nanoseconds)
- Test timezone handling (ensure UTC consistency)
- Validate JSON serialization format matches existing API contracts
- Test database read/write operations
- Verify cursor-based pagination still works correctly
- Test GraphQL timestamp fields
- Ensure backward compatibility with existing data in database

## Recommended Migration Path

### Step 1: Upgrade to Java 17 + Spring Boot 3.2

1. Update `build.gradle`:
   - Set `sourceCompatibility = '17'` and `targetCompatibility = '17'`
   - Update Spring Boot to 3.2.x
   - Update all dependencies to Spring Boot 3.x compatible versions

2. Migrate `javax.*` to `jakarta.*`:
   - Update all servlet imports
   - Update JPA/persistence imports (if any)
   - Update validation imports
   - Update security imports

3. Update Netflix DGS to 8.x:
   - Update DGS dependencies
   - Update code generation configuration
   - Refactor data fetchers and mutations for new API

4. Update MyBatis to 3.x:
   - Update MyBatis Spring Boot Starter
   - Update configuration for Jakarta EE

5. Update security configuration:
   - Refactor `WebSecurityConfig` for Spring Security 6.x
   - Update JWT filter for Jakarta servlet API

### Step 2: Replace Joda-Time with java.time

1. Create new `InstantTypeHandler` for MyBatis
2. Update all domain entities to use `Instant`
3. Update Jackson configuration for `java.time` serialization
4. Update all service and repository methods
5. Update all tests
6. Remove Joda-Time dependency

### Step 3: Upgrade to Java 21

1. Update `build.gradle`:
   - Set `sourceCompatibility = '21'` and `targetCompatibility = '21'`

2. Update Gradle wrapper to 8.5+

3. Update build tools:
   - Spotless to 6.23.0+
   - JaCoCo to 0.8.11+

4. Verify all tests pass

5. Test application thoroughly

### Step 4: Validation & Testing

1. Run all unit tests
2. Run all integration tests
3. Test GraphQL queries and mutations
4. Test REST API endpoints
5. Verify JWT authentication/authorization
6. Test database migrations
7. Verify JSON serialization format
8. Performance testing

## Potential Risks & Mitigation

### Risk 1: Breaking API Changes in Netflix DGS 8.x
**Impact**: High - GraphQL functionality may break
**Mitigation**: 
- Review DGS 8.x migration guide thoroughly
- Create comprehensive GraphQL integration tests before migration
- Test all queries and mutations after upgrade

### Risk 2: Spring Security Configuration Changes
**Impact**: High - Authentication/authorization may break
**Mitigation**:
- Review Spring Security 6.x migration guide
- Test all security scenarios (login, JWT validation, authorization)
- Maintain backward compatibility for JWT tokens

### Risk 3: Joda-Time to java.time Migration
**Impact**: Medium - Timestamp handling may change
**Mitigation**:
- Verify timestamp precision and timezone handling
- Ensure JSON serialization format remains consistent
- Test with existing database data

### Risk 4: Database Migration Issues
**Impact**: Medium - Data access may fail
**Mitigation**:
- Test Flyway migrations with new version
- Verify SQLite JDBC driver compatibility
- Test all MyBatis mappers thoroughly

### Risk 5: Build Tool Compatibility
**Impact**: Low - Build may fail
**Mitigation**:
- Update Gradle wrapper first
- Update all build plugins
- Test build process in CI/CD pipeline

## GitHub Actions Workflow Updates

**Current**: `.github/workflows/gradle.yml` uses Java 11

**Required Changes**:
1. Update Java version in workflow to match target (17, then 21)
2. Update Gradle version if needed
3. Verify all CI steps pass with new Java version

## Estimated Effort

- **Phase 2** (Java 17 + Spring Boot 3.x): 3-5 days
- **Phase 3** (`javax.*` to `jakarta.*`): 1-2 days
- **Phase 4** (Joda-Time migration): 2-3 days
- **Phase 5** (Java 21 upgrade): 1 day
- **Phase 6** (Testing & validation): 2-3 days

**Total Estimated Effort**: 9-14 days

## Next Steps

1. ✅ **Phase 1 Complete**: Compatibility assessment documented
2. **Phase 2**: Create feature branch and upgrade to Java 17 + Spring Boot 3.2
3. **Phase 3**: Migrate `javax.*` to `jakarta.*` namespace
4. **Phase 4**: Replace Joda-Time with `java.time` API
5. **Phase 5**: Upgrade to Java 21
6. **Phase 6**: Comprehensive testing and validation

## References

- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Security 6.0 Migration Guide](https://docs.spring.io/spring-security/reference/migration/index.html)
- [Netflix DGS Framework Documentation](https://netflix.github.io/dgs/)
- [MyBatis Spring Boot 3.0 Migration](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [Joda-Time to java.time Migration Guide](https://blog.joda.org/2014/11/converting-from-joda-time-to-javatime.html)
- [Java 21 Release Notes](https://openjdk.org/projects/jdk/21/)
