# Java 21 Compatibility Assessment & Migration Plan
**Ticket**: JP-9  
**Date**: October 7, 2025  
**Current Java Version**: Java 11  
**Target Java Version**: Java 21 LTS  
**Repository**: mbatchelor81/spring-boot-realworld-example-app

---

## Executive Summary

This document provides a comprehensive compatibility assessment for migrating the Spring Boot RealWorld application from **Java 11 to Java 21**. The migration requires upgrading all four primary dependencies to versions compatible with Java 21, with **Spring Boot 2.6.3 → 3.2+** being the most critical upgrade path.

### Key Findings

✅ **All dependencies support Java 21** with appropriate version upgrades  
⚠️ **Major breaking changes** due to Jakarta EE namespace migration (javax.* → jakarta.*)  
⚠️ **Significant version gaps** across all four primary dependencies  
🔄 **Recommended approach**: Incremental migration via Java 17 LTS stepping stone

### Recommended Migration Strategy

1. **Upgrade to Java 17 LTS** with Spring Boot 3.0/3.1 (intermediate step)
2. **Complete Jakarta EE migration** and resolve all namespace breaking changes
3. **Upgrade to Java 21** with Spring Boot 3.2+ (final step)
4. **Adopt virtual threads** and other Java 21 features (optimization)

**Estimated Total Effort**: 4-6 weeks for full migration with testing

---

## Current State Inventory

### Runtime Environment
| Component | Current Version | Configuration File |
|-----------|----------------|-------------------|
| Java Version | 11 | `build.gradle` lines 11-12 |
| CI/CD Java | 11 (Zulu distribution) | `.github/workflows/gradle.yml` line 24 |
| Gradle Wrapper | 7.x (compatible) | Wrapper files |

### Primary Dependencies

#### 1. Spring Boot Framework
- **Current Version**: 2.6.3
- **Release Date**: January 20, 2022
- **Age**: ~3.75 years old
- **Configuration**: `build.gradle` line 2
- **Included Starters**:
  - `spring-boot-starter-web`
  - `spring-boot-starter-validation`
  - `spring-boot-starter-hateoas`
  - `spring-boot-starter-security`

#### 2. MyBatis Spring Boot Starter
- **Current Version**: 2.2.2
- **Release Date**: January 2022
- **Age**: ~3.75 years old
- **Configuration**: `build.gradle` line 63
- **Usage**: Data Mapper pattern for SQLite persistence

#### 3. Netflix DGS (GraphQL Framework)
- **Current Version**: 4.9.21
- **Release Date**: May 2022
- **Age**: ~3.5 years old
- **Configuration**: `build.gradle` line 64
- **Codegen Plugin**: 5.0.6 (`build.gradle` line 5)
- **Usage**: GraphQL API implementation alongside REST

#### 4. JJWT (JSON Web Token)
- **Current Version**: 0.11.2
- **Release Date**: 2020
- **Age**: ~5 years old
- **Configuration**: `build.gradle` lines 66-68 (api, impl, jackson)
- **Usage**: JWT authentication in Spring Security filters

### Supporting Dependencies
| Dependency | Current Version | Purpose |
|------------|----------------|---------|
| Flyway | (managed by Spring Boot) | Database migrations |
| Joda-Time | 2.10.13 | Date/time handling |
| SQLite JDBC | 3.36.0.3 | Database driver |
| Lombok | (compileOnly) | Boilerplate reduction |
| JaCoCo | 0.8.7 | Code coverage |
| Spotless | 6.2.1 | Code formatting |
| Rest-Assured | 4.5.1 | API testing |

---

## Java 21 Compatibility Matrix

### Primary Dependencies Analysis

| Dependency | Current | Target for Java 21 | First Java 21 Version | Version Gap | Breaking Changes |
|------------|---------|-------------------|----------------------|-------------|------------------|
| **Spring Boot** | 2.6.3 | 3.2.0+ | 3.2.0 (Nov 2023) | 6 minor versions | ⚠️ MAJOR |
| **MyBatis Starter** | 2.2.2 | 3.0.3+ | 3.0.0 (Nov 2022) | 1 major version | ⚠️ MAJOR |
| **Netflix DGS** | 4.9.21 | 7.0.0+ (Java 17), 8.0.0+ (Java 21 recommended) | 7.0.0 (May 2023) | 3 major versions | ⚠️ MAJOR |
| **JJWT** | 0.11.2 | 0.12.0+ | 0.12.0 (Oct 2023) | 1 minor version | ⚠️ MODERATE |

### Detailed Compatibility Research

#### Spring Boot 2.6.3 → 3.2+

**Spring Boot 3.0 Baseline Requirements**:
- ✅ **Minimum Java Version**: Java 17 (Java 8-16 no longer supported)
- ✅ **Spring Framework**: Upgraded to 6.0
- ✅ **Jakarta EE**: Migrated from Java EE (javax.* → jakarta.*)
- ✅ **Hibernate**: Upgraded to 6.1

**Spring Boot 3.2 Java 21 Features**:
- ✅ **Full Java 21 Support**: Released November 2023
- ✅ **Virtual Threads**: Enable with `spring.threads.virtual.enabled=true`
- ✅ **JDK 21 LTS**: Official support and testing

**Release Timeline**:
- Spring Boot 2.6.3: January 2022
- Spring Boot 3.0.0: November 2022 (Java 17+ required)
- Spring Boot 3.2.0: November 2023 (Java 21 support added)
- Spring Boot 3.5.6: Current latest (October 2025)

#### MyBatis Spring Boot Starter 2.2.2 → 3.0.3+

**MyBatis 3.0 Migration**:
- ✅ **Spring Boot 3.0+ Compatibility**: First version supporting Spring Boot 3.x
- ✅ **Jakarta Namespace**: Uses jakarta.* packages
- ✅ **Java 17+ Required**: Follows Spring Boot 3.0 baseline
- ✅ **Java 21 Compatible**: Works with Spring Boot 3.2+

**Release Timeline**:
- MyBatis Spring Boot 2.2.2: January 2022 (Spring Boot 2.6.x)
- MyBatis Spring Boot 3.0.0: November 2022 (Spring Boot 3.0+)
- MyBatis Spring Boot 3.0.3: November 2023 (Spring Boot 3.2+)

**Key Changes**:
- Mapper XML namespaces remain compatible
- TypeHandlers work with jakarta.* types
- Transaction management via Spring Boot 3.x

#### Netflix DGS 4.9.21 → 7.0.0+

**DGS 7.0 Major Upgrade**:
- ✅ **Spring Boot 3.0+ Required**: First version supporting Spring Boot 3.x
- ✅ **GraphQL Java**: Upgraded from 19.x to 20.2
- ✅ **Jakarta Migration**: Full jakarta.* namespace support
- ✅ **Java 17+ Required**: Follows Spring Boot 3.0 baseline

**DGS Evolution Timeline**:
- DGS 4.9.21: May 2022 (Spring Boot 2.x)
- DGS 7.0.0: May 2023 (Spring Boot 3.0+, Java 17+)
- DGS 7.3.0: July 2023 (Spring Boot 3.0.8)
- DGS 10.2.0: June 2025 (Spring Boot 3.5, Java 21 fully tested)

**Codegen Plugin Compatibility**:
- Current: 5.0.6
- Target: 7.0.0+ (aligned with DGS framework version)
- Breaking: Schema generation APIs changed

#### JJWT 0.11.2 → 0.12.0+

**JJWT 0.12 Series**:
- ✅ **Java 8+ Required**: Baseline unchanged
- ✅ **Java 21 Compatible**: Fully tested and supported
- ✅ **API Improvements**: Enhanced builder patterns
- ✅ **Jakarta Support**: Works with both javax.* and jakarta.*

**Release Timeline**:
- JJWT 0.11.2: 2020
- JJWT 0.12.0: October 2023 (Java 21 tested)
- JJWT 0.13.0: August 2025 (latest, final Java 7 support)

**Migration Impact**:
- ⚠️ Minimal breaking changes for basic JWT operations
- ⚠️ Some deprecated methods removed
- ✅ Backward compatible API design

### Supporting Dependencies

#### Lombok
- **Current**: compileOnly (version managed by IDE)
- **Java 21 Support**: Lombok 1.18.30+ (October 2023)
- **Consideration**: Evaluate migration to Java Records for immutable DTOs
- **Impact**: LOW - Lombok continues to work with Java 21

#### Flyway
- **Current**: Managed by Spring Boot 2.6.3
- **Java 21 Support**: Flyway 9.0+ (included in Spring Boot 3.2+)
- **Impact**: LOW - Migration scripts remain compatible

#### Joda-Time
- **Current**: 2.10.13
- **Java 21 Support**: 2.12.5+ (fully compatible)
- **Consideration**: Migrate to java.time.* API (preferred in Java 8+)
- **Impact**: MODERATE - Refactoring recommended but not required

#### SQLite JDBC
- **Current**: 3.36.0.3
- **Java 21 Support**: 3.40.0+ (fully compatible)
- **Impact**: LOW - Driver upgrade straightforward

---

## Breaking Changes Analysis

### Critical Breaking Change: Jakarta EE Namespace Migration

**Impact Scope**: Spring Boot 2.x → 3.x migration requires replacing all `javax.*` imports with `jakarta.*`

#### Affected Areas in Codebase

**1. Spring Security Components** (HIGH IMPACT)
```
Current Imports (javax.*):
- javax.servlet.FilterChain
- javax.servlet.ServletException
- javax.servlet.http.HttpServletRequest
- javax.servlet.http.HttpServletResponse

Required New Imports (jakarta.*):
- jakarta.servlet.FilterChain
- jakarta.servlet.ServletException
- jakarta.servlet.http.HttpServletRequest
- jakarta.servlet.http.HttpServletResponse
```

**Files Requiring Updates**:
- `src/main/java/io/spring/api/security/JwtTokenFilter.java` - Authentication filter
- `src/main/java/io/spring/infrastructure/security/WebSecurityConfig.java` - Security configuration
- All controller classes using servlet annotations

**2. Validation Annotations** (MODERATE IMPACT)
```
Current (javax.validation.*):
- javax.validation.constraints.Email
- javax.validation.constraints.NotBlank
- javax.validation.Valid

Required (jakarta.validation.*):
- jakarta.validation.constraints.Email
- jakarta.validation.constraints.NotBlank
- jakarta.validation.Valid
```

**Files Requiring Updates**:
- All DTO classes with validation annotations (`@NotBlank`, `@Email`, etc.)
- `src/main/java/io/spring/api/*/` - Request/Response DTOs

**3. Persistence Annotations** (MODERATE IMPACT)
```
Current (javax.persistence.*):
- javax.persistence.EntityManager (if used)

Required (jakarta.persistence.*):
- jakarta.persistence.EntityManager
```

**Note**: This codebase uses MyBatis (not JPA), so direct JPA usage is minimal.

### Spring Boot 3.0 Breaking Changes

#### Configuration Property Changes
- **Deprecated Properties**: Many `spring.*` properties renamed or removed
- **Security**: Default security configuration more restrictive
- **Web**: `spring.mvc.*` properties restructured

#### Dependency Management Changes
- **Version Catalogs**: New dependency management approach
- **Minimum Versions**: Enforced minimum versions for transitives
- **Removed Starters**: Some deprecated starters removed

### Netflix DGS Breaking Changes

#### DGS 4.x → 7.x Migration

**1. GraphQL Schema Processing**
- **Schema Location**: Default location may have changed
- **Codegen Output**: Generated types package structure updated
- **Type Mappings**: Custom scalar mappings require updates

**2. Data Fetchers**
- **Annotations**: `@DgsComponent` usage unchanged
- **Context**: `DgsRequestData` API changes
- **Errors**: Exception handling mechanism updated

**3. Code Generation Plugin**
- **Build Configuration**: Plugin configuration syntax changed
- **Generated Files**: Output directory structure modified
- **Type Mapping**: Custom type mapping configuration updated

**Files Requiring Review**:
- `build.gradle` lines 96-99 (generateJava task configuration)
- `src/main/resources/schema/schema.graphqls` (schema compatibility)
- `src/main/java/io/spring/graphql/` (all resolvers and mutations)

### MyBatis Breaking Changes

#### MyBatis 2.x → 3.x Migration

**1. Configuration**
- **Auto-configuration**: Property names may have changed
- **Type Handlers**: Registration process updated for jakarta.*
- **Mapper Scanning**: Compatible with Spring Boot 3.x component scanning

**2. XML Mappers**
- **DTD Declaration**: May require updated DOCTYPE
- **Result Maps**: Type references must use jakarta.* for any Java EE types
- **Parameters**: Java types in parameterType/resultType annotations

**Files Requiring Review**:
- `src/main/resources/mapper/*.xml` (all MyBatis mapper files)
- `src/main/resources/mybatis-config.xml` (global configuration)
- All repository interfaces in `src/main/java/io/spring/infrastructure/mybatis/`

### JJWT Breaking Changes

#### JJWT 0.11.x → 0.12.x Migration

**1. API Changes**
- **Deprecated Methods**: Some builder methods removed
- **Key Types**: Enhanced type safety in key builders
- **Claims**: Claims API slightly refined

**2. Security Updates**
- **Algorithms**: Deprecated algorithms removed
- **Key Sizes**: Enforced minimum key sizes
- **Validation**: Stricter validation by default

**Files Requiring Review**:
- `src/main/java/io/spring/infrastructure/service/DefaultJwtService.java`
- Any JWT token generation/validation code
- Test files validating JWT behavior

### Build Configuration Changes

#### build.gradle Updates Required

**1. Java Version**
```gradle
// Current
sourceCompatibility = '11'
targetCompatibility = '11'

// Target for Java 21
sourceCompatibility = '21'
targetCompatibility = '21'
```

**2. Spring Boot Plugin**
```gradle
// Current
id 'org.springframework.boot' version '2.6.3'
id 'io.spring.dependency-management' version '1.0.11.RELEASE'

// Target for Java 21
id 'org.springframework.boot' version '3.2.0' // or later
id 'io.spring.dependency-management' version '1.1.0' // or later
```

**3. Netflix DGS Plugin**
```gradle
// Current
id "com.netflix.dgs.codegen" version "5.0.6"

// Target for Java 21
id "com.netflix.dgs.codegen" version "7.0.0" // or later
```

**4. Dependency Versions**
```gradle
// Current
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:2.2.2'
implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:4.9.21'
implementation 'io.jsonwebtoken:jjwt-api:0.11.2'

// Target for Java 21
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
implementation 'com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter:7.6.0' // or 8.x for latest
implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
```

**5. JaCoCo Version**
```gradle
// Current
toolVersion = "0.8.7"

// Target for Java 21
toolVersion = "0.8.11" // Java 21 compatible
```

### CI/CD Configuration Changes

#### .github/workflows/gradle.yml

```yaml
# Current
- name: Set up JDK 11
  uses: actions/setup-java@v4
  with:
    distribution: zulu
    java-version: '11'

# Target for Java 21
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    distribution: zulu
    java-version: '21'
```

---

## Recommended Upgrade Path

### Migration Strategy Decision: Incremental vs. Direct

#### Option A: Direct Migration (Java 11 → 21)
**Approach**: Single upgrade jumping directly to Java 21 and all latest dependencies

**Pros**:
- ✅ Fewer migration cycles
- ✅ Single comprehensive testing phase
- ✅ Faster to production if successful

**Cons**:
- ❌ Higher risk of unforeseen issues
- ❌ Difficult to isolate root causes
- ❌ All breaking changes hit at once
- ❌ Rollback requires reverting everything

**Recommendation**: ⚠️ NOT RECOMMENDED for this codebase due to complexity

#### Option B: Stepping Stone via Java 17 LTS (RECOMMENDED)
**Approach**: Two-phase migration via intermediate Java 17 LTS version

**Phase 1**: Java 11 → Java 17 with Spring Boot 3.0/3.1
**Phase 2**: Java 17 → Java 21 with Spring Boot 3.2+

**Pros**:
- ✅ Lower risk per migration cycle
- ✅ Easier to isolate and fix issues
- ✅ Java 17 LTS provides stable intermediate state
- ✅ Can validate Jakarta migration separately from Java 21 features
- ✅ Rollback points at each phase

**Cons**:
- ⚠️ More migration cycles
- ⚠️ Extended timeline

**Recommendation**: ✅ STRONGLY RECOMMENDED

### Recommended Migration Sequence

#### Phase 1: Java 17 Migration with Jakarta EE (4-5 weeks)

**Step 1.1: Upgrade to Java 17 and Spring Boot 3.0** (Week 1)
- Update `build.gradle` Java version to 17
- Update Spring Boot to 3.0.13 or 3.1.x (last stable before 3.2)
- Update Spring dependency management plugin
- Update CI/CD to Java 17

**Step 1.2: Complete Jakarta EE Namespace Migration** (Week 2-3)
- Use IDE find/replace for javax.* → jakarta.* across entire codebase
- Update all servlet, validation, and persistence imports
- Update Spring Security filters and configuration
- Fix compilation errors systematically

**Step 1.3: Upgrade MyBatis to 3.0.x** (Week 2)
- Update dependency to mybatis-spring-boot-starter:3.0.3
- Test all mapper XML files
- Verify type handlers work with jakarta.* types
- Run database integration tests

**Step 1.4: Upgrade JJWT to 0.12.x** (Week 2)
- Update JJWT dependencies to 0.12.5
- Update JWT service implementation for API changes
- Test token generation and validation
- Update security integration tests

**Step 1.5: Upgrade Netflix DGS to 7.x** (Week 3-4)
- Update DGS framework to 7.6.0 (latest 7.x)
- Update DGS codegen plugin to 7.0.0+
- Regenerate GraphQL types with new codegen
- Update GraphQL resolvers and mutations for API changes
- Test GraphQL schema compatibility
- Verify GraphQL queries and mutations

**Step 1.6: Update Supporting Dependencies** (Week 3)
- Update Flyway (managed by Spring Boot)
- Update SQLite JDBC to 3.45.0+
- Update JaCoCo to 0.8.11
- Update Spotless if needed
- Update test dependencies (Rest-Assured, etc.)

**Step 1.7: Comprehensive Testing** (Week 4-5)
- Run full test suite
- Manual testing of REST API endpoints
- Manual testing of GraphQL API
- Test JWT authentication flow
- Test database operations
- Integration testing
- Performance testing
- Update documentation

**Phase 1 Deliverables**:
- ✅ Working application on Java 17
- ✅ All dependencies on Spring Boot 3.0/3.1 compatible versions
- ✅ Jakarta EE migration complete
- ✅ All tests passing
- ✅ CI/CD updated and green

#### Phase 2: Java 21 Migration (1-2 weeks)

**Step 2.1: Upgrade to Java 21** (Week 1)
- Update `build.gradle` Java version to 21
- Update CI/CD to Java 21
- Test application startup

**Step 2.2: Upgrade Spring Boot to 3.2+** (Week 1)
- Update Spring Boot to 3.2.x or later (3.5.x for latest features)
- Update dependency management plugin
- Review and update any deprecated configuration properties
- Test application with Spring Boot 3.2+

**Step 2.3: Upgrade DGS to 8.x or 10.x** (Week 1)
- Update DGS framework to 8.x or 10.2+ (latest with Spring Boot 3.5)
- Update codegen plugin if needed
- Regenerate types if API changed
- Test GraphQL functionality

**Step 2.4: Enable Java 21 Features (Optional)** (Week 2)
- Enable virtual threads: `spring.threads.virtual.enabled=true`
- Evaluate String Templates (JEP 430) for SQL queries
- Evaluate Pattern Matching improvements
- Evaluate Record Patterns (JEP 440) for DTOs
- Consider replacing Lombok with Java Records for immutable DTOs

**Step 2.5: Final Testing and Performance Validation** (Week 2)
- Run full test suite on Java 21
- Performance benchmarking vs. Java 17 baseline
- Verify virtual threads don't cause issues
- Load testing
- Security testing
- Update documentation

**Phase 2 Deliverables**:
- ✅ Working application on Java 21
- ✅ All dependencies on latest stable versions
- ✅ Virtual threads enabled and validated
- ✅ Performance metrics documented
- ✅ All tests passing
- ✅ Documentation updated

### Alternative: Big Bang Approach (NOT RECOMMENDED)

If the team decides to attempt a single-phase migration despite the risks:

**Week 1-2**: Dependency updates and namespace migration
**Week 3-4**: DGS framework migration
**Week 5-6**: Testing and bug fixes
**Week 7-8**: Stabilization and deployment

**Risks**:
- ❌ High probability of unforeseen integration issues
- ❌ Difficult debugging when multiple major changes interact
- ❌ Extended stabilization period likely
- ❌ Higher chance of needing to rollback and restart

---

## Risk Assessment & Mitigation Strategies

### High-Risk Areas

#### 1. Jakarta EE Namespace Migration (CRITICAL)

**Risk Level**: 🔴 CRITICAL  
**Probability**: HIGH (guaranteed breaking change)  
**Impact**: HIGH (affects entire codebase)

**Specific Risks**:
- Missed javax.* references causing runtime failures
- Third-party libraries with javax.* dependencies
- Reflection-based code using string class names
- Configuration files referencing javax.* classes

**Mitigation Strategies**:
1. ✅ Use IDE global find/replace with regex: `import javax\.` → `import jakarta.`
2. ✅ Run static analysis to find all javax.* references
3. ✅ Comprehensive testing of all endpoints (REST + GraphQL)
4. ✅ Test all security filters and authentication flows
5. ✅ Review all XML configuration files and property files
6. ✅ Incremental migration approach allows validation before Java 21

**Validation Checklist**:
- [ ] All compilation errors resolved
- [ ] No javax.* imports in source code
- [ ] All tests passing
- [ ] Manual testing of authentication
- [ ] Manual testing of validation errors
- [ ] Security filters functioning correctly

#### 2. GraphQL Framework Migration (HIGH)

**Risk Level**: 🟠 HIGH  
**Probability**: MEDIUM-HIGH  
**Impact**: HIGH (dual API surface affected)

**Specific Risks**:
- DGS codegen breaking schema generation
- Generated type incompatibilities
- Resolver signature changes
- GraphQL query/mutation failures
- Breaking changes in data fetcher APIs
- Schema validation failures

**Mitigation Strategies**:
1. ✅ Commit schema files before codegen regeneration
2. ✅ Compare generated types before/after upgrade
3. ✅ Test all GraphQL queries and mutations comprehensively
4. ✅ Review DGS migration guides thoroughly
5. ✅ Keep DGS 7.x stable version before moving to 8.x/10.x
6. ✅ Maintain GraphQL schema compatibility

**Validation Checklist**:
- [ ] Schema.graphqls validates correctly
- [ ] Code generation succeeds without errors
- [ ] All resolvers compile
- [ ] All mutations compile
- [ ] GraphQL endpoint responds correctly
- [ ] Query complexity doesn't cause issues
- [ ] Error handling works as expected

#### 3. MyBatis Distributed Mappers (MODERATE-HIGH)

**Risk Level**: 🟠 MODERATE-HIGH  
**Probability**: MEDIUM  
**Impact**: MODERATE (data access layer)

**Specific Risks**:
- Mapper XML compatibility issues
- Type handler breaking changes
- Transaction management changes
- SQLite driver compatibility
- Parameterized query changes
- Result mapping failures

**Mitigation Strategies**:
1. ✅ Test each mapper file individually
2. ✅ Verify type handlers work with jakarta.* types
3. ✅ Run full integration test suite
4. ✅ Test transaction rollback scenarios
5. ✅ Validate all CRUD operations
6. ✅ SQLite database compatibility testing

**Validation Checklist**:
- [ ] All mapper XML files load correctly
- [ ] Database migrations succeed (Flyway)
- [ ] Type handlers functioning
- [ ] Transactions commit/rollback correctly
- [ ] All repository tests passing
- [ ] Complex queries returning correct results

#### 4. JWT Authentication Security (MODERATE)

**Risk Level**: 🟡 MODERATE  
**Probability**: LOW-MEDIUM  
**Impact**: HIGH (security implications)

**Specific Risks**:
- Token generation/validation failures
- Security filter chain breaking
- JWT claims handling changes
- Token expiration logic changes
- Incompatible signature algorithms

**Mitigation Strategies**:
1. ✅ Comprehensive security testing
2. ✅ Test token generation and validation separately
3. ✅ Verify existing tokens still validate (if needed)
4. ✅ Test all authentication flows end-to-end
5. ✅ Security audit after upgrade

**Validation Checklist**:
- [ ] Token generation successful
- [ ] Token validation successful
- [ ] Invalid tokens properly rejected
- [ ] Token expiration enforced
- [ ] Security filter chain intact
- [ ] All authentication tests passing

### Medium-Risk Areas

#### 5. CQRS Pattern Implementation (MODERATE)

**Risk Level**: 🟡 MODERATE  
**Probability**: LOW  
**Impact**: MODERATE

**Risks**:
- Query service compatibility with new framework versions
- Command handler changes
- Event handling if used

**Mitigation**:
- Test command and query flows separately
- Verify service layer integration
- Test application service orchestration

#### 6. Test Suite Compatibility (MODERATE)

**Risk Level**: 🟡 MODERATE  
**Probability**: MEDIUM  
**Impact**: MODERATE

**Risks**:
- Rest-Assured compatibility with Spring Boot 3.x
- MockMVC API changes
- Test configuration changes
- @WebMvcTest annotation changes

**Mitigation**:
- Update test dependencies to Spring Boot 3.x compatible versions
- Review test configuration for Spring Boot 3.x
- Run tests incrementally during migration

### Low-Risk Areas

#### 7. Build and Tooling (LOW)

**Risk Level**: 🟢 LOW  
**Probability**: LOW  
**Impact**: LOW

**Risks**:
- Gradle wrapper compatibility
- Spotless Java formatting
- JaCoCo coverage reporting

**Mitigation**:
- Update Gradle wrapper if needed
- Update JaCoCo to 0.8.11+
- Test build process on Java 21

#### 8. Frontend Integration (LOW)

**Risk Level**: 🟢 LOW  
**Probability**: VERY LOW  
**Impact**: LOW

**Risks**:
- API contract changes
- CORS configuration

**Mitigation**:
- Frontend uses REST/GraphQL APIs which remain compatible
- Test frontend integration after backend migration

### Rollback Strategy

#### Rollback Points

**Phase 1 Rollback** (Java 17 + Spring Boot 3.x):
- Restore `build.gradle` to Java 11 and Spring Boot 2.6.3
- Revert namespace changes (jakarta.* → javax.*)
- Revert dependency versions
- Redeploy from previous stable release

**Phase 2 Rollback** (Java 21):
- Restore `build.gradle` to Java 17
- Revert Spring Boot to 3.0/3.1
- Disable virtual threads
- Redeploy from Phase 1 stable state

#### Rollback Prerequisites

1. ✅ Tag stable release before each phase
2. ✅ Backup production database before deployment
3. ✅ Document configuration changes at each step
4. ✅ Maintain feature flags for new Java 21 features
5. ✅ CI/CD pipeline supports multi-version deployment

---

## Timeline Estimates

### Phase 1: Java 17 + Spring Boot 3.x (4-5 weeks)

| Task | Duration | Dependencies | Resources |
|------|----------|--------------|-----------|
| Environment setup & planning | 2-3 days | None | 1 developer |
| Java 17 + Spring Boot 3.0 upgrade | 3-4 days | Environment ready | 1-2 developers |
| Jakarta namespace migration | 5-7 days | Spring Boot 3.0 | 2 developers |
| MyBatis upgrade & testing | 3-4 days | Jakarta migration | 1 developer |
| JJWT upgrade & security testing | 2-3 days | Jakarta migration | 1 developer |
| DGS framework upgrade | 5-7 days | Spring Boot 3.0 | 1-2 developers |
| Integration testing | 3-5 days | All upgrades | 2 developers |
| Bug fixes & stabilization | 3-5 days | Testing complete | 1-2 developers |
| Documentation & review | 2-3 days | Stabilization | 1 developer |

**Total Phase 1**: 4-5 weeks with 1-2 developers

### Phase 2: Java 21 Optimization (1-2 weeks)

| Task | Duration | Dependencies | Resources |
|------|----------|--------------|-----------|
| Java 21 + Spring Boot 3.2+ upgrade | 2-3 days | Phase 1 complete | 1 developer |
| DGS upgrade to 8.x/10.x (optional) | 2-3 days | Java 21 stable | 1 developer |
| Virtual threads enablement | 1-2 days | Java 21 stable | 1 developer |
| Java 21 feature adoption | 2-4 days | Virtual threads tested | 1-2 developers |
| Performance testing & tuning | 2-3 days | Features enabled | 1 developer |
| Final testing & documentation | 2-3 days | Performance validated | 1 developer |

**Total Phase 2**: 1-2 weeks with 1-2 developers

### Total Project Timeline

**Recommended Approach** (Incremental):
- **Phase 1**: 4-5 weeks
- **Phase 2**: 1-2 weeks
- **Total**: 5-7 weeks

**Alternative Big Bang Approach** (Not Recommended):
- **Total**: 6-8 weeks (higher risk, likely longer stabilization)

### Effort Estimates

| Role | Phase 1 Hours | Phase 2 Hours | Total Hours |
|------|--------------|--------------|-------------|
| Senior Developer | 120-160 | 40-60 | 160-220 |
| Developer | 80-120 | 20-40 | 100-160 |
| QA Engineer | 60-80 | 20-30 | 80-110 |
| DevOps Engineer | 20-30 | 10-15 | 30-45 |

**Total Project Effort**: 370-535 person-hours

---

## Open Questions & Recommendations

### 1. Documentation Format

**Question**: What format should the final compatibility documentation take?

**Answer**: This document is provided in **Markdown format** (`JAVA_21_COMPATIBILITY_ASSESSMENT.md`) for several reasons:
- ✅ Version controllable in Git
- ✅ Readable in GitHub/GitLab UI
- ✅ Easy to update and maintain
- ✅ Supports code blocks and formatting
- ✅ Can be converted to PDF/HTML if needed

**Recommendation**: Keep this document in the repository root and update it as migration progresses.

### 2. Java 17 LTS as Stepping Stone

**Question**: Should we consider intermediate Java versions (17 LTS) as stepping stones?

**Answer**: ✅ **YES - STRONGLY RECOMMENDED**

**Rationale**:
1. **Lower Risk**: Separates Jakarta EE migration from Java 21 features
2. **Stable Intermediate State**: Java 17 LTS provides production-ready milestone
3. **Easier Debugging**: Issues can be isolated to specific upgrade phases
4. **Rollback Points**: Can rollback to Java 17 if Java 21 issues arise
5. **Industry Practice**: Most organizations migrating 11→21 use 17 as intermediate

**Recommendation**: Follow the two-phase approach outlined in this document.

### 3. Jakarta Namespace Impact on Security + JWT

**Question**: What are the specific jakarta.* namespace impacts on our current Spring Security + JWT setup?

**Answer**: **MODERATE-HIGH IMPACT**

**Affected Components**:
- `JwtTokenFilter` uses javax.servlet.* (lines 8-11) → jakarta.servlet.*
- All HTTP request/response handling in filters
- Security configuration using javax.servlet.Filter
- Authentication entry points
- CORS configuration

**Required Changes**:
1. Update all servlet imports in security package
2. Update filter chain configuration
3. Update security configuration
4. Test authentication flow end-to-end
5. Verify JWT token generation/validation still works

**JJWT Library Compatibility**: JJWT 0.12.x is agnostic to javax vs jakarta, so JWT functionality itself is not affected.

**Recommendation**: Thoroughly test authentication after namespace migration.

### 4. Dual API Surface Complication

**Question**: How will the dual API surface (REST + GraphQL) complicate the Spring Boot upgrade?

**Answer**: **MODERATE COMPLICATION**

**REST API Impact**:
- Spring MVC controller changes minimal
- Jakarta namespace migration required
- Validation annotations updated
- HATEOAS compatibility with Spring Boot 3.x

**GraphQL API Impact**:
- Netflix DGS 4.9→7.0 major upgrade required
- Code generation changes
- Resolver API updates
- Schema compatibility validation

**Interdependencies**:
- Both APIs share same domain layer (benefit)
- Both use same security configuration (single point of update)
- Both use same MyBatis repositories (single upgrade path)

**Recommendation**:
1. Migrate REST API first (less complex)
2. Validate REST endpoints thoroughly
3. Then migrate GraphQL API
4. Test both APIs in integration

### 5. CQRS Pattern Implementation

**Question**: What are the implications for our CQRS pattern implementation with newer framework versions?

**Answer**: **LOW-MODERATE IMPACT**

**Current CQRS Implementation**:
- Command side: Article/User/Comment command services
- Query side: Query services with read models
- Separation of write/read operations

**Migration Impact**:
- ✅ Domain layer (entities) mostly unaffected
- ✅ CQRS pattern itself framework-agnostic
- ⚠️ Query services using MyBatis need testing
- ⚠️ Application services orchestration unchanged
- ⚠️ Transaction management in Spring Boot 3.x

**Recommendation**:
- CQRS pattern does not complicate migration
- Test command and query flows separately
- Verify transaction boundaries still correct

### 6. Show-Stopping Compatibility Issues

**Question**: Are there any show-stopping compatibility issues that would block Java 21 migration?

**Answer**: ✅ **NO SHOW-STOPPERS IDENTIFIED**

**Analysis**:
1. ✅ All four primary dependencies have Java 21-compatible versions
2. ✅ SQLite JDBC driver supports Java 21
3. ✅ No known incompatible dependencies in dependency tree
4. ✅ Spring Boot 3.2+ officially supports and tests Java 21
5. ✅ Netflix DGS framework has active Java 21 support

**Potential Blockers to Monitor**:
- ⚠️ Custom code using deprecated Java APIs (low probability)
- ⚠️ Transitive dependencies with Java 21 issues (check during upgrade)
- ⚠️ IDE/tooling support for Java 21 (should be mature by now)

**Recommendation**: Proceed with migration plan. No fundamental blockers exist.

### 7. Lombok vs. Java Records Decision

**Question**: Should migration to Java 21 be an opportunity to adopt Records?

**Answer**: **EVALUATE CASE-BY-CASE**

**Java Records Benefits**:
- ✅ Native language feature (no annotation processor needed)
- ✅ Immutable by default (good for DTOs)
- ✅ Cleaner syntax for data carriers
- ✅ Better IDE support

**Lombok Benefits**:
- ✅ More flexible (can have mutable fields)
- ✅ More annotations available (@Builder, @With, etc.)
- ✅ Works with Java 17 during Phase 1

**Recommendation**:
1. **Phase 1**: Keep Lombok (focus on Jakarta migration)
2. **Phase 2**: Evaluate Records for new DTOs
3. **Post-Migration**: Gradually migrate DTOs to Records where appropriate

**Good Candidates for Records**:
- Immutable DTOs (ArticleData, UserData, ProfileData, etc.)
- Value objects in domain layer
- GraphQL input types

**Keep Lombok For**:
- Entities with complex builder patterns
- Classes requiring @With or @Wither
- Classes with custom logic in getters/setters

---

## Next Steps & Action Items

### Immediate Actions (Before Migration Starts)

1. ✅ **Review and approve this migration plan** with team and stakeholders
2. ✅ **Set up Java 17 development environment** on developer machines
3. ✅ **Create feature branch** for Java 17 migration (devin/java-17-migration)
4. ✅ **Set up Java 17 CI/CD pipeline** (parallel to Java 11 initially)
5. ✅ **Backup production database** and create restore procedure
6. ✅ **Schedule migration timeline** with stakeholders
7. ✅ **Identify testing resources** for each phase

### Phase 1 Preparation

1. ✅ **Install Java 17 JDK** on all development machines
2. ✅ **Update IDE configurations** for Java 17
3. ✅ **Create comprehensive test plan** for Jakarta migration
4. ✅ **Document current API contracts** (REST + GraphQL)
5. ✅ **Set up monitoring** for migration progress
6. ✅ **Create rollback runbook** for each phase

### Phase 2 Preparation

1. ✅ **Validate Java 17 stability** in production (Phase 1 complete)
2. ✅ **Install Java 21 JDK** on development machines
3. ✅ **Create performance baseline** on Java 17
4. ✅ **Plan Java 21 feature adoption** (virtual threads, etc.)
5. ✅ **Update documentation** for Java 21 features

---

## Conclusion

The migration from Java 11 to Java 21 is **feasible and recommended** with a structured, incremental approach. All four primary dependencies have mature Java 21-compatible versions, and no show-stopping compatibility issues have been identified.

### Key Recommendations

1. ✅ **Use incremental migration** via Java 17 LTS stepping stone
2. ✅ **Prioritize Jakarta EE migration** separately from Java 21 features
3. ✅ **Allocate 5-7 weeks** for complete migration with testing
4. ✅ **Focus on comprehensive testing** at each phase
5. ✅ **Maintain rollback capability** at each milestone

### Success Criteria

The migration will be considered successful when:
- ✅ Application runs on Java 21 with Spring Boot 3.2+
- ✅ All tests passing (unit, integration, security)
- ✅ REST and GraphQL APIs functioning correctly
- ✅ Performance meets or exceeds Java 11 baseline
- ✅ CI/CD pipeline stable on Java 21
- ✅ Documentation updated
- ✅ Team trained on Java 21 features

This assessment provides the **implementation-ready migration plan** requested in ticket JP-9, enabling the team to proceed with confidence toward Java 21 adoption.

---

**Document Version**: 1.0  
**Last Updated**: October 7, 2025  
**Status**: Ready for Review and Approval
