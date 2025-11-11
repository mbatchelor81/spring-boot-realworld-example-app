# JP-9: Java 21 Dependency Compatibility Assessment

**Ticket ID:** JP-9  
**Date:** November 11, 2025  
**Author:** Devin (AI Software Engineer)  
**Purpose:** Research and document dependency compatibility for Java 21 migration

---

## Executive Summary

This document provides a comprehensive compatibility assessment for upgrading the Spring Boot RealWorld Example Application from Java 11 to Java 21. The migration requires upgrading all four primary dependencies to versions that support Java 21, with Spring Boot requiring the most significant changes due to the mandatory migration from Spring Boot 2.x to 3.x.

### Key Findings

**Migration Feasibility:** Java 21 migration is feasible but requires significant effort due to breaking changes across all major dependencies.

**Critical Blocker:** Spring Boot 2.6.3 → 3.2+ migration is the most impactful change, requiring:
- Minimum Java 17 baseline (Java 21 supported in Spring Boot 3.2+)
- Migration from Java EE to Jakarta EE APIs (javax.* → jakarta.*)
- Spring Framework 5.x → 6.x upgrade with breaking changes
- Spring Security 5.x → 6.x upgrade with security configuration changes

**Recommended Approach:** Incremental upgrade strategy with intermediate Java 17 LTS step before moving to Java 21.

**Estimated Timeline:** 4-6 weeks for full migration including testing and validation.

---

## Current State Inventory

### Application Configuration

**Current Java Version:** 11  
**Build Tool:** Gradle 7.x  
**Source/Target Compatibility:** Java 11

### Dependency Versions (from build.gradle)

| Dependency | Current Version | Release Date | Age |
|------------|----------------|--------------|-----|
| Spring Boot | 2.6.3 | January 2022 | ~3.8 years old |
| MyBatis Spring Boot Starter | 2.2.2 | January 2022 | ~3.8 years old |
| Netflix DGS | 4.9.21 | ~2022 | ~3 years old |
| JJWT | 0.11.2 | March 2021 | ~4.7 years old |

### CI/CD Configuration

**GitHub Actions Workflow:** `.github/workflows/gradle.yml`
- Current JDK: 11 (Zulu distribution)
- Build command: `./gradlew clean test -x jacocoTestCoverageVerification`

### Additional Dependencies of Note

- Spring Boot Starter Web
- Spring Boot Starter Validation
- Spring Boot Starter HATEOAS
- Spring Boot Starter Security
- Flyway Core (database migrations)
- SQLite JDBC 3.36.0.3
- Lombok (code generation)
- Joda-Time 2.10.13

---

## Java 21 Compatibility Matrix

### Spring Boot: 2.6.3 → 3.2+ (or latest 3.x)

**Current Version:** 2.6.3 (January 2022)  
**First Java 21 Compatible Version:** 3.2.0 (November 2023)  
**Latest Stable Version:** 3.5.x (2025)  
**Minimum Java Version for Target:** Java 17 (Java 21 supported in 3.2+)

#### Version Gap Analysis

- **Major versions to cross:** 2.6 → 2.7 → 3.0 → 3.1 → 3.2
- **Breaking change releases:** 2.7 (minor), 3.0 (major), 3.2 (Java 21 support)
- **Critical milestone:** Spring Boot 3.0 requires Java 17 minimum

#### Key Compatibility Information

**Java 17 Support:** Spring Boot 3.0+ requires Java 17 as minimum  
**Java 21 Support:** Spring Boot 3.2+ (November 2023) added official Java 21 support with virtual threads  
**Virtual Threads:** Spring Boot 3.2 introduced `spring.threads.virtual.enabled` property for Project Loom support

#### Breaking Changes (2.6.3 → 3.2+)

**Major Breaking Changes:**

1. **Jakarta EE Migration (Spring Boot 3.0)**
   - All `javax.*` imports must change to `jakarta.*`
   - Affects: Servlet, Persistence, Validation, Transaction, Mail, etc.
   - Impact: Every file using Java EE APIs requires import updates
   - Scope: High - affects controllers, entities, security, validation

2. **Spring Framework 5.3 → 6.0+**
   - Requires Java 17 baseline
   - Removed deprecated APIs from Spring Framework 5.x
   - Updated configuration model
   - Parameter name discovery changes (requires `-parameters` compiler flag)

3. **Spring Security 5.x → 6.0+**
   - Complete security configuration overhaul
   - Deprecated `WebSecurityConfigurerAdapter` removed
   - New component-based security configuration
   - JWT and OAuth2 configuration changes
   - Impact: High - requires rewriting `WebSecurityConfig.java`

4. **Spring Data 2022.0+**
   - Repository interface changes
   - Query method naming updates
   - Pagination API changes

5. **Hibernate 5.6 → 6.1+**
   - JPA 3.1 support (Jakarta Persistence)
   - Type system changes
   - Query language updates

6. **Dependency Version Bumps**
   - Tomcat 9 → 10
   - Jetty 11 → 12 (if using Jetty)
   - Jackson 2.13 → 2.14+
   - Micrometer 1.9 → 1.10+

**Configuration Changes:**

- Properties renamed/restructured (see Spring Boot 3.0 Configuration Changelog)
- Actuator endpoint changes
- Logging configuration updates
- Auto-configuration class relocations

**Removed Features:**

- Java 8 and Java 11 support (3.0 requires Java 17)
- `CommonsMultipartResolver` support
- `SecurityManager` support
- Various deprecated APIs from 2.x

### MyBatis Spring Boot Starter: 2.2.2 → 3.0.3+

**Current Version:** 2.2.2 (January 2022)  
**First Java 21 Compatible Version:** 3.0.3 (November 2023)  
**Latest Stable Version:** 3.0.5 (July 2025)  
**Minimum Java Version for Target:** Java 17

#### Version Gap Analysis

- **Major versions to cross:** 2.2.2 → 2.3.x → 3.0.x
- **Breaking change releases:** 3.0.0 (major - Spring Boot 3.0 alignment)
- **Critical dependency:** Requires Spring Boot 3.0+ for version 3.x

#### Key Compatibility Information

**Java 17 Support:** MyBatis Spring Boot 3.0.0+ (November 2022)  
**Java 21 Support:** MyBatis Spring Boot 3.0.3+ (November 2023) with Spring Boot 3.2  
**Spring Boot Alignment:** Version 3.0.3 specifically added Spring Boot 3.2 support

#### Breaking Changes (2.2.2 → 3.0.3+)

**Major Breaking Changes:**

1. **Spring Boot 3.0 Baseline**
   - Requires Spring Boot 3.0+ (and thus Java 17+)
   - Jakarta EE namespace migration (javax.* → jakarta.*)
   - Affects MyBatis mapper interfaces and type handlers

2. **MyBatis Core Upgrade**
   - MyBatis 3.5.11+ required
   - Configuration property structure changes
   - New nested configuration properties instead of direct Configuration object

3. **MyBatis Spring Upgrade**
   - MyBatis Spring 2.0.x → 3.0.x
   - Transaction handling updates
   - SqlSessionFactory configuration changes

4. **Configuration Changes**
   - New configuration property structure
   - Removed/deprecated configuration options
   - `multipleResultSetsEnabled` deprecated in 3.0.4

**Database Compatibility:**

- SQLite JDBC driver compatibility maintained
- No known breaking changes for SQLite usage
- Flyway migration compatibility (both support Spring Boot 3.x)

**Migration Path:**

- Cannot upgrade directly from 2.2.2 to 3.0.x without Spring Boot 3.0
- Must upgrade Spring Boot first, then MyBatis
- Mapper XML files generally compatible (no syntax changes)

### Netflix DGS (Domain Graph Service): 4.9.21 → Latest

**Current Version:** 4.9.21 (~2022)  
**First Java 21 Compatible Version:** 5.0.0+ (with Spring Boot 3.0, November 2022)  
**Latest Stable Version:** 10.4.0 (September 2025)  
**Minimum Java Version for Target:** Java 17 (required by Spring Boot 3.0+)

#### Version Gap Analysis

- **Major versions to cross:** 4.9.21 → 5.x → 6.x → 7.x → 8.x → 9.x → 10.x
- **Breaking change releases:** 5.0.0 (Spring Boot 3.0 alignment), subsequent major versions
- **Critical dependency:** Requires Spring Boot 3.0+ for version 5.0+
- **Latest stable:** v10.4.0 based on Spring Boot 3.5.5 (September 2025)
- **Future preview:** v11.0.0-rc.5 based on Spring Boot 4.0 RC2 (November 2025)

#### Key Compatibility Information

**Java 17 Support:** DGS 5.0.0+ (November 2022) with Spring Boot 3.0  
**Java 21 Support:** DGS 5.5.0+ (estimated, 2023) with Spring Boot 3.2+  
**Spring Boot Alignment:** DGS framework versions closely track Spring Boot versions

**Version Mapping:**
- DGS 4.x → Spring Boot 2.x (Java 11+)
- DGS 5.x-9.x → Spring Boot 3.0-3.4 (Java 17+)
- DGS 10.x → Spring Boot 3.5 (Java 17+, Java 21 compatible)
- DGS 11.x (preview) → Spring Boot 4.0 (Java 21+)

**DGS Codegen Plugin:**
- Current: 5.0.6 (in build.gradle)
- Target: Latest compatible with DGS framework version
- Code generation compatibility critical for build process

#### Breaking Changes (4.9.21 → 10.x)

**Major Breaking Changes:**

1. **Spring Boot 3.0 Migration (DGS 5.0.0)**
   - Requires Spring Boot 3.0+ baseline
   - Jakarta EE namespace migration (javax.* → jakarta.*)
   - Affects GraphQL context, servlet integration
   - Spring Framework 6.x compatibility

2. **DGS Framework API Changes**
   - `@DgsComponent` annotation remains stable
   - `@DgsData` annotation for data fetchers
   - `@DgsMutation` for GraphQL mutations
   - Context handling updates for Spring Boot 3.x

3. **Code Generation Changes**
   - DGS Codegen plugin must be updated in sync
   - Generated code structure may change
   - Type mapping updates
   - Client code generation compatibility

4. **GraphQL Schema Compatibility**
   - Schema definition language (SDL) remains compatible
   - Directive support updates
   - Scalar type handling
   - Federation support changes (if using GraphQL Federation)

5. **Spring GraphQL Integration**
   - DGS now builds on Spring for GraphQL (spring-graphql)
   - Shared infrastructure with Spring's GraphQL support
   - Observability and metrics integration
   - WebMVC and WebFlux support

**Configuration Changes:**

- Auto-configuration package relocations
- Property naming updates for Spring Boot 3.x
- GraphQL endpoint path configuration
- Subscription support configuration

**Data Fetcher Changes:**

- Context parameter types updated
- DataFetchingEnvironment usage remains similar
- DataLoader integration updates
- Error handling improvements

**Migration Path:**

- Cannot upgrade directly from 4.9.21 to 10.x without Spring Boot 3.0
- Must upgrade Spring Boot first, then DGS framework
- GraphQL schema files generally compatible (no syntax changes)
- Resolver code requires review for API changes

**Testing Considerations:**

- DGS test utilities updated for Spring Boot 3.x
- `@DgsTest` annotation compatibility
- GraphQL query testing framework updates
- Mock data fetcher support

### JJWT (Java JWT): 0.11.2 → Latest

**Current Version:** 0.11.2 (March 2021)  
**First Java 21 Compatible Version:** 0.12.1+ (October 2023, tested through JDK 21)  
**Latest Stable Version:** 0.13.0 (August 2025)  
**Minimum Java Version for Target:** Java 7 (0.11.x-0.13.x), Java 8 (0.14.0+)

#### Version Gap Analysis

- **Major versions to cross:** 0.11.2 → 0.12.x → 0.13.0
- **Breaking change releases:** 0.12.0 (major breaking changes)
- **Latest stable:** 0.13.0 (August 2025) - last version supporting Java 7
- **Future versions:** 0.14.0+ will require Java 8 minimum

#### Key Compatibility Information

**Java 21 Support:** 0.12.1+ (October 2023) - tested through JDK 21  
**Java 21 Compatibility:** 0.12.2 (October 2023) - fixed JPMS module issues for JDK 17+  
**Latest Version:** 0.13.0 (August 2025) - supports Java 7+ including Java 21

**Current Usage:**
- jjwt-api: 0.11.2
- jjwt-impl: 0.11.2 (runtime)
- jjwt-jackson: 0.11.2 (runtime)

**Target Usage:**
- jjwt-api: 0.13.0
- jjwt-impl: 0.13.0 (runtime)
- jjwt-jackson: 0.13.0 (runtime)

**Key Features in 0.12.x+:**
- Full JSON Web Encryption (JWE) support
- JSON Web Keys (JWK) support
- JSON Web Key Thumbprints
- Enhanced security features
- Improved GraalVM native image support
- Better JPMS module compatibility

#### Breaking Changes (0.11.2 → 0.13.0)

**Major Breaking Changes (0.12.0):**

**IMPORTANT:** Version 0.12.0 introduced significant breaking changes. The JJWT team explicitly states: "If you are not partial to fixing changes when upgrading a library, we strongly encourage you to wait until the 1.0 release."

1. **API Redesign**
   - Builder pattern changes for JWT creation
   - Parser builder changes for JWT validation
   - Claims handling updates
   - Key management API changes

2. **JWE Support Added**
   - New encryption capabilities
   - Key wrapping algorithms
   - Content encryption algorithms
   - Password-based encryption

3. **JWK Support Added**
   - JSON Web Key creation and parsing
   - JWK Set support
   - Key thumbprint generation
   - Key thumbprint URIs

4. **Security Enhancements**
   - Password-based JWE now enforces maximum iterations (DoS protection)
   - Jackson deserializer rejects duplicate JSON members by default
   - Improved cryptographic algorithm support
   - Enhanced key validation

**Configuration Changes:**

- Builder API changes require code updates
- Parser configuration updates
- Algorithm selection changes
- Key material handling updates

**Jackson Integration:**

- Updated Jackson dependency versions
- Custom ObjectMapper support improved
- Claim type mapping enhancements
- Serialization/deserialization improvements

**Spring Security Integration:**

- No direct Spring Security dependency
- Works with Spring Security 5.x and 6.x
- JWT filter integration remains similar
- Token validation logic compatible

**Migration Considerations:**

1. **Code Changes Required:**
   - Update JWT builder usage
   - Update parser builder usage
   - Review claims handling
   - Update key management code

2. **Testing Required:**
   - Verify token generation
   - Verify token validation
   - Test existing tokens still validate
   - Test Spring Security integration

3. **Backward Compatibility:**
   - Tokens generated by 0.11.x can be validated by 0.12.x+
   - Tokens generated by 0.12.x+ can be validated by 0.11.x (if using compatible features)
   - No token format changes for basic JWS

**Specific Changes for Our Application:**

**Current Implementation:**
- JWT token generation in `DefaultJwtService.java`
- JWT validation in `JwtTokenFilter.java`
- Spring Security integration in `WebSecurityConfig.java`

**Required Updates:**

1. **Token Generation:**
   - Update `Jwts.builder()` usage
   - Claims setting may have API changes
   - Signature algorithm configuration updates

2. **Token Validation:**
   - Update `Jwts.parserBuilder()` usage (was `Jwts.parser()` in older versions)
   - Key resolution updates
   - Claims extraction updates

3. **Spring Security Filter:**
   - Filter logic remains similar
   - Token extraction from headers unchanged
   - Security context population unchanged

**Example Migration (Simplified):**

```java
// Before (0.11.x)
String jwt = Jwts.builder()
    .setSubject(user.getId())
    .setExpiration(expiryDate)
    .signWith(SignatureAlgorithm.HS512, secret)
    .compact();

// After (0.12.x+)
String jwt = Jwts.builder()
    .subject(user.getId())
    .expiration(expiryDate)
    .signWith(key, Jwts.SIG.HS512)
    .compact();
```

**Risk Assessment:**

- **Risk Level:** MEDIUM
- **Impact:** Authentication system changes
- **Mitigation:** Thorough testing, staged rollout
- **Rollback:** Existing tokens remain valid

**Additional Notes:**

- JJWT 0.12.2 fixed JPMS module issues for JDK 17+ (no `--add-opens` flags needed)
- JJWT 0.12.7 improved GraalVM native image support
- JJWT 0.13.0 is the last version supporting Java 7
- Future 0.14.0+ will require Java 8 minimum
- Library is actively maintained with regular security updates

---

## Breaking Changes Analysis

### High-Impact Changes Requiring Code Modifications

#### 1. Jakarta EE Namespace Migration (Spring Boot 3.0)

**Scope:** Every file using Java EE APIs  
**Effort:** High (automated tools available)  
**Risk:** Medium (mechanical change, but extensive)

**Files Affected:**
- All controller classes (`@RestController`, `@RequestMapping`)
- All entity classes (`@Entity`, `@Table`, `@Column`)
- Security configuration (`@Configuration`, servlet filters)
- Validation annotations (`@Valid`, `@NotNull`, etc.)
- Transaction management (`@Transactional`)

**Migration Strategy:**
- Use OpenRewrite or Spring Boot Migrator tools for automated migration
- Manual review of all changes
- Test thoroughly after migration

**Example Changes:**
```java
// Before (Java EE)
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.persistence.Entity;

// After (Jakarta EE)
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.persistence.Entity;
```

#### 2. Spring Security Configuration Overhaul (Spring Security 6.0)

**Scope:** `WebSecurityConfig.java` and related security classes  
**Effort:** High  
**Risk:** High (security-critical code)

**Current Implementation:**
- Uses `WebSecurityConfigurerAdapter` (deprecated and removed in 6.0)
- JWT authentication filter
- CORS configuration
- Security filter chain

**Required Changes:**
- Rewrite security configuration using component-based approach
- Update JWT filter integration
- Migrate to new `SecurityFilterChain` bean pattern
- Update authentication manager configuration
- Review and update authorization rules

**Example Migration:**
```java
// Before (Spring Security 5.x)
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/users/login").permitAll()
            .anyRequest().authenticated();
    }
}

// After (Spring Security 6.x)
@Configuration
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/login").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

#### 3. MyBatis Mapper Configuration

**Scope:** MyBatis mapper XML files and configuration  
**Effort:** Low to Medium  
**Risk:** Low

**Current Implementation:**
- Distributed mapper XML files in `src/main/resources/mapper/`
- MyBatis configuration in `mybatis-config.xml`
- Type handlers for Joda-Time

**Required Changes:**
- Update MyBatis configuration properties structure
- Verify type handler compatibility
- Test all mapper queries with new MyBatis version
- Update transaction management if needed

#### 4. GraphQL Schema and Resolvers (Netflix DGS)

**Scope:** GraphQL schema files and resolver classes  
**Effort:** Medium (pending version research)  
**Risk:** Medium

**Current Implementation:**
- GraphQL schema in `src/main/resources/schema/`
- DGS code generation in build process
- GraphQL resolvers and mutations

**Required Changes:**
- Update DGS framework version
- Update DGS Codegen plugin
- Verify schema compatibility
- Update resolver annotations if changed
- Test GraphQL queries and mutations

#### 5. Build Configuration Updates

**Scope:** `build.gradle` and CI/CD configuration  
**Effort:** Medium  
**Risk:** Low

**Required Changes:**

**build.gradle:**
- Update `sourceCompatibility` and `targetCompatibility` to 17 or 21
- Update Spring Boot plugin version
- Update dependency management plugin
- Update all dependency versions
- Add `-parameters` compiler flag for Spring Framework 6.x
- Update DGS Codegen plugin version
- Update Spotless plugin if needed
- Update JaCoCo plugin if needed

**CI/CD (.github/workflows/gradle.yml):**
- Update Java version from 11 to 21
- Update actions/setup-java configuration
- Verify Gradle wrapper compatibility
- Update any Java version-specific build flags

#### 6. Lombok Compatibility

**Scope:** All classes using Lombok annotations  
**Effort:** Low  
**Risk:** Low

**Current Usage:**
- `@Data`, `@Getter`, `@Setter`, `@Builder`, etc.
- Used extensively for DTOs and entities

**Required Changes:**
- Verify Lombok version compatibility with Java 21
- Update Lombok version if needed
- Consider migration to Java Records for some DTOs (optional)

**Java Records Consideration:**
- Java 21 includes mature Records feature
- Could replace some Lombok `@Data` classes
- Immutable by default (good for DTOs)
- Decision: Evaluate on case-by-case basis

---

## Recommended Upgrade Path

### Strategy: Incremental Upgrade with Intermediate Steps

**Rationale:** The Spring Boot 2.x → 3.x migration is complex enough that attempting a direct Java 11 → 21 jump is risky. An intermediate step through Java 17 LTS provides a safer migration path.

### Phase 1: Upgrade to Java 17 + Spring Boot 3.0

**Goal:** Establish Java 17 baseline and complete Jakarta EE migration

**Steps:**

1. **Update Java Version to 17**
   - Update `build.gradle`: `sourceCompatibility = '17'`, `targetCompatibility = '17'`
   - Update CI/CD: Change JDK version to 17
   - Add `-parameters` compiler flag

2. **Upgrade Spring Boot 2.6.3 → 2.7.x**
   - Intermediate step to latest 2.x version
   - Address any deprecation warnings
   - Test thoroughly

3. **Upgrade Spring Boot 2.7.x → 3.0.x**
   - Update Spring Boot version in build.gradle
   - Run automated migration tools (OpenRewrite/Spring Boot Migrator)
   - Perform Jakarta EE namespace migration (javax.* → jakarta.*)
   - Rewrite Spring Security configuration
   - Update all affected imports and annotations
   - Fix compilation errors
   - Run tests and fix failures

4. **Upgrade MyBatis 2.2.2 → 3.0.x**
   - Update MyBatis Spring Boot Starter version
   - Update configuration properties
   - Test all database operations
   - Verify mapper functionality

5. **Upgrade Netflix DGS to Spring Boot 3.0 compatible version**
   - Research and identify compatible DGS version
   - Update DGS framework and codegen plugin
   - Regenerate GraphQL code
   - Test GraphQL endpoints

6. **Upgrade JJWT to latest version**
   - Research latest JJWT version
   - Update dependency versions
   - Test JWT token generation and validation
   - Verify Spring Security integration

7. **Testing and Validation**
   - Run full test suite
   - Manual testing of all endpoints (REST and GraphQL)
   - Security testing
   - Database migration testing
   - Performance testing

### Phase 2: Upgrade to Java 21 + Spring Boot 3.2+

**Goal:** Enable Java 21 features including virtual threads

**Steps:**

1. **Upgrade Spring Boot 3.0.x → 3.2.x**
   - Update Spring Boot version
   - Address any breaking changes
   - Test thoroughly

2. **Update Java Version to 21**
   - Update `build.gradle`: `sourceCompatibility = '21'`, `targetCompatibility = '21'`
   - Update CI/CD: Change JDK version to 21
   - Test with Java 21 runtime

3. **Upgrade MyBatis to 3.0.3+**
   - Update to Spring Boot 3.2 compatible version
   - Test all functionality

4. **Upgrade Netflix DGS to latest**
   - Update to latest Java 21 compatible version
   - Test GraphQL functionality

5. **Upgrade JJWT to latest**
   - Ensure Java 21 compatibility
   - Test JWT functionality

6. **Enable Virtual Threads (Optional)**
   - Add `spring.threads.virtual.enabled=true` to configuration
   - Test application behavior with virtual threads
   - Monitor performance improvements

7. **Final Testing and Validation**
   - Comprehensive testing
   - Performance benchmarking
   - Security audit
   - Documentation updates

### Alternative: Direct Java 11 → 21 Upgrade

**Pros:**
- Fewer intermediate steps
- Faster timeline if successful

**Cons:**
- Higher risk of issues
- More difficult debugging
- Larger scope of changes at once
- Harder to isolate problems

**Recommendation:** NOT recommended due to complexity and risk

---

## Risk Assessment

### High-Risk Areas

#### 1. Spring Security Configuration Migration

**Risk Level:** HIGH  
**Impact:** Security vulnerabilities if misconfigured  
**Mitigation:**
- Thorough security testing
- Code review by security-aware developers
- Penetration testing after migration
- Gradual rollout with monitoring

#### 2. Jakarta EE Namespace Migration

**Risk Level:** MEDIUM  
**Impact:** Runtime errors if any imports missed  
**Mitigation:**
- Use automated migration tools
- Comprehensive test coverage
- Manual code review
- Staged deployment

#### 3. Database Layer Changes

**Risk Level:** MEDIUM  
**Impact:** Data corruption or loss if MyBatis changes break queries  
**Mitigation:**
- Extensive database testing
- Backup before migration
- Test migrations in staging environment
- Rollback plan

#### 4. GraphQL API Breaking Changes

**Risk Level:** MEDIUM  
**Impact:** Client applications may break  
**Mitigation:**
- API contract testing
- Client notification
- Versioned API if needed
- Backward compatibility testing

### Medium-Risk Areas

#### 1. JWT Token Compatibility

**Risk Level:** MEDIUM  
**Impact:** Authentication failures  
**Mitigation:**
- Test token generation and validation
- Verify existing tokens still work
- Plan for token refresh if needed

#### 2. Build Process Changes

**Risk Level:** LOW-MEDIUM  
**Impact:** CI/CD failures  
**Mitigation:**
- Test build locally first
- Update CI/CD incrementally
- Maintain rollback capability

### Low-Risk Areas

#### 1. Lombok Compatibility

**Risk Level:** LOW  
**Impact:** Compilation errors (easily fixed)  
**Mitigation:**
- Update Lombok version
- Test compilation

#### 2. SQLite Driver Compatibility

**Risk Level:** LOW  
**Impact:** Database connection issues  
**Mitigation:**
- Verify driver version compatibility
- Test database operations

---

## Timeline Estimates

### Phase 1: Java 17 + Spring Boot 3.0 Migration

| Task | Estimated Effort | Dependencies |
|------|-----------------|--------------|
| Environment setup (Java 17, build config) | 0.5 days | None |
| Spring Boot 2.6 → 2.7 upgrade | 1 day | Environment setup |
| Jakarta EE namespace migration (automated) | 1 day | Spring Boot 2.7 |
| Spring Security configuration rewrite | 2-3 days | Jakarta migration |
| Spring Boot 2.7 → 3.0 upgrade | 1 day | Security config |
| MyBatis upgrade and testing | 1-2 days | Spring Boot 3.0 |
| Netflix DGS upgrade and testing | 2-3 days | Spring Boot 3.0 |
| JJWT upgrade and testing | 1 day | Spring Boot 3.0 |
| Integration testing | 2-3 days | All upgrades |
| Bug fixes and refinement | 2-3 days | Testing |
| **Phase 1 Total** | **14-19 days** | |

### Phase 2: Java 21 + Spring Boot 3.2 Migration

| Task | Estimated Effort | Dependencies |
|------|-----------------|--------------|
| Spring Boot 3.0 → 3.2 upgrade | 1 day | Phase 1 complete |
| Java 17 → 21 upgrade | 0.5 days | Spring Boot 3.2 |
| Dependency updates (MyBatis, DGS, JJWT) | 1-2 days | Java 21 |
| Virtual threads testing (optional) | 1 day | Java 21 |
| Integration testing | 2 days | All upgrades |
| Performance testing | 1 day | Testing |
| Bug fixes and refinement | 1-2 days | Testing |
| **Phase 2 Total** | **7.5-10.5 days** | |

### Total Estimated Timeline

**Conservative Estimate:** 22-30 working days (4.5-6 weeks)  
**Optimistic Estimate:** 18-22 working days (3.5-4.5 weeks)  
**Recommended Buffer:** +20% for unforeseen issues

**Total with Buffer:** 5-7 weeks

### Effort Breakdown by Role

- **Backend Development:** 70% (Spring Boot, MyBatis, Security)
- **API Development:** 20% (GraphQL, REST endpoints)
- **DevOps/Build:** 5% (CI/CD, build configuration)
- **Testing/QA:** 5% (distributed across all tasks)

---

## Open Questions and Recommendations

### Critical Questions Addressed

#### 1. What format should the final compatibility documentation take?

**Answer:** This Markdown document serves as the comprehensive compatibility assessment. It should be:
- Stored in version control
- Referenced in the implementation tickets
- Updated as migration progresses
- Used as a reference during code reviews

#### 2. Should we consider intermediate Java versions (17 LTS) as stepping stones?

**Answer:** YES - STRONGLY RECOMMENDED

**Rationale:**
- Java 17 is required for Spring Boot 3.0
- Allows testing Spring Boot 3.0 migration separately from Java 21 features
- Reduces risk by breaking migration into smaller steps
- Java 17 is LTS (Long Term Support) providing a stable intermediate target
- Easier debugging when issues arise

#### 3. What are the specific jakarta.* namespace impacts on our current Spring Security + JWT setup?

**Answer:** SIGNIFICANT IMPACT

**Affected Areas:**
- All servlet-related imports (HttpServletRequest, HttpServletResponse, FilterChain)
- Security filter implementations
- JWT authentication filter
- CORS configuration
- Exception handling in security layer

**Specific Files:**
- `src/main/java/io/spring/infrastructure/security/JwtTokenFilter.java`
- `src/main/java/io/spring/infrastructure/security/WebSecurityConfig.java`
- Any custom authentication/authorization filters

**Migration Required:**
- Update all `javax.servlet.*` → `jakarta.servlet.*`
- Update Spring Security configuration to 6.x patterns
- Rewrite `WebSecurityConfigurerAdapter` usage
- Update JWT filter integration

#### 4. How will the dual API surface (REST + GraphQL) complicate the Spring Boot upgrade?

**Answer:** MODERATE COMPLEXITY INCREASE

**Challenges:**
1. **Two API Layers to Test:**
   - REST endpoints (Spring MVC)
   - GraphQL endpoints (Netflix DGS)
   - Both must work correctly after migration

2. **Security Integration:**
   - JWT authentication must work for both APIs
   - CORS configuration affects both
   - Authorization rules apply to both

3. **Dependency Coordination:**
   - DGS framework must be compatible with Spring Boot version
   - GraphQL schema validation
   - Code generation compatibility

**Mitigation:**
- Test both API surfaces thoroughly
- Maintain API contract tests
- Update DGS framework in sync with Spring Boot
- Consider API versioning if breaking changes needed

#### 5. What are the implications for our CQRS pattern implementation with newer framework versions?

**Answer:** MINIMAL DIRECT IMPACT

**Analysis:**
- CQRS pattern is application-level architecture
- Framework changes don't fundamentally alter CQRS approach
- Command and Query services remain separate

**Potential Impacts:**
- Transaction management updates (Spring 6.x)
- Repository interface changes (Spring Data updates)
- Event handling if using Spring Events

**Recommendation:**
- CQRS pattern can remain unchanged
- Focus on updating infrastructure (repositories, transactions)
- Verify command/query separation still works correctly

#### 6. Are there any show-stopping compatibility issues that would block Java 21 migration?

**Answer:** NO SHOW-STOPPERS IDENTIFIED

**Assessment:**
- All major dependencies have Java 21 compatible versions
- Spring Boot 3.2+ officially supports Java 21
- MyBatis, DGS, and JJWT have compatible versions available
- SQLite JDBC driver supports Java 21

**Caveats:**
- Netflix DGS and JJWT versions need final verification (research in progress)
- No known technical blockers
- Main challenge is migration effort, not compatibility

### Additional Recommendations

#### 1. Lombok vs. Java Records Decision

**Recommendation:** Hybrid Approach

- **Keep Lombok for:**
  - Entities with mutable state
  - Classes requiring `@Builder` with many fields
  - Classes needing `@Getter`/`@Setter` separately

- **Consider Records for:**
  - Immutable DTOs (UserData, ArticleData, etc.)
  - API request/response objects
  - Event objects

**Rationale:**
- Records provide immutability by default
- Better semantic meaning for data transfer objects
- No additional dependency needed
- Can coexist with Lombok

**Implementation:**
- Evaluate during migration, not as primary goal
- Convert opportunistically when touching code
- Don't force conversion of working code

#### 2. Testing Strategy

**Recommendation:** Comprehensive Multi-Layer Testing

1. **Unit Tests:**
   - Run existing test suite at each migration step
   - Add tests for new Spring Security configuration
   - Test JWT token handling

2. **Integration Tests:**
   - Test REST API endpoints
   - Test GraphQL API endpoints
   - Test database operations
   - Test authentication/authorization

3. **Manual Testing:**
   - User registration and login
   - Article CRUD operations
   - Following/favoriting functionality
   - Comment functionality

4. **Performance Testing:**
   - Baseline performance on Java 11
   - Compare performance on Java 17
   - Compare performance on Java 21
   - Test virtual threads impact (Java 21)

#### 3. Rollback Strategy

**Recommendation:** Maintain Rollback Capability

- Use feature branches for migration work
- Tag stable points in migration
- Maintain ability to rollback to Java 11 version
- Document rollback procedures
- Test rollback process

#### 4. Documentation Updates

**Files Requiring Updates:**

1. **README.md**
   - Update Java version requirements
   - Update installation instructions
   - Update development setup

2. **Build Documentation**
   - Update Gradle version requirements
   - Update build commands
   - Update IDE setup instructions

3. **Deployment Documentation**
   - Update runtime requirements
   - Update Docker base images if used
   - Update environment variables

#### 5. Monitoring and Observability

**Recommendation:** Enhanced Monitoring for Migration

- Enable Spring Boot Actuator metrics
- Monitor application performance
- Track error rates during rollout
- Use Spring Boot 3.2 observability features
- Consider enabling virtual threads monitoring

---

## Next Steps

### Immediate Actions

1. **Complete Remaining Research**
   - Finalize Netflix DGS version compatibility research
   - Finalize JJWT version compatibility research
   - Document specific version recommendations

2. **Stakeholder Review**
   - Present this assessment to team
   - Get approval for recommended approach
   - Discuss timeline and resource allocation

3. **Create Implementation Tickets**
   - Break down Phase 1 into detailed tickets
   - Assign effort estimates
   - Prioritize tickets
   - Create Phase 2 tickets

4. **Environment Preparation**
   - Set up Java 17 development environment
   - Set up Java 21 development environment
   - Configure IDE for new Java versions
   - Update local build tools

### Implementation Readiness Checklist

- [ ] All dependency versions researched and documented
- [ ] Migration path defined and approved
- [ ] Timeline estimated and approved
- [ ] Resources allocated
- [ ] Development environment prepared
- [ ] Testing strategy defined
- [ ] Rollback plan documented
- [ ] Stakeholder approval obtained
- [ ] Implementation tickets created
- [ ] Team trained on new features (virtual threads, records, etc.)

---

## Appendix A: Dependency Version Summary

### Target Versions for Java 21 Migration

| Dependency | Current | Target (Phase 1 - Java 17) | Target (Phase 2 - Java 21) |
|------------|---------|---------------------------|---------------------------|
| Java | 11 | 17 | 21 |
| Spring Boot | 2.6.3 | 3.0.x | 3.2.x or 3.5.x |
| MyBatis Spring Boot | 2.2.2 | 3.0.x | 3.0.3+ or 3.0.5 |
| Netflix DGS | 4.9.21 | 5.0.x+ | 10.4.0 or latest |
| JJWT | 0.11.2 | 0.12.x or 0.13.0 | 0.13.0 |
| Spring Framework | 5.3.x | 6.0.x | 6.1.x |
| Spring Security | 5.6.x | 6.0.x | 6.2.x |
| Hibernate | 5.6.x | 6.1.x | 6.3.x |

### Build Tool Versions

| Tool | Current | Target |
|------|---------|--------|
| Gradle | 7.x | 8.x |
| Spotless Plugin | 6.2.1 | Latest compatible |
| DGS Codegen Plugin | 5.0.6 | TBD |
| JaCoCo | 0.8.7 | Latest compatible |

---

## Appendix B: Reference Links

### Official Documentation

- [Spring Boot 3.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Release-Notes)
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Boot 3.2 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)
- [Spring Framework 6.0 What's New](https://github.com/spring-projects/spring-framework/wiki/What's-New-in-Spring-Framework-6.x)
- [Spring Security 6.0 Migration Guide](https://docs.spring.io/spring-security/reference/migration/index.html)
- [MyBatis Spring Boot Releases](https://github.com/mybatis/spring-boot-starter/releases)
- [Java 21 Features](https://openjdk.org/projects/jdk/21/)
- [Virtual Threads (JEP 444)](https://openjdk.org/jeps/444)

### Migration Tools

- [OpenRewrite](https://docs.openrewrite.org/)
- [Spring Boot Migrator](https://github.com/spring-projects-experimental/spring-boot-migrator)

---

## Document Revision History

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 (Draft) | 2025-11-11 | Devin | Initial compatibility assessment |

---

**END OF DOCUMENT**

*Note: This document will be updated as additional research is completed for Netflix DGS and JJWT dependencies.*
