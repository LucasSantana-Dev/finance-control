# Code Quality Tools Configuration and Synchronization

This document describes the comprehensive code quality tools implemented in the Finance Control project to ensure high code quality, proper linting, and code coverage validation, along with how these tools are synchronized to work together.

## Overview

The project implements the following code quality tools:

1. **Checkstyle** - Code style and formatting enforcement
2. **PMD** - Static code analysis for potential bugs and code smells
3. **SpotBugs** - Bytecode analysis for potential bugs
4. **JaCoCo** - Code coverage analysis and reporting
5. **SonarQube** - Comprehensive code quality analysis

## Tools Configuration

### 1. Checkstyle

**Purpose**: Enforces Java coding standards and best practices.

**Configuration**: `checkstyle.xml`

**Key Features**:
- Naming conventions enforcement
- Import statement validation
- Code size limits (methods, classes, files)
- Whitespace and formatting rules
- Complexity metrics (cyclomatic, NPath)
- Documentation requirements
- Best practices enforcement

**Usage**:
```bash
# Run Checkstyle
./gradlew checkstyleMain

# Generate report
./gradlew checkstyleMain
```

**Thresholds**:
- Maximum line length: 160 characters
- Maximum method length: 150 lines (warning only)
- Maximum file length: 500 lines (warning only)
- Cyclomatic complexity: 15 (fails build if exceeded)
- NPath complexity: 200 (warning only)
- Class data abstraction coupling: 7 (warning only)
- Class fan-out complexity: 20 (warning only)

### 2. PMD

**Purpose**: Static code analysis for potential bugs, code smells, and design issues.

**Configuration**: `pmd-ruleset.xml`

**Key Features**:
- Best practices enforcement
- Code style validation
- Design pattern violations
- Performance issues detection
- Security vulnerabilities
- Error-prone code patterns

**Usage**:
```bash
# Run PMD
./gradlew pmdMain

# Generate report
./gradlew pmdMain
```

**Rule Categories**:
- **Best Practices** (Priority 3)
- **Code Style** (Priority 3)
- **Design** (Priority 3)
- **Documentation** (Priority 4)
- **Error Prone** (Priority 2)
- **Performance** (Priority 3)
- **Security** (Priority 2)
- **Multithreading** (Priority 3)

### 3. SpotBugs

**Purpose**: Bytecode analysis to find potential bugs in compiled Java code.

**Configuration**:
- `spotbugs-include.xml` - Include filters
- `spotbugs-exclude.xml` - Exclude filters

**Key Features**:
- Null pointer dereference detection
- Resource leak detection
- Thread safety issues
- Performance problems
- Security vulnerabilities
- Concurrency issues

**Usage**:
```bash
# Run SpotBugs
./gradlew spotbugsMain

# Generate report
./gradlew spotbugsMain
```

**Settings**:
- Effort: Max (thorough analysis)
- Threshold: Low (sensitive detection)
- Output formats: XML and HTML

### 4. JaCoCo

**Purpose**: Code coverage analysis and reporting.

**Configuration**: Configured in `build.gradle`

**Key Features**:
- Line coverage analysis
- Branch coverage analysis
- Method coverage analysis
- Class coverage analysis
- Coverage thresholds enforcement
- HTML and XML reports

**Usage**:
```bash
# Run tests with coverage
./gradlew clean test

# Generate coverage report
./gradlew jacocoTestReport

# Check coverage thresholds
./gradlew jacocoTestCoverageVerification
```

**Coverage Thresholds**:
- Minimum line coverage: 80%
- Minimum branch coverage: 80%
- Fail on violation: true

**Exclusions**:
- Main application class
- Configuration classes
- DTOs and models
- Exception classes
- Enums and utilities
- Validation classes

### 5. SonarQube

**Purpose**: Comprehensive code quality analysis and reporting.

**Configuration**: `sonar-project.properties`

**Key Features**:
- Code quality metrics
- Security hotspots analysis
- Technical debt calculation
- Duplicate code detection
- Code smell identification
- Maintainability analysis

**Usage**:
```bash
# Run SonarQube analysis
./gradlew clean build sonarqube
```

**Quality Gates**:
- Maintainability Rating: A
- Reliability Rating: A
- Security Rating: A
- Security Hotspots Rating: A
- Coverage: 80% minimum

## Code Quality Tools Synchronization

This section describes how code quality tools (SonarLint, PMD, Checkstyle, SpotBugs) are configured to work together in the Finance Control project.

### Configured Tools

#### 1. Checkstyle
- **File**: `checkstyle.xml`
- **Purpose**: Code standards, naming, formatting
- **Integration**: Reports sent to SonarQube

#### 2. PMD
- **File**: `pmd-ruleset.xml`
- **Purpose**: Static analysis, complexity, best practices
- **Integration**: Reports sent to SonarQube

#### 3. SpotBugs
- **File**: `spotbugs-exclude.xml`
- **Purpose**: Potential bug detection
- **Integration**: Reports sent to SonarQube

#### 4. SonarQube
- **File**: `sonar-project.properties`
- **Purpose**: Consolidated analysis and metrics
- **Integration**: Consolidates reports from all tools

### Synchronized Rules

#### Cognitive Complexity (Primary Metric)
The project uses **cognitive complexity** as the primary metric for code quality, focusing on maintainability and readability rather than arbitrary line counts.

**Production Code Thresholds**:
- **Checkstyle**: `CyclomaticComplexity` - max 15 (fails build if exceeded)
- **PMD**: `CyclomaticComplexity` - methodReportLevel 15, `CognitiveComplexity` - reportLevel 15 (fails build if exceeded)
- **SonarQube**: `sonar.complexity.function.threshold=15`, `sonar.java.cognitive.complexity.threshold=15`

**Test Code Thresholds** (higher thresholds for test files):
- **PMD**: `CyclomaticComplexity` - methodReportLevel 20, `CognitiveComplexity` - reportLevel 20 (fails build if exceeded)
- **SonarQube**: Test files use threshold of 20

**Rationale**:
- Cognitive complexity measures how difficult code is to understand, not just how many branches it has
- Focuses on maintainability and code readability
- Allows longer files if they remain simple to understand
- Different thresholds for tests reflect their different purpose (setup/teardown can be verbose)

#### Method Size (Warning Only)
- **Checkstyle**: `MethodLength` - max 150 lines (warning only, not blocking)
- **PMD**: `ExcessiveMethodLength` - minimum 150 lines (priority 3, warning only)
- **SonarQube**: `sonar.size.limit.function=150` (informational)

**Note**: Method length violations are warnings only. If a method exceeds 150 lines but has low cognitive complexity, it may be acceptable. However, consider refactoring for better maintainability.

#### Class/File Size (Warning Only)
- **Checkstyle**: `FileLength` - max 500 lines (warning only, not blocking)
- **PMD**: `ExcessiveClassLength` - minimum 500 lines (priority 3, warning only)
- **SonarQube**: `sonar.size.limit.class=2000` (informational)

**Note**: File length violations are warnings only. Large files with low cognitive complexity may be acceptable, but consider splitting for better organization.

#### Imports
- **Checkstyle**: Custom rule to avoid inline imports
- **PMD**: `UnusedImports`, `RedundantImports`
- **SonarQube**: Automatic import rules

#### Naming
- **Checkstyle**: Specific naming rules
- **PMD**: `AtLeastOneConstructor`, `OnlyOneReturn`
- **SonarQube**: Automatic naming rules

### Synchronized Exclusions

#### Exclusion Patterns
```xml
<!-- Checkstyle -->
<module name="BeforeExecutionExclusionFileFilter">
    <property name="fileNamePattern" value=".*[/\\]target[/\\].*"/>
</module>

<!-- PMD -->
<exclude-pattern>*/target/*</exclude-pattern>
<exclude-pattern>*/generated/*</exclude-pattern>
<exclude-pattern>*/test/*</exclude-pattern>

<!-- SpotBugs -->
<Match>
    <Class name="~.*\.generated\..*"/>
</Match>
<Match>
    <Class name="~.*Test$"/>
</Match>
```

#### Excluded Packages
- `**/target/**`
- `**/generated/**`
- `**/test/**`
- `**/config/**`
- `**/dto/**`
- `**/model/**`
- `**/exception/**`
- `**/enums/**`
- `**/util/**`
- `**/validation/**`

## Gradle Configuration

### Dependencies
```gradle
plugins {
    id 'checkstyle'
    id 'pmd'
    id 'com.github.spotbugs'
    id 'org.sonarqube'
}

checkstyle {
    toolVersion = '10.12.5'
    configFile = file('checkstyle.xml')
    reportsDir = file("$buildDir/reports/checkstyle")
}

pmd {
    toolVersion = '6.55.0'
    ruleSetFiles = files('pmd-ruleset.xml')
    reportsDir = file("$buildDir/reports/pmd")
}

spotbugs {
    toolVersion = '4.7.3'
    effort = 'max'
    reportLevel = 'medium'
    excludeFilter = file('spotbugs-exclude.xml')
    reportsDir = file("$buildDir/reports/spotbugs")
}
```

### SonarQube Integration
```gradle
sonarqube {
    properties {
        property 'sonar.java.checkstyle.reportPaths', 'build/reports/checkstyle/main.xml'
        property 'sonar.java.pmd.reportPaths', 'build/reports/pmd/main.xml'
        property 'sonar.java.spotbugs.reportPaths', 'build/reports/spotbugs/main.xml'
    }
}
```

## Workflow

### 1. Local Development
```bash
# Check code quality
./gradlew checkstyleMain pmdMain spotbugsMain

# Complete analysis
./gradlew sonarqube
```

### 2. CI/CD Pipeline
```yaml
- name: Code Quality Analysis
  run: |
    ./gradlew checkstyleMain pmdMain spotbugsMain
    ./gradlew sonarqube
```

### 3. Consolidated Reports
- **Checkstyle**: `build/reports/checkstyle/`
- **PMD**: `build/reports/pmd/`
- **SpotBugs**: `build/reports/spotbugs/`
- **SonarQube**: Online dashboard

## Synchronization Benefits

1. **Consistency**: Same rules applied by all tools
2. **Coverage**: Different quality aspects covered
3. **Integration**: Consolidated reports in SonarQube
4. **Maintainability**: Centralized and documented configurations
5. **Performance**: Optimized exclusions to avoid false positives

## IDE Integration

### IntelliJ IDEA

1. **Checkstyle Plugin**:
   - Install "CheckStyle-IDEA" plugin
   - Configure `checkstyle.xml` as rules file
   - Enable real-time checking

2. **PMD Plugin**:
   - Install "PMD Plugin"
   - Configure `pmd-ruleset.xml` as rules file
   - Enable real-time checking

3. **SpotBugs Plugin**:
   - Install "SpotBugs" plugin
   - Configure include/exclude filters
   - Enable real-time checking

4. **JaCoCo Plugin**:
   - Built-in coverage support
   - Configure coverage thresholds
   - View coverage reports

### Eclipse

1. **Checkstyle Plugin**:
   - Install "Checkstyle Plugin"
   - Import `checkstyle.xml` configuration
   - Enable project-specific settings

2. **PMD Plugin**:
   - Install "PMD Plugin"
   - Import `pmd-ruleset.xml` configuration
   - Enable real-time checking

3. **SpotBugs Plugin**:
   - Install "SpotBugs Plugin"
   - Configure filters
   - Enable real-time checking

## CI/CD Integration

### GitHub Actions

```yaml
name: Code Quality

on: [push, pull_request]

jobs:
  quality:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Run Checkstyle
      run: ./gradlew checkstyleMain

    - name: Run PMD
      run: ./gradlew pmdMain

    - name: Run SpotBugs
      run: ./gradlew spotbugsMain

    - name: Run tests with coverage
      run: ./gradlew clean test jacocoTestReport

    - name: Check coverage thresholds
      run: ./gradlew jacocoTestCoverageVerification

    - name: SonarQube analysis
      run: ./gradlew sonarqube
      env:
        SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

### Jenkins Pipeline

```groovy
pipeline {
    agent any

    stages {
        stage('Checkstyle') {
            steps {
                sh './gradlew checkstyleMain'
            }
        }

        stage('PMD') {
            steps {
                sh './gradlew pmdMain'
            }
        }

        stage('SpotBugs') {
            steps {
                sh './gradlew spotbugsMain'
            }
        }

        stage('Test & Coverage') {
            steps {
                sh './gradlew clean test jacocoTestReport'
                sh './gradlew jacocoTestCoverageVerification'
            }
        }

        stage('SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh './gradlew sonarqube'
                }
            }
        }
    }

    post {
        always {
            publishHTML([
                allowMissing: false,
                alwaysLinkToLastBuild: true,
                keepAll: true,
                reportDir: 'build/reports',
                reportFiles: 'index.html',
                reportName: 'JaCoCo Coverage Report'
            ])
        }
    }
}
```

## Quality Gates

### Checkstyle Quality Gate
- **Severity**: Warning
- **Fail on Error**: true
- **Console Output**: true

### PMD Quality Gate
- **Fail on Violation**: true
- **Print Failing Errors**: true
- **Analysis Cache**: enabled

### SpotBugs Quality Gate
- **Fail on Error**: true
- **Effort**: Max
- **Threshold**: Low

### JaCoCo Quality Gate
- **Line Coverage**: 80% minimum
- **Branch Coverage**: 80% minimum
- **Fail on Violation**: true

### SonarQube Quality Gate
- **Maintainability**: A
- **Reliability**: A
- **Security**: A
- **Coverage**: 80% minimum
- **Duplicated Lines**: < 3%

## Best Practices

### 1. Code Organization

#### File Structure
Organize your code in a logical, consistent structure following the project's package organization:

```
src/main/java/com/finance_control/
├── transactions/           # Transaction module
│   ├── controller/        # REST controllers
│   ├── service/           # Business logic
│   ├── repository/        # Data access
│   ├── model/             # Entity classes
│   ├── dto/               # Data transfer objects
│   └── mapper/            # MapStruct mappers
├── shared/                 # Shared utilities
│   ├── config/            # Configuration classes
│   ├── exception/         # Exception handlers
│   └── util/              # Utility classes
└── goals/                  # Goals module
    └── ...
```

#### Naming Conventions
- **Classes**: PascalCase (e.g., `TransactionService`, `UserController`)
- **Methods/Variables**: camelCase (e.g., `findUserById`, `isTransactionValid`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`, `DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase with dots (e.g., `com.finance_control.transactions.service`)
- **Test Classes**: Same as class with `Test` suffix (e.g., `TransactionServiceTest`)

#### Import Organization
```java
// 1. Java standard library imports
import java.math.BigDecimal;
import java.time.LocalDate;

// 2. Third-party library imports
import org.springframework.stereotype.Service;
import jakarta.persistence.Entity;

// 3. Spring Framework imports
import org.springframework.beans.factory.annotation.Autowired;

// 4. Project imports
import com.finance_control.shared.config.BaseService;
import com.finance_control.transactions.model.Transaction;
```

### 2. Type Safety and Java Best Practices

#### Use Strong Typing
```java
// ❌ Bad - Using Object or raw types
public void processData(Object data) {
    String value = (String) data;
}

// ✅ Good - Proper typing
public void processData(TransactionDTO data) {
    String value = data.getDescription();
}
```

#### Use Java 21 Features When Appropriate
```java
// Records for DTOs
public record TransactionSummary(BigDecimal total, long count) {}

// Pattern matching (Java 21)
if (transaction instanceof ExpenseTransaction expense) {
    expense.processExpense();
}

// Sealed classes for restricted hierarchies
public sealed class Transaction permits Income, Expense {}
```

#### Null Safety
```java
// ❌ Bad - Null pointer risk
String name = user.getName();
return name.toUpperCase();

// ✅ Good - Null safety
String name = user.getName();
return name != null ? name.toUpperCase() : "";
// Or use Optional
return Optional.ofNullable(user.getName())
    .map(String::toUpperCase)
    .orElse("");
```

### 3. Service Design Patterns

#### Single Responsibility Principle
```java
// ❌ Bad - Multiple responsibilities
@Service
class TransactionService {
    public void processTransaction() {
        // Validates, saves, sends email, logs, calculates tax...
    }
}

// ✅ Good - Single responsibility
@Service
class TransactionService {
    private final TransactionValidator validator;
    private final TransactionRepository repository;
    private final NotificationService notificationService;

    public TransactionDTO createTransaction(TransactionDTO dto) {
        validator.validate(dto);
        Transaction saved = repository.save(TransactionMapper.toEntity(dto));
        notificationService.notifyTransactionCreated(saved);
        return TransactionMapper.toDTO(saved);
    }
}
```

#### Dependency Injection
```java
// ✅ Good - Constructor injection
@Service
class TransactionService {
    private final TransactionRepository repository;
    private final CategoryService categoryService;

    public TransactionService(
            TransactionRepository repository,
            CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
    }
}
```

### 4. Code Style
- Follow Checkstyle rules consistently
- Use consistent naming conventions
- Maintain proper code formatting
- Write self-documenting code
- Avoid unnecessary comments - code should explain itself

### 5. Code Quality (Cognitive Complexity Focus)
- **Primary Metric**: Keep cognitive complexity low (max 15 for production, max 20 for tests)
- **Method Size**: Aim for < 150 lines, but prioritize low complexity over strict line limits
- **File Size**: Aim for < 500 lines, but large files with low complexity are acceptable
- **Complexity Violations**: Will fail the build - must be addressed
- **Size Violations**: Warnings only - consider refactoring but not blocking
- Avoid code duplication (DRY principle)
- Follow SOLID principles
- Use meaningful variable and method names
- Extract magic numbers into constants

#### Refactoring Complex Methods

When a method exceeds complexity thresholds, consider these refactoring strategies:

**Extract Methods**:
```java
// ❌ Bad - High complexity (multiple responsibilities)
public TransactionDTO processTransaction(TransactionDTO dto) {
    if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new ValidationException("Amount must be positive");
    }
    if (dto.getCategoryId() == null) {
        throw new ValidationException("Category is required");
    }
    Category category = categoryRepository.findById(dto.getCategoryId())
        .orElseThrow(() -> new CategoryNotFoundException(dto.getCategoryId()));
    if (!category.isActive()) {
        throw new ValidationException("Category is not active");
    }
    // ... more validation and processing
    return result;
}

// ✅ Good - Lower complexity (extracted validation)
public TransactionDTO processTransaction(TransactionDTO dto) {
    validateTransaction(dto);
    Category category = findAndValidateCategory(dto.getCategoryId());
    return createTransaction(dto, category);
}

private void validateTransaction(TransactionDTO dto) {
    if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new ValidationException("Amount must be positive");
    }
    if (dto.getCategoryId() == null) {
        throw new ValidationException("Category is required");
    }
}

private Category findAndValidateCategory(Long categoryId) {
    Category category = categoryRepository.findById(categoryId)
        .orElseThrow(() -> new CategoryNotFoundException(categoryId));
    if (!category.isActive()) {
        throw new ValidationException("Category is not active");
    }
    return category;
}
```

**Use Strategy Pattern for Complex Conditionals**:
```java
// ❌ Bad - High complexity (nested conditionals)
public BigDecimal calculateFee(Transaction transaction) {
    if (transaction.getType() == TransactionType.INCOME) {
        if (transaction.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            return transaction.getAmount().multiply(new BigDecimal("0.05"));
        } else {
            return transaction.getAmount().multiply(new BigDecimal("0.02"));
        }
    } else if (transaction.getType() == TransactionType.EXPENSE) {
        // ... more nested conditions
    }
    // ...
}

// ✅ Good - Lower complexity (strategy pattern)
public BigDecimal calculateFee(Transaction transaction) {
    return feeCalculatorFactory.getCalculator(transaction.getType())
        .calculate(transaction);
}
```

**Replace Complex Conditionals with Polymorphism**:
```java
// ❌ Bad - High complexity (switch/if-else chain)
public void processTransaction(Transaction transaction) {
    switch (transaction.getType()) {
        case INCOME:
            processIncome(transaction);
            break;
        case EXPENSE:
            processExpense(transaction);
            break;
        // ... many cases
    }
}

// ✅ Good - Lower complexity (polymorphism)
public void processTransaction(Transaction transaction) {
    transaction.process(); // Delegates to specific implementation
}
```

### 6. Testing
- Write comprehensive unit tests
- Maintain high code coverage (80% minimum)
- Test edge cases and error conditions
- Use meaningful test names (should[ExpectedBehavior]When[StateUnderTest])
- Test behavior, not implementation
- Follow AAA pattern (Arrange, Act, Assert)
- See [Testing Best Practices](testing/BEST_PRACTICES.md) for detailed guidelines

### 7. Security
- Follow security best practices
- Validate all inputs (use Bean Validation)
- Use secure coding patterns
- Never hardcode secrets or credentials
- Use Spring Security for authentication/authorization
- Sanitize user inputs
- Use parameterized queries (JPA/Hibernate handles this)
- Regular security audits using `scripts/modules/security-check.sh`

### 8. Performance Guidelines

#### Database Optimization
```java
// ❌ Bad - N+1 query problem
List<Transaction> transactions = repository.findAll();
transactions.forEach(t -> t.getCategory().getName()); // Extra queries

// ✅ Good - Use fetch joins
@Query("SELECT t FROM Transaction t JOIN FETCH t.category")
List<Transaction> findAllWithCategory();
```

#### Caching Strategy
```java
// Use Spring Cache for expensive operations
@Cacheable(value = "categories", key = "#id")
public CategoryDTO findCategoryById(Long id) {
    return categoryRepository.findById(id)
        .map(CategoryMapper::toDTO)
        .orElseThrow(() -> new CategoryNotFoundException(id));
}
```

#### Avoid Performance Anti-patterns
- Don't load entire collections unnecessarily
- Use pagination for large datasets
- Avoid excessive logging in production
- Use lazy loading appropriately with JPA
- Profile before optimizing

### 9. Error Handling

```java
// ✅ Good - Descriptive error messages
public TransactionDTO findById(Long id) {
    return repository.findById(id)
        .map(TransactionMapper::toDTO)
        .orElseThrow(() -> new TransactionNotFoundException(
            "Transaction with ID " + id + " not found"
        ));
}

// ✅ Good - Centralized exception handling
@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(TransactionNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(TransactionNotFoundException e) {
        return ResponseEntity.status(404)
            .body(new ErrorResponse(e.getMessage()));
    }
}
```

## Continuous Improvement

### Regular Code Reviews
1. **Focus on quality, not just functionality**
2. **Review for**: Code style, test coverage, security, performance
3. **Use checklists** to ensure consistency
4. **Provide constructive feedback**

### Refactoring Guidelines
1. **Identify technical debt** during code reviews
2. **Refactor incrementally** - small, safe changes
3. **Maintain test coverage** during refactoring
4. **Document complex refactoring decisions**

### Team Practices
1. **Pair programming** for knowledge sharing
2. **Code review** for all changes
3. **Regular retrospectives** on code quality
4. **Share learnings** through documentation

### Monitoring Quality Metrics
- Track trends in code quality metrics over time
- Set up alerts for quality gate failures
- Review SonarQube reports regularly
- Address technical debt proactively

## Success Metrics and Targets

### Code Quality Targets
- **Checkstyle violations**: 0 (complexity errors), < 100 (warnings for file/method length)
- **PMD violations**: 0 (Priority 1 - complexity), < 10 (Priority 2-3)
- **SpotBugs issues**: 0 (High/Critical), < 5 (Medium)
- **Cognitive complexity**: < 15 per method (production), < 20 per method (tests)
- **Cyclomatic complexity**: < 15 per method (production), < 20 per method (tests)
- **Code duplication**: < 3% (SonarQube)

### Test Coverage Targets
- **Overall coverage**: > 80%
- **Line coverage**: > 80%
- **Branch coverage**: > 80%
- **Critical business logic**: > 95%

### Performance Targets
- **API response time**: < 200ms (p95)
- **Database query time**: < 100ms (p95)
- **Test execution time**: < 5 minutes (full suite)

### Maintainability Targets
- **Maintainability Rating**: A (SonarQube)
- **Technical Debt Ratio**: < 5%
- **Code Smells**: < 50 per 1000 lines

## Automated Quality Checks

### Pre-commit Checks
Use the provided scripts for quality checks:

```bash
# Code quality check
./scripts/modules/code-quality.sh

# Security check
./scripts/modules/security-check.sh

# Full quality analysis
./scripts/dev.sh quality-local
```

### CI/CD Integration
Quality gates should be enforced in CI/CD pipeline:
1. **Checkstyle** - Fail build on complexity violations (errors), allow warnings for file/method length
2. **PMD** - Fail build on high-priority complexity violations (Priority 1), allow warnings for design issues (Priority 2-3)
3. **SpotBugs** - Fail build on critical issues
4. **Test Coverage** - Fail build if below threshold
5. **Security Scan** - Block merge on critical vulnerabilities

**Build Configuration**:
- `checkstyle.ignoreFailures = false` - Build fails on complexity errors
- `pmd.ignoreFailures = false` - Build fails on Priority 1 violations
- File/method length violations are warnings only and don't block the build

## Maintenance

### Regular Tasks
1. **Update tool versions** in `build.gradle`
2. **Review and update rules** as needed
3. **Monitor quality metrics** trends
4. **Address technical debt** regularly
5. **Update documentation** when rules change

### Adding New Rule
1. Identify the most appropriate tool
2. Configure the rule in the specific file
3. Synchronize with other tools if necessary
4. Update this documentation
5. Test with `./gradlew clean build`

### Excluding False Positive
1. Identify the false positive pattern
2. Add exclusion in the appropriate file
3. Document the reason for exclusion
4. Verify if it affects other tools

### Version Updates
- Check for new tool versions monthly
- Test new versions in development
- Update configurations as needed
- Document breaking changes

## Troubleshooting

### Common Issues

1. **Checkstyle Violations**:
   - Review violation messages
   - Update code to match standards
   - Consider rule customization if needed

2. **PMD Warnings**:
   - Address high-priority issues first
   - Refactor complex methods
   - Improve code design

3. **SpotBugs Findings**:
   - Investigate potential bugs
   - Fix resource leaks
   - Address thread safety issues

4. **Coverage Issues**:
   - Add missing test cases
   - Improve test coverage
   - Review uncovered code

5. **SonarQube Issues**:
   - Address security hotspots
   - Reduce technical debt
   - Improve maintainability

### Rule Conflicts
- Check if the same rule is configured in multiple tools
- Adjust priorities or thresholds as needed
- Document configuration decisions

### Performance
- Optimize exclusions to reduce analysis time
- Use `effort = 'max'` only when necessary
- Consider incremental analysis for large projects

### Configuration Customization

1. **Checkstyle**: Modify `checkstyle.xml` for project-specific rules
2. **PMD**: Update `pmd-ruleset.xml` for custom rule configurations
3. **SpotBugs**: Adjust include/exclude filters in XML files
4. **JaCoCo**: Modify exclusions and thresholds in `build.gradle`
5. **SonarQube**: Update `sonar-project.properties` for project settings

## Reports and Artifacts

### Generated Reports
- **Checkstyle**: `build/reports/checkstyle/main.xml`
- **PMD**: `build/reports/pmd/main.xml`
- **SpotBugs**: `build/reports/spotbugs/`
- **JaCoCo**: `build/reports/jacoco/`
- **SonarQube**: Online dashboard

### Report Locations
- HTML reports: `build/reports/`
- XML reports: `build/reports/`
- Coverage reports: `build/reports/jacoco/`

## Available Gradle Tasks

```bash
# Run all quality checks
./gradlew clean build

# Run specific tools
./gradlew checkstyleMain
./gradlew pmdMain
./gradlew spotbugsMain
./gradlew jacocoTestCoverageVerification

# Generate reports
./gradlew checkstyleMain
./gradlew pmdMain
./gradlew spotbugsMain
./gradlew jacocoTestReport

# SonarQube analysis
./gradlew sonarqube
```

This comprehensive code quality setup ensures that the Finance Control project maintains high standards of code quality, security, and maintainability throughout its development lifecycle.
