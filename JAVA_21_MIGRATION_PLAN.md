# Java 21 Migration Plan - Spring Boot RealWorld Example App

**Ticket ID:** JP-9  
**Document Version:** 1.0  
**Date:** October 28, 2025  
**Author:** Devin AI  
**Requested by:** Mason Batchelor (@mbatchelor81)

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [Current State Inventory](#current-state-inventory)
3. [Java 21 Compatibility Matrix](#java-21-compatibility-matrix)
4. [Breaking Changes Analysis](#breaking-changes-analysis)
5. [Recommended Upgrade Path](#recommended-upgrade-path)
6. [Risk Assessment](#risk-assessment)
7. [Timeline Estimates](#timeline-estimates)
8. [Open Questions Addressed](#open-questions-addressed)

---

## Executive Summary

### Overview

This document provides a comprehensive compatibility assessment for migrating the Spring Boot RealWorld Example Application from **Java 11 to Java 21**. The migration involves significant framework upgrades across all four primary dependencies and requires careful planning due to the application's complex architecture (CQRS pattern, dual REST/GraphQL APIs, and distributed MyBatis mappers).

### Key Findings

- **Java 21 is achievable** but requires a major framework migration through Spring Boot 3.x
- **All four primary dependencies** have Java 21-compatible versions available
- **The Jakarta EE namespace migration** (javax.* → jakarta.*) is the most significant breaking change
- **Recommended approach:** Incremental migration via Java 17 LTS as a stepping stone
- **Estimated effort:** 4-6 weeks for complete migration with thorough testing

### Critical Dependencies Status

| Dependency | Current | Target | Compatibility | Risk Level |
|------------|---------|--------|---------------|------------|
| Spring Boot | 2.6.3 | 3.2+ | ✅ Compatible | 🔴 High (Major Version) |
| MyBatis | 2.2.2 (EOL) | 3.0+ | ✅ Compatible | 🟡 Medium |
| Netflix DGS | 4.9.21 | 10.x+ | ✅ Compatible | 🟡 Medium |
| JJWT | 0.11.2 | 0.13.0+ | ✅ Compatible | 🟢 Low |

### Recommendation

**Proceed with incremental migration strategy:**
1. Upgrade to Java 17 LTS first
2. Migrate to Spring Boot 3.x with Jakarta EE namespace changes
3. Upgrade remaining dependencies to Spring Boot 3-compatible versions
4. Finally upgrade to Java 21

This approach minimizes risk and allows for thorough testing at each stage.

---

## Current State Inventory

### Java Configuration

**Current Java Version:** Java 11

**Configuration Locations:**
- `build.gradle` (lines 11-12): `sourceCompatibility = '11'` and `targetCompatibility = '11'`
- `.github/workflows/gradle.yml` (lines 20-24): GitHub Actions CI/CD using Java 11

### Dependency Versions

#### Primary Dependencies

**Spring Boot Framework**
- Current Version: 2.6.3
- Configuration: `build.gradle` line 2
- Status: Stable but dated (released January 2022)
- Support Status: OSS support ended (Spring Boot 2.x EOL was December 2023)

**MyBatis Spring Boot Starter**
- Current Version: 2.2.2
- Configuration: `build.gradle` line 63
- Status: End of Life (EOL)
- Support Status: No longer maintained, replaced by 3.x series

**Netflix DGS (GraphQL Framework)**
- Current Version: 4.9.21
- Configuration: `build.gradle` line 64
- Status: Outdated (version 5.x was last to support Spring Boot 2)
- Support Status: No longer compatible with Spring Boot 3+

**JJWT (Java JWT Library)**
- Current Versions: 0.11.2 across api, impl, and jackson modules
- Configuration: `build.gradle` lines 66-68
- Status: Outdated but functional
- Latest Version: 0.13.0 (released August 20, 2025)

#### Supporting Dependencies

- **DGS Codegen Plugin:** 5.0.6 (`build.gradle` line 5)
- **SQLite JDBC:** 3.36.0.3 (`build.gradle` line 70)
- **Flyway:** Core migration tool (`build.gradle` line 65)
- **Lombok:** Used for annotations (`build.gradle` lines 72-73)

### Architecture Overview

The application implements a sophisticated architecture that will be impacted by the migration:

**Design Patterns:**
- **CQRS (Command Query Responsibility Segregation):** Separate read and write operations
- **Domain-Driven Design:** Clean separation between domain, application, and infrastructure layers
- **Dual API Surface:** Both REST and GraphQL endpoints for the same domain

**Key Components:**
- REST Controllers in `/api` package
- GraphQL Resolvers in `/graphql` package (using Netflix DGS)
- MyBatis distributed mappers across the codebase
- Spring Security with JWT authentication
- Custom security filters and exception handlers

---

## Java 21 Compatibility Matrix

### Dependency Compatibility Analysis

#### Spring Boot

| Version | Java Support | Release Date | Key Features | Notes |
|---------|-------------|--------------|--------------|-------|
| 2.6.3 (current) | Java 8-17 | Jan 2022 | Stable Spring Boot 2.x | OSS support ended |
| 2.7.x (last 2.x) | Java 8-17 | May 2022 | Final 2.x series | Extended support until Nov 2025 |
| 3.0.0 | Java 17+ (tested with 19) | Nov 2022 | Jakarta EE migration | First Java 17 baseline |
| 3.1.x | Java 17+ | May 2023 | Spring Framework 6.1 | Production-ready for Java 17 |
| 3.2.x | Java 17-21 | Nov 2023 | Virtual Threads support | **First official Java 21 support** |
| 3.3.x+ | Java 17-21+ | May 2024+ | Latest features | Current stable version |

**Recommended Target:** Spring Boot 3.2.x or later for Java 21 compatibility

**Key Breaking Changes:**
- **Baseline:** Java 17 minimum (Java 11 no longer supported)
- **Jakarta EE Migration:** All `javax.*` packages renamed to `jakarta.*`
- **Spring Framework 6.0:** Required dependency upgrade
- **Spring Security 6.0:** Major API changes
- **Removed/Deprecated APIs:** Various Spring Boot 2.x APIs removed

**Documentation:**
- [Spring Boot 3.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes)
- [Spring Boot 3.2 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)

#### MyBatis Spring Boot Starter

| Version | Java Support | Spring Boot Support | Status | Notes |
|---------|-------------|---------------------|--------|-------|
| 2.2.2 (current) | Java 8+ | 2.5-2.7 | EOL | Current version |
| 2.3.x | Java 8+ | 2.7 | Maintenance | Last 2.x series |
| 3.0.x | Java 17+ | 3.0-3.4 | **Active** | **Java 21 compatible** |

**Recommended Target:** MyBatis Spring Boot Starter 3.0.3 or later

**Key Breaking Changes:**
- **Baseline:** Java 17 minimum
- **Spring Boot Dependency:** Requires Spring Boot 3.0+
- **Jakarta EE:** Namespace migration required for annotations
- **MyBatis Core:** Upgraded to MyBatis 3.5.x series
- **API Compatibility:** Mostly backward compatible at the MyBatis level

**Documentation:**
- [MyBatis Spring Boot Starter Documentation](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)

#### Netflix DGS (GraphQL Framework)

| Version | Java Support | Spring Boot Support | Status | Notes |
|---------|-------------|---------------------|--------|-------|
| 4.9.21 (current) | Java 8+ | 2.x | Unsupported | Current version |
| 5.x | Java 8+ | 2.x | Unsupported | Last Spring Boot 2 support |
| 6.x | Java 17+ | 3.x | Maintenance | First Spring Boot 3 support |
| 7.x-9.x | Java 17+ | 3.x | Active | Progressive improvements |
| 10.x+ | Java 17+ | 3.x | **Current** | **Latest stable version** |

**Recommended Target:** Netflix DGS 10.x or later (latest stable)

**Key Breaking Changes:**
- **Baseline:** Java 17 minimum and Spring Boot 3.0+
- **GraphQL Java:** Upgraded to graphql-java 21.x+
- **Code Generation:** DGS Codegen plugin requires update to 6.x+
- **API Changes:** Some annotation and API changes between 4.x and 6.x+
- **Schema Support:** Improved GraphQL schema federation support

**Documentation:**
- [Netflix DGS Framework Documentation](https://netflix.github.io/dgs/)
- [DGS Migration Guide](https://netflix.github.io/dgs/migration/)

#### JJWT (Java JWT Library)

| Version | Java Support | Release Date | Status | Notes |
|---------|-------------|--------------|--------|-------|
| 0.11.2 (current) | Java 7+ | 2020 | Stable | Current version |
| 0.12.x | Java 7+ | 2023 | Stable | Incremental improvements |
| 0.13.0 | Java 7+ | Aug 2025 | **Latest** | Last Java 7 support |
| 0.14.0+ | Java 8+ | Future | Planned | Future release |

**Recommended Target:** JJWT 0.13.0 (latest stable)

**Key Breaking Changes:**
- **Minimal:** JJWT has excellent backward compatibility
- **Java 21 Compatibility:** Fully compatible (supports Java 7+)
- **API Stability:** No major breaking changes between 0.11.x and 0.13.0
- **Dependencies:** May require minor adjustments to Jackson dependencies

**Documentation:**
- [JJWT GitHub Repository](https://github.com/jwtk/jjwt)
- [JJWT Release Notes](https://github.com/jwtk/jjwt/releases)

### Additional Dependencies Impact

#### SQLite JDBC Driver
- Current: 3.36.0.3 (2021)
- Target: 3.45+ (latest stable)
- Java 21 Compatibility: ✅ Compatible
- Notes: Regular updates recommended for security and performance

#### Flyway
- Current: Managed by Spring Boot 2.6.3 (Flyway 8.x)
- Target: Managed by Spring Boot 3.x (Flyway 9.x+)
- Java 21 Compatibility: ✅ Compatible
- Notes: Minor API changes in Flyway 9+, mostly transparent with Spring Boot

#### Lombok
- Current: Managed by Spring Boot 2.6.3
- Target: Latest stable (1.18.30+)
- Java 21 Compatibility: ✅ Compatible (Lombok 1.18.28+ supports Java 21)
- Notes: May need explicit version specification for Java 21 features

---

## Breaking Changes Analysis

### 1. Spring Boot 2.6.3 → 3.2+ Migration

#### Jakarta EE Namespace Migration

**Impact: 🔴 CRITICAL - Affects entire codebase**

The most significant breaking change is the migration from Java EE (`javax.*`) to Jakarta EE (`jakarta.*`) namespaces.

**Affected Packages:**
```java
// Old (Java EE)              →  // New (Jakarta EE)
javax.servlet.*              →  jakarta.servlet.*
javax.persistence.*          →  jakarta.persistence.*
javax.validation.*           →  jakarta.validation.*
javax.annotation.*           →  jakarta.annotation.*
javax.transaction.*          →  jakarta.transaction.*
```

**Code Locations Requiring Updates:**

1. **Security Layer** (`/infrastructure/security/`):
   - `WebSecurityConfig.java` - Security configuration
   - `JwtTokenFilter.java` - JWT authentication filter
   - Custom security annotations and filters

2. **API Layer** (`/api/`):
   - All REST controllers using `@RestController`, `@RequestMapping`
   - Request/Response handling with `javax.servlet.http.*`
   - Exception handlers using servlet APIs

3. **Validation** (throughout codebase):
   - `@Valid`, `@NotNull`, `@Email` annotations
   - Custom validators
   - Bean Validation constraints

4. **Dependency Injection**:
   - `@Inject` → `jakarta.inject.@Inject` (if used)
   - Component scanning annotations

**Estimated Impact:**
- ~50-100 files requiring namespace changes
- Automated tooling can handle most of the migration
- Manual review required for complex security configurations

**Migration Tools:**
- Spring Boot Migrator: Automated namespace rewriting
- OpenRewrite recipes: Automated refactoring
- IDE find-replace with careful review

#### Spring Security 6.0 API Changes

**Impact: 🟡 MEDIUM - Requires security configuration updates**

Spring Security 6.0 (required by Spring Boot 3.0) includes significant API changes:

**Configuration Changes:**
```java
// Old (Spring Security 5.x)
http.authorizeRequests()
    .antMatchers("/api/users/login", "/api/users").permitAll()
    .anyRequest().authenticated();

// New (Spring Security 6.0)
http.authorizeHttpRequests()
    .requestMatchers("/api/users/login", "/api/users").permitAll()
    .anyRequest().authenticated();
```

**Affected Files:**
- `WebSecurityConfig.java` - Security configuration
- `JwtTokenFilter.java` - May require updates for filter chain
- Custom authentication/authorization logic

**Key Changes:**
- `authorizeRequests()` → `authorizeHttpRequests()`
- `antMatchers()` → `requestMatchers()`
- `WebSecurityConfigurerAdapter` removed (use SecurityFilterChain beans)
- CSRF configuration changes

#### Spring Framework 6.0 Changes

**Impact: 🟡 MEDIUM - Affects dependency injection and AOP**

**Key Changes:**
- Baseline servlet API upgraded (Servlet 6.0 / Jakarta Servlet)
- AOT (Ahead-of-Time) compilation support
- Observability improvements (Micrometer integration)
- Method parameter name reflection changes

**Potential Impacts:**
- Custom AOP aspects may need review
- Reflection-based code may need adjustments
- Native image compilation considerations

### 2. MyBatis 2.2.2 → 3.0+ Migration

**Impact: 🟢 LOW-MEDIUM - Mostly transparent upgrade**

MyBatis Spring Boot Starter 3.0 is designed for compatibility, but some areas require attention:

**Key Changes:**
1. **Jakarta EE Annotations:**
   - `@Mapper` annotations (remain unchanged)
   - Any `javax.annotation.*` imports → `jakarta.annotation.*`

2. **Configuration:**
   - `application.properties` MyBatis configuration mostly unchanged
   - Type handler registration may need minor updates

3. **XML Mappers:**
   - No changes required to `.xml` mapper files
   - SQL queries remain unchanged

**Affected Areas:**
- MyBatis repository implementations: `MyBatisArticleRepository.java`, etc.
- XML mappers: `ArticleReadService.xml`, `CommentReadService.xml`, etc.
- Type handlers: `DateTimeHandler.java` (Joda-Time support)

**Verification Required:**
- Test all database operations thoroughly
- Verify transaction management with Spring Boot 3.x
- Check MyBatis-Spring integration points

### 3. Netflix DGS 4.9.21 → 10.x Migration

**Impact: 🟡 MEDIUM - Requires GraphQL code updates**

**Key Changes:**

1. **Annotation Updates:**
```java
// Most annotations remain the same, but some have changed
@DgsComponent  // Unchanged
@DgsQuery      // Unchanged
@DgsData       // Check for deprecations
```

2. **GraphQL Java Library:**
   - Upgraded from graphql-java 17.x to 21.x+
   - Schema definition language (SDL) improvements
   - DataLoader API changes

3. **Code Generation Plugin:**
   - DGS Codegen plugin: 5.0.6 → 6.x+
   - Generated code structure may differ
   - Client code generation may need adjustments

4. **Federation Support:**
   - Enhanced GraphQL Federation 2.0 support
   - May affect federated schema definitions

**Affected Files:**
- All GraphQL resolvers in `/graphql/` package:
  - `ArticleDataFetcher.java`
  - `UserMutation.java`
  - `ProfileDataFetcher.java`
  - Custom data loaders
- GraphQL schema files (`schema.graphqls`)
- Code generation configuration in `build.gradle`

**Testing Requirements:**
- Comprehensive GraphQL query testing
- Mutation operation verification
- DataLoader performance testing
- Schema validation

### 4. JJWT 0.11.2 → 0.13.0 Migration

**Impact: 🟢 LOW - Minimal breaking changes**

JJWT maintains excellent backward compatibility between versions.

**Key Changes:**
1. **API Compatibility:**
   - Core JWT generation/parsing API unchanged
   - Builder patterns remain consistent
   - Backward compatible changes only

2. **Dependencies:**
   - Jackson dependencies may need version alignment
   - Security provider updates

3. **Performance:**
   - Minor performance improvements
   - Enhanced security validations

**Affected Files:**
- `DefaultJwtService.java` - JWT generation and validation
- Security configuration referencing JWT

**Testing Requirements:**
- Token generation verification
- Token validation testing
- Expiration and claims testing

### 5. Build Configuration Changes

**Impact: 🟡 MEDIUM - Requires careful Gradle updates**

**build.gradle Changes Required:**

```gradle
// Java Version
sourceCompatibility = '21'  // Was '11'
targetCompatibility = '21'  // Was '11'

// Spring Boot
id 'org.springframework.boot' version '3.2.x'  // Was 2.6.3
id 'io.spring.dependency-management' version '1.1.x'  // Update needed

// DGS Codegen
id 'com.netflix.dgs.codegen' version '6.x'  // Was 5.0.6

// Dependencies
implementation 'org.springframework.boot:spring-boot-starter-web'  // Auto-managed by Spring Boot 3.2.x
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'  // Was 2.2.2
implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:10.x'  // Was 4.9.21
implementation 'io.jsonwebtoken:jjwt-api:0.13.0'  // Was 0.11.2
implementation 'io.jsonwebtoken:jjwt-impl:0.13.0'  // Was 0.11.2
implementation 'io.jsonwebtoken:jjwt-jackson:0.13.0'  // Was 0.11.2

// SQLite (consider updating)
implementation 'org.xerial:sqlite-jdbc:3.45.x'  // Was 3.36.0.3

// Lombok (may need explicit version for Java 21)
compileOnly 'org.projectlombok:lombok:1.18.30'  // Ensure Java 21 support
annotationProcessor 'org.projectlombok:lombok:1.18.30'
```

**Plugin Compatibility:**
- Gradle wrapper: Upgrade to 8.5+ (for Java 21 support)
- Spotless plugin: Verify Java 21 compatibility
- JaCoCo plugin: Upgrade to 0.8.11+ for Java 21

### 6. CI/CD Configuration Changes

**Impact: 🟢 LOW - Straightforward updates**

**.github/workflows/gradle.yml:**
```yaml
# Change Java version
- name: Set up JDK 21
  uses: actions/setup-java@v3
  with:
    java-version: '21'  # Was '11'
    distribution: 'temurin'
```

**Considerations:**
- Ensure GitHub Actions runners support Java 21
- Update any Docker images to Java 21
- Verify cache keys for dependency caching

### 7. Database and Persistence Layer

**Impact: 🟢 LOW - Minimal changes expected**

**SQLite JDBC Driver:**
- Update recommended but not required for Java 21
- Current version (3.36.0.3) should work but is outdated

**Flyway Migrations:**
- Flyway 9+ (Spring Boot 3.x) vs Flyway 8 (Spring Boot 2.x)
- Existing migration scripts should work unchanged
- New migration naming conventions to review

**MyBatis Type Handlers:**
- `DateTimeHandler.java` for Joda-Time needs review
- May need updates for Jakarta EE annotations

---

## Recommended Upgrade Path

### Strategy: Incremental Migration via Java 17 LTS

**Rationale:**
- Minimizes risk by breaking the migration into manageable stages
- Java 17 LTS provides a stable intermediate target
- Allows thorough testing at each stage before proceeding
- Spring Boot 3.0 requires Java 17 minimum (not Java 21)
- Industry best practice for major framework migrations

### Migration Phases

#### Phase 1: Java 17 Upgrade (1-2 weeks)

**Objective:** Upgrade from Java 11 to Java 17 while maintaining Spring Boot 2.x

**Steps:**
1. Update `build.gradle`:
   ```gradle
   sourceCompatibility = '17'
   targetCompatibility = '17'
   ```

2. Update CI/CD configuration:
   ```yaml
   java-version: '17'
   ```

3. Update Gradle wrapper (if needed):
   ```bash
   ./gradlew wrapper --gradle-version 8.5
   ```

4. Address Java 17 specific issues:
   - Review removed/deprecated JDK APIs
   - Test with Java 17 JVM
   - Fix any reflection warnings
   - Update IDE configurations

5. Run comprehensive test suite:
   ```bash
   ./gradlew clean test
   ./gradlew spotlessJavaCheck
   ```

**Verification:**
- All tests pass on Java 17
- Application runs successfully on Java 17
- No runtime warnings or errors
- CI/CD pipeline passes

**Rollback Plan:**
- Revert `build.gradle` and CI/CD changes
- No database migrations at this stage
- Low-risk rollback

**Benefits:**
- Validates Java 17 compatibility early
- Identifies JDK-related issues before framework migration
- Establishes baseline for Spring Boot 3.x upgrade

#### Phase 2: Spring Boot 3.x Migration (2-3 weeks)

**Objective:** Migrate from Spring Boot 2.6.3 to Spring Boot 3.2.x with Jakarta EE namespace changes

**Substeps:**

**2.1: Preparation and Analysis**
- Review Spring Boot 3.0 migration guide
- Identify all `javax.*` imports in codebase
- Review Spring Security 5 → 6 migration guide
- Backup current working state

**2.2: Dependency Updates**
1. Update Spring Boot version in `build.gradle`:
   ```gradle
   id 'org.springframework.boot' version '3.2.5'  // Use latest 3.2.x
   id 'io.spring.dependency-management' version '1.1.4'
   ```

2. Update all Spring Boot starters (auto-managed by dependency management)

**2.3: Jakarta EE Namespace Migration**

Use automated tools for initial migration:
```bash
# Using OpenRewrite (recommended)
./gradlew rewriteRun

# OR manual find-replace with careful review
find src -name "*.java" -exec sed -i 's/javax.servlet/jakarta.servlet/g' {} +
find src -name "*.java" -exec sed -i 's/javax.validation/jakarta.validation/g' {} +
find src -name "*.java" -exec sed -i 's/javax.persistence/jakarta.persistence/g' {} +
```

**Critical files to review manually:**
- `WebSecurityConfig.java`
- `JwtTokenFilter.java`
- All controllers in `/api` package
- All GraphQL resolvers
- Custom validators

**2.4: Spring Security 6.0 Migration**

Update security configuration:
```java
// WebSecurityConfig.java
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/users/login", "/api/users").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .csrf(csrf -> csrf.disable());
        
        return http.build();
    }
}
```

**2.5: Testing and Validation**
1. Run all unit tests:
   ```bash
   ./gradlew test
   ```

2. Run integration tests:
   ```bash
   ./gradlew integrationTest  # If separate
   ```

3. Manual testing:
   - Test user registration and login
   - Test JWT token generation and validation
   - Test article CRUD operations
   - Test comment functionality
   - Test following/favoriting features

4. Security testing:
   - Verify authentication still works
   - Test authorization rules
   - Verify CORS configuration
   - Test JWT expiration and refresh

**2.6: Application Configuration Review**
- Review `application.properties` for deprecated properties
- Update any Spring Boot 2.x specific configurations
- Verify logging configuration
- Check actuator endpoint configurations

**Verification:**
- All tests pass
- Application starts successfully
- All API endpoints respond correctly
- GraphQL queries work
- Authentication and authorization function correctly
- No errors in logs during startup

**Rollback Plan:**
- Git branch allows easy revert
- Database state unchanged (no schema migrations)
- Can rollback to Phase 1 (Java 17 + Spring Boot 2.x)

#### Phase 3: MyBatis 3.0 Upgrade (3-5 days)

**Objective:** Upgrade MyBatis Spring Boot Starter to 3.0+ for Jakarta EE compatibility

**Steps:**
1. Update dependency in `build.gradle`:
   ```gradle
   implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
   ```

2. Review MyBatis configuration:
   - Check `mybatis-config.xml` for any deprecated settings
   - Verify type handlers still work
   - Review custom MyBatis plugins

3. Update repository implementations:
   - Review `MyBatisArticleRepository.java`
   - Review `MyBatisUserRepository.java`
   - Check transaction management

4. Test data access layer:
   ```bash
   ./gradlew test --tests *RepositoryTest
   ```

5. Verify database operations:
   - CRUD operations for all entities
   - Complex queries with joins
   - Transaction rollback scenarios
   - Pagination functionality

**Verification:**
- All repository tests pass
- Database operations work correctly
- No N+1 query issues
- Transaction management works

**Rollback Plan:**
- Simple dependency version revert
- No database schema changes

#### Phase 4: Netflix DGS 10.x Upgrade (1 week)

**Objective:** Upgrade DGS framework to latest version compatible with Spring Boot 3.x

**Steps:**
1. Update DGS dependencies in `build.gradle`:
   ```gradle
   id 'com.netflix.dgs.codegen' version '6.2.1'  // Update codegen plugin
   
   implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:latest.release'
   ```

2. Update code generation configuration:
   - Review DGS codegen configuration
   - Regenerate GraphQL client code
   - Review generated types

3. Update GraphQL resolvers:
   - Review all `@DgsQuery` and `@DgsMutation` methods
   - Update any deprecated DGS annotations
   - Review DataLoader implementations
   - Check custom scalar types

4. Review GraphQL schema:
   - Verify schema compatibility
   - Check for any breaking changes in schema syntax
   - Review federation directives (if used)

5. Test GraphQL API:
   - Test all queries
   - Test all mutations
   - Test subscriptions (if used)
   - Test error handling
   - Verify DataLoader batching

**Verification:**
- All GraphQL tests pass
- Schema compiles successfully
- Code generation works
- All queries and mutations function correctly
- Performance is maintained

**Rollback Plan:**
- Revert DGS dependency versions
- Revert code generation plugin

#### Phase 5: JJWT 0.13.0 Upgrade (2-3 days)

**Objective:** Upgrade JJWT library to latest stable version

**Steps:**
1. Update JJWT dependencies in `build.gradle`:
   ```gradle
   implementation 'io.jsonwebtoken:jjwt-api:0.13.0'
   runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.13.0'
   runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.13.0'
   ```

2. Review JWT service implementation:
   - Check `DefaultJwtService.java`
   - Verify token generation still works
   - Verify token parsing and validation
   - Check claims extraction

3. Test JWT functionality:
   - Token generation with various claims
   - Token validation with expired tokens
   - Token validation with invalid signatures
   - Custom claims handling

**Verification:**
- JWT tests pass
- Token generation works
- Token validation works
- Authentication flow works end-to-end

**Rollback Plan:**
- Simple dependency version revert
- No breaking changes expected

#### Phase 6: Java 21 Final Upgrade (3-5 days)

**Objective:** Upgrade from Java 17 to Java 21 and leverage new features

**Steps:**
1. Update `build.gradle`:
   ```gradle
   sourceCompatibility = '21'
   targetCompatibility = '21'
   ```

2. Update CI/CD:
   ```yaml
   java-version: '21'
   ```

3. Update Lombok (ensure Java 21 compatibility):
   ```gradle
   compileOnly 'org.projectlombok:lombok:1.18.30'
   annotationProcessor 'org.projectlombok:lombok:1.18.30'
   ```

4. Test with Java 21:
   - Run full test suite
   - Check for any Java 21 specific issues
   - Verify performance

5. **Optional:** Evaluate Java 21 features:
   - Virtual Threads (Spring Boot 3.2+ support)
   - Pattern Matching enhancements
   - Record Patterns
   - Switch expressions
   - **Lombok vs Records evaluation** (see Open Questions section)

**Verification:**
- All tests pass on Java 21
- Application runs successfully
- No performance regressions
- No new warnings or errors
- CI/CD pipeline passes

**Rollback Plan:**
- Revert to Java 17 (already tested in Phase 1)
- Clean rollback path

#### Phase 7: Performance Testing and Optimization (1 week)

**Objective:** Ensure the migrated application performs well and leverage Java 21 features

**Steps:**
1. Performance baseline comparison:
   - Compare Java 11 vs Java 21 performance
   - Measure API response times
   - Check memory usage
   - Monitor GC behavior

2. Load testing:
   - Use tools like Apache JMeter or Gatling
   - Test concurrent user scenarios
   - Test GraphQL query performance
   - Verify database connection pooling

3. **Optional:** Enable Virtual Threads:
   ```properties
   # application.properties
   spring.threads.virtual.enabled=true
   ```

4. Monitoring and observability:
   - Check application metrics
   - Review logs for warnings
   - Monitor actuator endpoints

**Verification:**
- Performance meets or exceeds baseline
- No memory leaks detected
- Application stable under load
- Metrics look healthy

### Alternative Approach: Big Bang Migration

**Not Recommended** for this project due to:
- High complexity with multiple dependencies
- CQRS and dual API architecture increases risk
- Jakarta EE namespace migration affects entire codebase
- Difficult to isolate issues if multiple upgrades fail

**When Big Bang Might Work:**
- Smaller codebase with fewer dependencies
- Dedicated migration team with backup resources
- Extensive test coverage (this project has good coverage)
- Aggressive timeline requirements

### Timeline Summary

| Phase | Duration | Dependencies | Risk Level |
|-------|----------|--------------|------------|
| Phase 1: Java 17 | 1-2 weeks | None | 🟢 Low |
| Phase 2: Spring Boot 3.x | 2-3 weeks | Phase 1 | 🔴 High |
| Phase 3: MyBatis 3.0 | 3-5 days | Phase 2 | 🟡 Medium |
| Phase 4: Netflix DGS 10.x | 1 week | Phase 2 | 🟡 Medium |
| Phase 5: JJWT 0.13.0 | 2-3 days | Phase 2 | 🟢 Low |
| Phase 6: Java 21 | 3-5 days | Phases 1-5 | 🟢 Low |
| Phase 7: Performance Testing | 1 week | Phase 6 | 🟢 Low |
| **Total** | **6-8 weeks** | Sequential | 🟡 Medium |

**Critical Path:** Phase 2 (Spring Boot 3.x) is the longest and highest risk phase.

---

## Risk Assessment

### High-Risk Areas

#### 1. Jakarta EE Namespace Migration 🔴

**Risk Level:** CRITICAL

**Description:** The javax.* → jakarta.* namespace change affects the entire codebase, with potential for subtle runtime issues.

**Potential Issues:**
- Missed imports in complex class hierarchies
- Third-party libraries not yet Jakarta EE compatible
- Reflection-based code using old namespaces
- Custom annotations and meta-annotations

**Mitigation Strategies:**
- Use automated migration tools (OpenRewrite, Spring Boot Migrator)
- Comprehensive code review of all changes
- Extensive integration testing
- Gradual rollout with feature flags
- Keep Spring Boot 2.x version running in parallel initially

**Contingency Plan:**
- Maintain separate branch for Spring Boot 2.x
- Prepare rollback procedures
- Document all namespace changes

#### 2. Spring Security 6.0 Configuration Changes 🔴

**Risk Level:** HIGH

**Description:** Security configuration changes could inadvertently expose endpoints or break authentication.

**Potential Issues:**
- Incorrectly migrated authorization rules
- JWT filter chain issues
- CORS configuration changes
- Session management changes

**Mitigation Strategies:**
- Security-focused code review
- Penetration testing after migration
- Comprehensive security test suite
- Test all authorization scenarios
- Verify JWT token validation

**Contingency Plan:**
- Security audit before production deployment
- Quick rollback if vulnerabilities discovered
- Staging environment testing

#### 3. GraphQL API Compatibility 🟡

**Risk Level:** MEDIUM-HIGH

**Description:** Netflix DGS major version upgrade could break GraphQL API contracts.

**Potential Issues:**
- Schema breaking changes
- Client query compatibility
- DataLoader behavior changes
- Generated code incompatibilities

**Mitigation Strategies:**
- GraphQL schema diff analysis
- Client compatibility testing
- Comprehensive GraphQL test suite
- Gradual client migration if needed

**Contingency Plan:**
- API versioning strategy
- Parallel DGS versions (if needed)
- Client notification of changes

### Medium-Risk Areas

#### 4. MyBatis Transaction Management 🟡

**Risk Level:** MEDIUM

**Description:** Spring Boot 3.x transaction management changes could affect MyBatis operations.

**Potential Issues:**
- Transaction propagation changes
- Distributed transaction issues
- Connection pool configuration

**Mitigation Strategies:**
- Thorough testing of all database operations
- Transaction boundary verification
- Stress testing with concurrent operations

**Contingency Plan:**
- Database rollback procedures
- Transaction logging for debugging

#### 5. Build and CI/CD Pipeline 🟡

**Risk Level:** MEDIUM

**Description:** Gradle and CI/CD changes could cause build failures.

**Potential Issues:**
- Gradle plugin compatibility
- Dependency resolution conflicts
- CI runner configuration issues
- Code coverage tool compatibility

**Mitigation Strategies:**
- Test builds locally first
- Update CI/CD in stages
- Pin dependency versions explicitly
- Maintain build reproducibility

**Contingency Plan:**
- Separate CI/CD branch for testing
- Rollback to working build configuration

### Low-Risk Areas

#### 6. JJWT Library Upgrade 🟢

**Risk Level:** LOW

**Description:** JJWT has excellent backward compatibility.

**Potential Issues:**
- Minor API changes (unlikely)
- Jackson version alignment

**Mitigation Strategies:**
- JWT integration tests
- Token validation testing

**Contingency Plan:**
- Simple dependency revert

#### 7. Java 21 Runtime 🟢

**Risk Level:** LOW (after Spring Boot 3.x migration)

**Description:** Java 21 is well-tested with Spring Boot 3.2+.

**Potential Issues:**
- Minor JDK API changes
- Performance characteristics

**Mitigation Strategies:**
- Performance baseline testing
- Monitoring in staging

**Contingency Plan:**
- Run on Java 17 (already tested)

### Cross-Cutting Risks

#### Third-Party Dependencies

**Risk:** Transitive dependencies may not be compatible with Java 21 or Spring Boot 3.x

**Mitigation:**
- Dependency analysis before each phase
- Explicit version management in Gradle
- Test with all transitive dependencies

#### Database Schema

**Risk:** Flyway migrations could fail with newer versions

**Mitigation:**
- Test migrations in isolated environment
- Backup database before migration
- Dry-run of all migrations

#### Frontend Compatibility

**Risk:** Next.js frontend may have API contract changes

**Mitigation:**
- Contract testing between frontend and backend
- Parallel testing of frontend with new backend
- API versioning if needed

### Risk Mitigation Summary

**Overall Risk Level:** 🟡 MEDIUM-HIGH

The migration is achievable but requires careful planning and execution. The incremental approach significantly reduces risk compared to a big bang migration.

**Key Success Factors:**
1. Comprehensive test coverage (project already has good coverage)
2. Incremental migration approach
3. Thorough testing at each phase
4. Staging environment for validation
5. Rollback procedures at each phase
6. Code review for security-critical changes
7. Performance monitoring

---

## Timeline Estimates

### Detailed Time Breakdown

#### Development Time

| Phase | Task | Estimated Time | Notes |
|-------|------|----------------|-------|
| **Phase 1** | Java 17 Upgrade | **1-2 weeks** | |
| | Environment setup | 1 day | Update JDK, IDE, build tools |
| | Build configuration | 1 day | Gradle, CI/CD updates |
| | Compatibility testing | 2-3 days | Run tests, fix issues |
| | Code review and validation | 2-3 days | Ensure no regressions |
| **Phase 2** | Spring Boot 3.x Migration | **2-3 weeks** | CRITICAL PATH |
| | Planning and analysis | 2 days | Review migration guides |
| | Dependency updates | 1 day | Update build.gradle |
| | Jakarta namespace migration | 3-4 days | Automated + manual updates |
| | Spring Security 6.0 migration | 3-4 days | Security config, testing |
| | Integration testing | 3-4 days | Full application testing |
| | Bug fixes and adjustments | 2-3 days | Address issues found |
| **Phase 3** | MyBatis 3.0 Upgrade | **3-5 days** | |
| | Dependency update | 0.5 day | Simple version change |
| | Repository testing | 1-2 days | Test all data access |
| | Integration testing | 1-2 days | End-to-end testing |
| | Bug fixes | 0.5-1 day | Address issues |
| **Phase 4** | Netflix DGS 10.x Upgrade | **1 week** | |
| | Dependency updates | 1 day | DGS and codegen plugin |
| | Code generation | 1 day | Regenerate, review changes |
| | Resolver updates | 2 days | Update GraphQL resolvers |
| | GraphQL testing | 2 days | Comprehensive API testing |
| | Bug fixes | 1 day | Address issues |
| **Phase 5** | JJWT 0.13.0 Upgrade | **2-3 days** | |
| | Dependency update | 0.5 day | Simple version change |
| | JWT service testing | 1 day | Token generation/validation |
| | Integration testing | 0.5-1 day | Auth flow testing |
| | Bug fixes | 0.5 day | Minor adjustments |
| **Phase 6** | Java 21 Final Upgrade | **3-5 days** | |
| | Configuration updates | 0.5 day | Java version, CI/CD |
| | Lombok compatibility | 0.5 day | Ensure Java 21 support |
| | Full test suite | 1-2 days | Comprehensive testing |
| | Feature evaluation | 1 day | Virtual Threads, Records |
| | Bug fixes | 0.5-1 day | Address issues |
| **Phase 7** | Performance Testing | **1 week** | |
| | Performance baseline | 1 day | Establish metrics |
| | Load testing | 2 days | Stress test application |
| | Optimization | 2 days | Address bottlenecks |
| | Documentation | 1 day | Update performance docs |
| **Total Development** | | **6-8 weeks** | Assumes sequential execution |

#### Additional Time Considerations

**Code Review:** +10-15% of development time
- Security-focused reviews for Phase 2
- Architecture reviews for major changes

**QA/Testing:** +20-30% of development time
- Manual testing of critical flows
- Exploratory testing
- Regression testing

**Documentation:** +5-10% of development time
- Update README.md
- Update API documentation
- Create migration runbook

**Buffer for Issues:** +15-20% of total time
- Unexpected compatibility issues
- Third-party dependency problems
- Performance issues requiring investigation

### Realistic Timeline with Contingency

**Optimistic (Everything goes smoothly):** 6 weeks  
**Realistic (Normal development pace):** 8-10 weeks  
**Pessimistic (Significant issues encountered):** 12-14 weeks

**Recommended Schedule:** 10 weeks (2.5 months)
- Includes adequate testing time
- Includes buffer for unexpected issues
- Allows for proper code review
- Includes staging environment validation

### Resource Requirements

**Developer Time:**
- 1 senior developer full-time for 8-10 weeks
- OR 2 developers part-time (50% each) for 10-12 weeks

**Supporting Resources:**
- QA engineer: 20-30 hours for testing
- DevOps engineer: 10-15 hours for CI/CD and deployment
- Security review: 5-10 hours

**Infrastructure:**
- Staging environment for validation
- Performance testing environment
- CI/CD pipeline capacity

### Milestones and Checkpoints

| Week | Milestone | Deliverable | Go/No-Go Decision |
|------|-----------|-------------|-------------------|
| 2 | Phase 1 Complete | Java 17 working | Continue to Phase 2 |
| 5 | Phase 2 Complete | Spring Boot 3.x working | Major checkpoint |
| 6 | Phase 3 Complete | MyBatis 3.0 working | Continue |
| 7 | Phase 4 Complete | DGS 10.x working | Continue |
| 7.5 | Phase 5 Complete | JJWT 0.13.0 working | Continue |
| 8 | Phase 6 Complete | Java 21 working | Continue to perf testing |
| 10 | Phase 7 Complete | Performance validated | Production ready |

**Critical Checkpoint:** End of Phase 2 (Week 5)
- This is the highest-risk phase
- Go/No-Go decision should assess:
  - All tests passing
  - Security verified
  - No major blockers identified
  - Performance acceptable

---

## Open Questions Addressed

### 1. What format should the final compatibility documentation take?

**Answer:** Markdown document in repository root

This document (`JAVA_21_MIGRATION_PLAN.md`) serves as the comprehensive compatibility assessment and migration plan. It provides:
- Executive summary for stakeholders
- Technical details for developers
- Step-by-step migration guide
- Risk assessment for project managers
- Timeline estimates for planning

The markdown format is:
- Version controlled alongside code
- Easy to read and navigate
- Supports links and formatting
- Can be converted to other formats (PDF, HTML) if needed

### 2. Should we consider intermediate Java versions (17 LTS) as stepping stones?

**Answer:** Yes, strongly recommended

**Rationale:**
- **Spring Boot 3.0 requires Java 17 minimum** (not Java 21)
- Java 17 is an LTS (Long-Term Support) release
- Allows validation of JDK compatibility separate from framework migration
- Industry best practice for major version upgrades
- Provides stable intermediate state for testing
- Reduces risk by breaking migration into smaller steps

**Recommended Path:**
1. Java 11 → Java 17 (Phase 1)
2. Spring Boot 2.x → Spring Boot 3.x on Java 17 (Phase 2-5)
3. Java 17 → Java 21 (Phase 6)

This approach allows each major change to be tested independently.

### 3. What are the specific jakarta.* namespace impacts on our current Spring Security + JWT setup?

**Answer:** Significant impact requiring code changes across security layer

**Specific Impacts:**

**Authentication Layer:**
```java
// Affected Files:
- WebSecurityConfig.java
- JwtTokenFilter.java
- InvalidAuthenticationException.java

// Namespace Changes Required:
import javax.servlet.FilterChain;           → import jakarta.servlet.FilterChain;
import javax.servlet.ServletException;      → import jakarta.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;  → import jakarta.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse; → import jakarta.servlet.http.HttpServletResponse;
```

**Security Configuration:**
- Spring Security 6.0 API changes compound the namespace migration
- `WebSecurityConfigurerAdapter` removed → use `SecurityFilterChain` beans
- Method signature changes in security configuration
- Filter chain configuration requires updates

**JWT Token Filter:**
- Servlet API imports need jakarta.* namespace
- Filter doFilter() method signature references Jakarta Servlet types
- Response and request handling updated for Jakarta

**Impact Assessment:**
- **Scope:** ~5-10 security-related files
- **Effort:** 1-2 days for migration + testing
- **Risk:** High (security-critical code)
- **Testing:** Requires comprehensive security testing

**Mitigation:**
- Use automated tools for initial migration
- Manual security-focused code review
- Test all authentication/authorization flows
- Verify JWT token validation still works
- Test CORS configuration

### 4. How will the dual API surface (REST + GraphQL) complicate the Spring Boot upgrade?

**Answer:** Moderate complication - both APIs require separate validation

**Complications:**

**1. Separate Migration Paths:**
- **REST API:** Spring MVC → Spring Web 6.0 (built into Spring Boot 3.x)
- **GraphQL API:** Netflix DGS 4.x → 10.x (requires major version upgrade)

**2. Testing Requirements:**
- Must test both API surfaces independently
- Integration tests for both REST and GraphQL
- Authentication must work for both (shared JWT)
- Authorization rules must be consistent

**3. Namespace Changes Affect Both:**
- REST controllers: `javax.servlet.*` → `jakarta.servlet.*`
- GraphQL resolvers: Less affected (DGS abstracts servlet layer)
- Common security layer: Affects both APIs

**4. Dependency Conflicts:**
- DGS may have transitive dependencies that conflict
- Both APIs share Spring Boot core
- Must ensure compatible versions

**Strategy to Minimize Complication:**

**Phase 2 (Spring Boot 3.x):**
- Migrate REST API first
- Ensure REST API fully functional
- Use Spring Boot 3.x compatible DGS version temporarily

**Phase 4 (DGS Upgrade):**
- Migrate GraphQL API
- Leverage working REST API as reference
- Test both APIs in parallel

**Testing Strategy:**
- Separate test suites for REST and GraphQL
- Integration tests that use both APIs
- Performance testing for both
- Security testing for both

**Impact Assessment:**
- **Added Complexity:** 20-30% more testing required
- **Risk:** Medium (both APIs must work)
- **Mitigation:** Phased approach, separate testing

### 5. What are the implications for our CQRS pattern implementation with newer framework versions?

**Answer:** Minimal impact - CQRS pattern is framework-agnostic

**CQRS Components Analysis:**

**Command Side (Write Operations):**
- Services in `/application/article/`, `/application/user/`
- Current implementation uses Spring services and MyBatis
- **Impact:** Low - MyBatis 3.0 is backward compatible
- **Changes:** None to CQRS pattern itself

**Query Side (Read Operations):**
- `ArticleQueryService`, `CommentQueryService`
- Complex read operations with MyBatis XML mappers
- **Impact:** Low - SQL queries unchanged
- **Changes:** None to CQRS pattern itself

**Separation of Concerns:**
- CQRS pattern maintained at application layer
- Spring Boot upgrade doesn't affect business logic separation
- Domain models remain unchanged

**Potential Improvements with Java 21:**
- **Records** could simplify DTOs (ArticleData, UserData, etc.)
- **Pattern Matching** could simplify command handling
- **Virtual Threads** could improve query performance

**Recommendations:**
1. **Maintain current CQRS structure** during migration
2. **Test read and write operations separately** to ensure both sides work
3. **Consider refactoring after migration** to leverage Java 21 features
4. **Document CQRS patterns** to ensure they're preserved

**Impact Assessment:**
- **Pattern Preservation:** No changes required
- **Testing:** Test command and query operations separately
- **Opportunity:** Post-migration refactoring to use Records for DTOs

### 6. Are there any show-stopping compatibility issues that would block Java 21 migration?

**Answer:** No show-stopping issues identified

**Compatibility Analysis:**

✅ **Spring Boot 3.2+:** Full Java 21 support  
✅ **MyBatis 3.0+:** Java 17+ support (compatible with Java 21)  
✅ **Netflix DGS 10.x:** Java 17+ support (compatible with Java 21)  
✅ **JJWT 0.13.0:** Java 7+ support (fully backward compatible)  
✅ **SQLite JDBC:** Java 21 compatible  
✅ **Lombok:** 1.18.28+ supports Java 21  
✅ **Gradle:** 8.5+ supports Java 21

**Potential Blockers Investigated:**

**1. Third-Party Dependencies:**
- Reviewed transitive dependencies
- No known Java 21 incompatibilities
- All major frameworks have Java 21 support

**2. Joda-Time (DateTimeHandler):**
- Project uses Joda-Time for date handling
- Joda-Time is compatible with Java 21
- Consider migrating to Java 8 Time API post-migration

**3. SQLite Database:**
- SQLite JDBC driver supports Java 21
- Database files are cross-compatible
- No schema changes required

**4. Build Tools:**
- Gradle 8.5+ fully supports Java 21
- All Gradle plugins have compatible versions
- CI/CD runners support Java 21

**Risks That Are NOT Blockers:**

🟡 **Jakarta EE Migration:** High effort, but well-documented and tooling available  
🟡 **Spring Security Changes:** Requires careful migration, but proven path exists  
🟡 **DGS Major Version:** Breaking changes, but migration guide available  
🟢 **JJWT Upgrade:** Minimal risk, backward compatible

**Conclusion:**

No technical blockers prevent Java 21 migration. The main challenges are:
1. **Effort required** (6-10 weeks)
2. **Jakarta namespace migration** (time-consuming but not blocking)
3. **Testing thoroughness** (critical for success)

**Risk Level:** 🟡 Medium-High (manageable with proper planning)

**Go/No-Go Recommendation:** ✅ **GO** - Proceed with migration using the incremental approach outlined in this document.

---

## Conclusion

### Summary

The migration from Java 11 to Java 21 for the Spring Boot RealWorld Example App is **feasible and recommended**, but requires careful planning and execution. The incremental migration strategy via Java 17 LTS provides the safest path forward, with a realistic timeline of **8-10 weeks** for complete migration.

### Key Takeaways

1. **All dependencies have Java 21-compatible versions** - No show-stoppers identified
2. **Jakarta EE namespace migration is the biggest challenge** - Well-documented with tooling support
3. **Incremental approach minimizes risk** - Test and validate at each phase
4. **Spring Boot 3.2+ provides excellent Java 21 support** - Including virtual threads
5. **Comprehensive testing is critical** - Both REST and GraphQL APIs must be validated

### Next Steps

1. **Review and approve this migration plan**
2. **Allocate development resources** (1 senior developer, 8-10 weeks)
3. **Set up staging environment** for migration testing
4. **Begin Phase 1:** Java 17 upgrade
5. **Proceed through phases** as outlined in Recommended Upgrade Path

### Success Criteria

- ✅ All tests passing on Java 21
- ✅ Both REST and GraphQL APIs functional
- ✅ Security verified and tested
- ✅ Performance meets or exceeds baseline
- ✅ No runtime errors or warnings
- ✅ CI/CD pipeline successful
- ✅ Documentation updated

### Documentation

This migration plan should be:
- Committed to the repository
- Referenced during migration
- Updated if issues arise
- Used for knowledge transfer

---

## References

### External Documentation

1. **Spring Boot:**
   - [Spring Boot 3.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes)
   - [Spring Boot 3.2 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)
   - [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)

2. **MyBatis:**
   - [MyBatis Spring Boot Starter Documentation](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)

3. **Netflix DGS:**
   - [DGS Framework Documentation](https://netflix.github.io/dgs/)
   - [DGS Migration Guide](https://netflix.github.io/dgs/migration/)

4. **JJWT:**
   - [JJWT GitHub Repository](https://github.com/jwtk/jjwt)
   - [JJWT 0.13.0 Release Notes](https://github.com/jwtk/jjwt/releases/tag/0.13.0)

5. **Jakarta EE:**
   - [Jakarta EE 9 Migration Guide](https://jakarta.ee/specifications/platform/9/)
   - [Spring Framework Jakarta Migration](https://docs.spring.io/spring-framework/reference/6.0/core/beans/java/jakarta.html)

### Internal References

- `build.gradle` - Current dependency configuration
- `.github/workflows/gradle.yml` - CI/CD configuration  
- `README.md` - Application documentation (to be updated post-migration)
- CQRS Architecture documentation (see project onboarding guide)

---

**Document End**

*This document was generated as part of ticket JP-9 for the Spring Boot RealWorld Example App Java 21 migration assessment.*
