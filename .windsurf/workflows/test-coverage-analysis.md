---
description: Workflow to run a test coverage analysis report and recommendation next steps
auto_execution_mode: 3
---

# Test Coverage Analysis Workflow

## Overview
Automated workflow to run tests, analyze JaCoCo coverage reports, and provide actionable recommendations for improving test coverage.

## Workflow Steps

### 1. Run Tests with Coverage
**Command:** `./gradlew clean test`

**What it does:**
- Executes all JUnit tests
- Generates JaCoCo coverage reports (HTML, XML, CSV)
- Displays formatted coverage summary in console
- Validates coverage against thresholds

**Expected Output:**
- Coverage summary table with metrics
- Report file locations
- Coverage violations (if any)

---

### 2. Analyze Coverage Report
**Command:** `./gradlew coverageSummary`

**Analysis Points:**
- **Overall Coverage:** Target 80% line, 75% branch
- **Class Coverage:** Minimum 70% per class
- **Method Complexity:** Maximum 15 per method
- **Exclusions:** Config, DTOs, entities, generated code

**Report Locations:**
- HTML: `build/reports/jacoco/test/html/index.html`
- CSV: `build/reports/jacoco/test/jacocoTestReport.csv`
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`

---

### 2.1. How to Read Coverage Reports

#### **HTML Report (Best for Visual Analysis)**
Open in browser: `open build/reports/jacoco/test/html/index.html`

**Report Structure:**
1. **Overview Page** - Shows package-level summary
   - Green bars = good coverage (>70%)
   - Yellow bars = needs improvement (40-70%)
   - Red bars = critical gaps (<40%)

2. **Package View** - Click package name to drill down
   - Lists all classes in package with coverage percentages
   - Sort by clicking column headers (Missed Instructions, Coverage %)

3. **Class View** - Click class name for detailed view
   - Shows method-level coverage
   - Color-coded source code:
     - **Green background** = line fully covered
     - **Yellow background** = line partially covered (some branches missed)
     - **Red background** = line not covered
     - **Gray diamond** = branch point (if/else, switch, etc.)

4. **Method View** - Click method name
   - Shows exact lines covered/missed
   - Branch coverage indicators show which conditions were tested

**Key Metrics Explained:**
- **Instructions (Cov.)** - Bytecode instructions executed (most granular)
- **Branches (Cov.)** - Decision points tested (if/else, switch, ternary)
- **Lines (Cov.)** - Source code lines executed
- **Methods (Cov.)** - Methods invoked during tests
- **Complexity** - Cyclomatic complexity (decision paths)

#### **CSV Report (Best for Automation/Scripting)**
Location: `build/reports/jacoco/test/jacocoTestReport.csv`

**CSV Column Structure:**
```
GROUP,PACKAGE,CLASS,INSTRUCTION_MISSED,INSTRUCTION_COVERED,
BRANCH_MISSED,BRANCH_COVERED,LINE_MISSED,LINE_COVERED,
COMPLEXITY_MISSED,COMPLEXITY_COVERED,METHOD_MISSED,METHOD_COVERED
```

**Reading CSV Data:**
- Column 1: `GROUP` - Project name
- Column 2: `PACKAGE` - Java package
- Column 3: `CLASS` - Fully qualified class name
- Columns 4-5: Instructions (missed, covered)
- Columns 6-7: Branches (missed, covered)
- Columns 8-9: Lines (missed, covered)
- Columns 10-11: Complexity (missed, covered)
- Columns 12-13: Methods (missed, covered)

**Calculate Coverage Percentage:**
```bash
# Line coverage = LINE_COVERED / (LINE_MISSED + LINE_COVERED) * 100
# Example: 8 covered, 2 missed = 8/(2+8)*100 = 80%
```

**Example CSV Row:**
```csv
spring-boot-realworld-example-app,io.spring.application.article,ArticleCommandService,34,6,0,0,14,1,2,1,2,1
```
**Interpretation:**
- Class: `ArticleCommandService`
- Instructions: 6 covered, 34 missed = 15% coverage
- Lines: 1 covered, 14 missed = 6.7% coverage
- Methods: 1 covered, 2 missed = 33% coverage
- **Action:** Critical - needs comprehensive test suite

#### **XML Report (Best for CI/CD Integration)**
Location: `build/reports/jacoco/test/jacocoTestReport.xml`

**Use Cases:**
- SonarQube integration
- CI/CD pipeline quality gates
- Automated coverage tracking
- Third-party tool integration

**Structure:**
```xml
<report name="spring-boot-realworld-example-app">
  <package name="io.spring.application">
    <class name="ArticleCommandService">
      <counter type="INSTRUCTION" missed="34" covered="6"/>
      <counter type="BRANCH" missed="0" covered="0"/>
      <counter type="LINE" missed="14" covered="1"/>
    </class>
  </package>
</report>
```

#### **Console Output (Quick Summary)**
After running tests, console shows:
```
JACOCO TEST COVERAGE SUMMARY
Metric          Covered    Total  Percentage
Instructions         60      110      54.55%
Branches              7       22      31.82%
Lines                 8        8     100.00%
```

**Reading Console Output:**
- Quick health check without opening files
- Shows overall project coverage
- Lists report file paths
- Displays coverage violations

#### **Coverage Interpretation Guide**

**Coverage Levels:**
- **90-100%** - Excellent (comprehensive testing)
- **80-89%** - Good (meets target)
- **70-79%** - Acceptable (needs improvement)
- **50-69%** - Poor (significant gaps)
- **<50%** - Critical (immediate attention needed)

**Branch Coverage Importance:**
- Line coverage can be misleading
- Branch coverage ensures all conditions tested
- Example:
  ```java
  if (user != null && user.isActive()) { // 2 branches
      return true;
  }
  return false;
  ```
  - 100% line coverage = both lines executed
  - 50% branch coverage = only tested one condition path

**What to Focus On:**
1. **Low coverage + high complexity** = highest risk
2. **Business logic services** = highest priority
3. **Public APIs** = user-facing, must be tested
4. **Exception handlers** = error paths often missed

---

### 3. Identify Coverage Gaps

**Parse CSV for Low Coverage Classes:**
```bash
awk -F',' 'NR>1 && $1=="CLASS" {
  total=$8+$9; 
  if(total>0) {
    pct=($9/total)*100; 
    if(pct<70) printf "%-60s %6.2f%%\n", $3, pct
  }
}' build/reports/jacoco/test/jacocoTestReport.csv | sort -t'%' -k2 -n
```

**Find High Complexity Methods:**
```bash
awk -F',' 'NR>1 && $1=="METHOD" && $10>15 {
  printf "%-60s Complexity: %d\n", $3"."$4, $10
}' build/reports/jacoco/test/jacocoTestReport.csv | sort -t':' -k2 -rn
```

---

### 4. Prioritize Test Writing

**Priority Order:**
1. **Critical Business Logic** (Services, Command handlers)
   - `ArticleCommandService` (6% coverage)
   - `CommentQueryService` (51% coverage)
   - `AuthorizationService` (66% coverage)

2. **API Controllers** (Public interfaces)
   - `TagsApi` (50% coverage)
   - Exception handlers (63% coverage)

3. **Domain Models with Logic** (Params, validators)
   - `NewArticleParam` (14% coverage)
   - `RegisterParam` (20% coverage)

4. **Infrastructure** (Handlers, utilities)
   - `DateTimeHandler` (63% coverage)
   - `Util` (50% coverage)

**Skip (Already Excluded):**
- Configuration classes
- DTOs and entities
- Lombok-generated code
- GraphQL generated code

---

### 5. Recommended Actions

**Immediate Actions:**
1. Add unit tests for `ArticleCommandService` methods
2. Add unit tests for `CommentQueryService` methods
3. Test exception handling paths in controllers
4. Add integration tests for API endpoints

**Coverage Improvement Strategy:**
```bash
# Focus on one service at a time
# Example: ArticleCommandService
# 1. Create test class: ArticleCommandServiceTest.java
# 2. Test each public method
# 3. Test error conditions
# 4. Run: ./gradlew test --tests ArticleCommandServiceTest
# 5. Verify coverage improved: ./gradlew coverageSummary
```

**Complexity Reduction:**
- Refactor `equals()` methods in data classes (use Lombok @EqualsAndHashCode)
- Break down methods with complexity >15 into smaller methods
- Consider excluding Lombok-generated methods from complexity checks

---

## Quick Commands

**Run full analysis:**
```bash
./gradlew clean test
```

**View HTML report:**
```bash
open build/reports/jacoco/test/html/index.html
```

**Check specific class coverage:**
```bash
./gradlew test --tests "io.spring.application.article.*" && ./gradlew coverageSummary
```

**Run without coverage verification (for CI):**
```bash
./gradlew test -x jacocoTestCoverageVerification
```

---

## Success Criteria

- [ ] Overall line coverage ≥ 80%
- [ ] Branch coverage ≥ 75%
- [ ] All service classes ≥ 70% coverage
- [ ] All API controllers ≥ 70% coverage
- [ ] No methods with complexity > 15 (excluding Lombok)
- [ ] All critical business logic paths tested

---

## Notes

- Coverage thresholds are enforced by `jacocoTestCoverageVerification` task
- Build will fail if thresholds not met (by design)
- Use `--info` flag for detailed coverage output
- CSV report is ideal for automated parsing and CI/CD integration