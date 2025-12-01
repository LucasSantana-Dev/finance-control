# Development Guide

This comprehensive guide covers all aspects of development in the Finance Control application, including code quality standards, dependency management, base classes architecture, MapStruct integration, naming conventions, and development workflow.

## Table of Contents

1. [Code Quality Standards and Tools](#1-code-quality-standards-and-tools)
2. [Dependency Management](#2-dependency-management)
3. [Base Classes Usage Guide](#3-base-classes-usage-guide)
4. [MapStruct Integration](#4-mapstruct-integration)
5. [Naming Conventions with Examples](#5-naming-conventions-with-examples)
6. [Development Workflow](#6-development-workflow)

---

## 1. Code Quality Standards and Tools

This section describes the comprehensive code quality tools implemented in the Finance Control project to ensure high code quality, proper linting, and code coverage validation.

### Overview of Quality Tools

The project implements the following code quality tools:

1. **Checkstyle** - Code style and formatting enforcement
2. **PMD** - Static code analysis for potential bugs and code smells
3. **SpotBugs** - Bytecode analysis for potential bugs
4. **JaCoCo** - Code coverage analysis and reporting
5. **SonarQube** - Comprehensive code quality analysis

### 1.1 Checkstyle

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

### 1.2 PMD

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

### 1.3 SpotBugs

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

### 1.4 JaCoCo

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

### 1.5 SonarQube

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

### 1.6 Code Quality Tools Synchronization

#### Synchronized Rules

##### Cognitive Complexity (Primary Metric)

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

##### Method Size (Warning Only)

- **Checkstyle**: `MethodLength` - max 150 lines (warning only, not blocking)
- **PMD**: `ExcessiveMethodLength` - minimum 150 lines (priority 3, warning only)
- **SonarQube**: `sonar.size.limit.function=150` (informational)

**Note**: Method length violations are warnings only. If a method exceeds 150 lines but has low cognitive complexity, it may be acceptable. However, consider refactoring for better maintainability.

##### Class/File Size (Warning Only)

- **Checkstyle**: `FileLength` - max 500 lines (warning only, not blocking)
- **PMD**: `ExcessiveClassLength` - minimum 500 lines (priority 3, warning only)
- **SonarQube**: `sonar.size.limit.class=2000` (informational)

**Note**: File length violations are warnings only. Large files with low cognitive complexity may be acceptable, but consider splitting for better organization.

#### Synchronized Exclusions

**Exclusion Patterns**:
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

**Excluded Packages**:
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

### 1.7 Gradle Configuration

#### Dependencies

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

#### SonarQube Integration

```gradle
sonarqube {
    properties {
        property 'sonar.java.checkstyle.reportPaths', 'build/reports/checkstyle/main.xml'
        property 'sonar.java.pmd.reportPaths', 'build/reports/pmd/main.xml'
        property 'sonar.java.spotbugs.reportPaths', 'build/reports/spotbugs/main.xml'
    }
}
```

### 1.8 Best Practices

#### Code Organization

**File Structure**:
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

**Naming Conventions**:
- **Classes**: PascalCase (e.g., `TransactionService`, `UserController`)
- **Methods/Variables**: camelCase (e.g., `findUserById`, `isTransactionValid`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`, `DEFAULT_PAGE_SIZE`)
- **Packages**: lowercase with dots (e.g., `com.finance_control.transactions.service`)
- **Test Classes**: Same as class with `Test` suffix (e.g., `TransactionServiceTest`)

**Import Organization**:
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

#### Type Safety and Java Best Practices

**Use Strong Typing**:
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

**Use Java 21 Features When Appropriate**:
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

**Null Safety**:
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

#### Service Design Patterns

**Single Responsibility Principle**:
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

**Dependency Injection**:
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

#### Performance Guidelines

**Database Optimization**:
```java
// ❌ Bad - N+1 query problem
List<Transaction> transactions = repository.findAll();
transactions.forEach(t -> t.getCategory().getName()); // Extra queries

// ✅ Good - Use fetch joins
@Query("SELECT t FROM Transaction t JOIN FETCH t.category")
List<Transaction> findAllWithCategory();
```

**Caching Strategy**:
```java
// Use Spring Cache for expensive operations
@Cacheable(value = "categories", key = "#id")
public CategoryDTO findCategoryById(Long id) {
    return categoryRepository.findById(id)
        .map(CategoryMapper::toDTO)
        .orElseThrow(() -> new CategoryNotFoundException(id));
}
```

**Avoid Performance Anti-patterns**:
- Don't load entire collections unnecessarily
- Use pagination for large datasets
- Avoid excessive logging in production
- Use lazy loading appropriately with JPA
- Profile before optimizing

#### Error Handling

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

### 1.9 IDE Integration

#### IntelliJ IDEA

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

#### Eclipse

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

### 1.10 CI/CD Integration

#### GitHub Actions

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

### 1.11 Quality Gates

- **Checkstyle**: Fail build on complexity violations (errors), allow warnings for file/method length
- **PMD**: Fail build on high-priority complexity violations (Priority 1), allow warnings for design issues (Priority 2-3)
- **SpotBugs**: Fail build on critical issues
- **Test Coverage**: Fail build if below threshold
- **Security Scan**: Block merge on critical vulnerabilities

### 1.12 Available Gradle Tasks

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

---

## 2. Dependency Management

This section explains the key dependencies used in the finance-control application and their purposes.

### 2.1 Core Spring Boot Dependencies

#### Spring Boot Starters

- **spring-boot-starter-web**: Web application support with embedded Tomcat
- **spring-boot-starter-data-jpa**: JPA and Hibernate support
- **spring-boot-starter-security**: Spring Security for authentication and authorization
- **spring-boot-starter-validation**: Bean Validation support
- **spring-boot-starter-actuator**: Production monitoring and metrics

#### Development Tools

- **spring-boot-devtools**: Development-time features (auto-restart, live reload)

### 2.2 Database & Migration

#### PostgreSQL

- **postgresql**: PostgreSQL JDBC driver
- **flyway-core**: Database migration tool
- **flyway-database-postgresql**: PostgreSQL-specific Flyway support

#### Testing Database

- **h2**: In-memory database for testing

### 2.3 Security & Authentication

#### JWT (JSON Web Tokens)

- **jjwt-api**: JWT API for creating and parsing tokens
- **jjwt-impl**: JWT implementation
- **jjwt-jackson**: Jackson integration for JWT

### 2.4 API Documentation

#### OpenAPI/Swagger

- **springdoc-openapi-starter-webmvc-ui**: OpenAPI 3 documentation with Swagger UI

### 2.5 Testing Dependencies

#### Core Testing

- **spring-boot-starter-test**: Spring Boot test support
- **spring-security-test**: Security testing utilities

#### End-to-End Testing

- **selenium-java**: Selenium WebDriver for browser automation
- **selenium-chrome-driver**: Chrome WebDriver

#### Container Testing

- **testcontainers**: Docker-based testing
- **testcontainers-junit-jupiter**: JUnit 5 integration
- **testcontainers-postgresql**: PostgreSQL container support

### 2.6 Utilities

#### Lombok

- **lombok**: Reduces boilerplate code with annotations
- **lombok-maven-plugin**: Maven integration for annotation processing

#### Environment Configuration

- **spring-dotenv**: .env file support for configuration

### 2.7 Build & Deployment

#### Maven Plugins

- **maven-compiler-plugin**: Java compilation with Lombok support
- **spring-boot-maven-plugin**: Spring Boot application packaging
- **flyway-maven-plugin**: Database migration via Maven

### 2.8 Version Information

#### Spring Boot Version

- **3.5.3**: Latest stable version with Java 24 support

#### Java Version

- **21**: Latest LTS version with modern features

### 2.9 Dependency Management Best Practices

#### Version Management

- Use Spring Boot parent for version management
- Keep dependencies up to date
- Use specific versions for critical dependencies

#### Security Considerations

- Regularly update security-related dependencies
- Use Spring Security for authentication
- Implement proper JWT handling

#### Testing Strategy

- Use appropriate testing dependencies for each test type
- Maintain test isolation
- Use realistic test data

#### Performance Considerations

- Use connection pooling for database connections
- Implement caching where appropriate
- Monitor application performance with Actuator

### 2.10 Configuration Properties

#### Database Configuration

```properties
spring.datasource.url=${DB_URL}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
```

#### JPA Configuration

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
```

#### Flyway Configuration

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

---

## 3. Base Classes Usage Guide

This section describes the base classes architecture for entities, DTOs, and controllers in the finance-control application.

> **Rule Reference**: For concise base class usage patterns, see `.cursor/rules/base-classes-usage.mdc`

### 3.1 Overview

The base classes provide a consistent foundation for common patterns across the application, reducing code duplication and ensuring consistency.

### 3.2 Base Entity Classes

#### BaseEntity<T>

**Location**: `src/main/java/com/finance_control/shared/model/BaseEntity.java`

**Purpose**: Provides common fields and annotations for all entities.

**Features**:
- `@Id` with `@GeneratedValue(strategy = GenerationType.IDENTITY)`
- `createdAt` and `updatedAt` audit fields with `@CreatedDate` and `@LastModifiedDate`
- `@EntityListeners(AuditingEntityListener.class)` for automatic audit field management
- Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@MappedSuperclass` to allow inheritance in JPA entities

**Usage Example**:
```java
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity<Long> {

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 8)
    @Column(nullable = false)
    private String password;

    // ... other fields
}
```

**Benefits**:
- Consistent ID and audit field management
- Reduced boilerplate code
- Automatic audit field population
- Type-safe ID handling

### 3.3 Base DTO Classes

#### BaseDTO<T>

**Location**: `src/main/java/com/finance_control/shared/dto/BaseDTO.java`

**Purpose**: Base class for response DTOs that include ID and audit fields.

**Features**:
- Generic ID field
- `createdAt` and `updatedAt` fields
- Lombok annotations: `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`

**Usage Example**:
```java
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends BaseDTO<Long> {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;

    private Boolean isActive;
}
```

#### BaseCreateDTO

**Location**: `src/main/java/com/finance_control/shared/dto/BaseCreateDTO.java`

**Purpose**: Base class for create DTOs that exclude ID and audit fields.

**Features**:
- No ID or audit fields (appropriate for creation)
- Lombok annotations: `@Data`, `@NoArgsConstructor`

**Usage Example**:
```java
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserCreateDTO extends BaseCreateDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    private String fullName;
}
```

### 3.4 Base API Response Classes

#### ApiResponse<T>

**Location**: `src/main/java/com/finance_control/shared/dto/ApiResponse.java`

**Purpose**: Standardized wrapper for all successful API responses.

**Features**:
- Generic type parameter for response data
- Success flag and descriptive message
- Timestamp for debugging and logging
- Path information for request tracing
- Static factory methods for easy creation

**Usage Example**:
```java
// Success response
ApiResponse<UserDTO> response = ApiResponse.success(userDTO, "User created successfully");

// Error response
ApiResponse<UserDTO> errorResponse = ApiResponse.error("User not found");

// With path information
ApiResponse<UserDTO> responseWithPath = ApiResponse.error("Validation failed", "/api/users");
```

**Response Structure**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "fullName": "John Doe"
  },
  "message": "User created successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}
```

#### ErrorResponse

**Location**: `src/main/java/com/finance_control/shared/dto/ErrorResponse.java`

**Purpose**: Standardized structure for all error responses.

**Features**:
- Error type classification
- Descriptive error message
- Request path for debugging
- Timestamp for logging
- Validation error details when applicable

**Usage Example**:
```java
ErrorResponse error = new ErrorResponse(
    "VALIDATION_ERROR",
    "Validation failed",
    "/api/users",
    LocalDateTime.now(),
    validationErrors
);
```

**Response Structure**:
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/users",
  "timestamp": "2024-01-15T10:30:00",
  "validationErrors": [
    {
      "field": "email",
      "message": "Email must be valid",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

### 3.5 Base Controller Classes

#### BaseController Interface

**Location**: `src/main/java/com/finance_control/shared/controller/BaseController.java`

**Purpose**: Defines the contract for standard CRUD operations.

**Features**:
- Standard CRUD endpoints: `findAll`, `findById`, `create`, `update`, `delete`
- Proper HTTP status codes and response types
- Generic type parameters for flexibility

#### AbstractBaseController

**Location**: `src/main/java/com/finance_control/shared/controller/AbstractBaseController.java`

**Purpose**: Provides implementation of standard CRUD operations.

**Features**:
- Implements all BaseController methods
- Proper error handling and validation
- `@Valid` annotations for request body validation
- Consistent response patterns

#### BaseRestController

**Location**: `src/main/java/com/finance_control/shared/controller/BaseRestController.java`

**Purpose**: Alternative base controller that doesn't implement the interface, allowing more flexibility.

**Features**:
- Same CRUD operations as AbstractBaseController
- Can be extended for controllers with custom operations
- No interface constraint

### 3.6 Migration Guide

#### Migrating Entities

1. **Extend BaseEntity**:
   ```java
   // Before
   @Entity
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   @EntityListeners(AuditingEntityListener.class)
   public class User {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       @CreatedDate
       @Column(name = "created_at", nullable = false, updatable = false)
       private LocalDateTime createdAt;

       @LastModifiedDate
       @Column(name = "updated_at")
       private LocalDateTime updatedAt;

       // ... other fields
   }

   // After
   @Entity
   @Data
   @NoArgsConstructor
   @EqualsAndHashCode(callSuper = true)
   public class User extends BaseEntity<Long> {
       // ... only domain-specific fields
   }
   ```

2. **Remove duplicate annotations and fields**:
   - Remove `@Id`, `@GeneratedValue`, `createdAt`, `updatedAt`
   - Remove `@EntityListeners`, `@AllArgsConstructor`
   - Add `@EqualsAndHashCode(callSuper = true)`

#### Migrating DTOs

1. **Response DTOs**:
   ```java
   // Before
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class UserDTO {
       private Long id;
       private String email;
       private String fullName;
       private LocalDateTime createdAt;
       private LocalDateTime updatedAt;
   }

   // After
   @Data
   @NoArgsConstructor
   @EqualsAndHashCode(callSuper = true)
   public class UserDTO extends BaseDTO<Long> {
       private String email;
       private String fullName;
   }
   ```

2. **Create DTOs**:
   ```java
   // Before
   @Data
   @NoArgsConstructor
   @AllArgsConstructor
   public class UserCreateDTO {
       private String email;
       private String password;
       private String fullName;
   }

   // After
   @Data
   @NoArgsConstructor
   @EqualsAndHashCode(callSuper = true)
   public class UserCreateDTO extends BaseCreateDTO {
       private String email;
       private String password;
       private String fullName;
   }
   ```

#### Migrating Controllers

**Option 1: Use AbstractBaseController (Interface-based)**
```java
@RestController
@RequestMapping("/users")
public class UserController extends AbstractBaseController<User, Long, UserCreateDTO, UserCreateDTO, UserDTO> {

    public UserController(UserService userService) {
        super(userService);
    }

    // Custom methods can be added here
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDTO> findByEmail(@PathVariable String email) {
        // Custom implementation
    }
}
```

**Option 2: Use BaseRestController (Direct inheritance)**
```java
@RestController
@RequestMapping("/users")
public class UserController extends BaseRestController<User, Long, UserCreateDTO, UserCreateDTO, UserDTO> {

    public UserController(UserService userService) {
        super(userService);
    }

    // Custom methods can be added here
    @GetMapping("/by-email/{email}")
    public ResponseEntity<UserDTO> findByEmail(@PathVariable String email) {
        // Custom implementation
    }
}
```

### 3.7 Best Practices

#### Entity Design

- Always extend `BaseEntity<T>` for new entities
- Use `@EqualsAndHashCode(callSuper = true)` to include parent fields in equals/hashCode
- Keep domain-specific fields only in the entity class

#### DTO Design

- Use `BaseDTO<T>` for response DTOs
- Use `BaseCreateDTO` for create DTOs
- Create separate `UpdateDTO` classes if update operations differ from create operations
- Use `@EqualsAndHashCode(callSuper = true)` for proper inheritance

#### Controller Design

- Choose between `AbstractBaseController` (interface-based) or `BaseRestController` (direct inheritance)
- Add custom endpoints as needed
- Override base methods if custom behavior is required

#### Service Design

- Services should extend `BaseService<T, I, C, U, R>` for standard CRUD operations
- Implement abstract methods: `mapToEntity`, `updateEntityFromDTO`, `mapToResponseDTO`
- Override validation methods if needed

#### Response Wrapping

- Always wrap successful responses in `ApiResponse<T>`
- Use descriptive messages for each operation
- Include appropriate HTTP status codes

#### Error Handling

- Use `ErrorResponse` for all error scenarios
- Provide meaningful error messages
- Include validation details when applicable
- Use standardized error types

### 3.8 Benefits

1. **Consistency**: All entities and DTOs follow the same patterns
2. **Reduced Boilerplate**: Common fields and annotations are inherited
3. **Type Safety**: Generic types ensure proper ID handling
4. **Maintainability**: Changes to base classes automatically apply to all implementations
5. **Audit Support**: Automatic audit field management
6. **Validation**: Built-in validation support with `@Valid` annotations

---

## 4. MapStruct Integration

This section describes the MapStruct integration in the Finance Control application, which provides type-safe, compile-time DTO-entity mapping.

### 4.1 What is MapStruct?

MapStruct is a code generation library that simplifies the mapping between Java bean types based on a convention over configuration approach. It generates mapping code at compile time, ensuring type safety and performance.

### 4.2 Benefits

- **Type Safety**: Compile-time validation of mappings
- **Performance**: No reflection overhead at runtime
- **Maintainability**: Generated code is easy to debug and understand
- **IDE Support**: Full IDE support with autocomplete and refactoring
- **Null Safety**: Built-in null handling and validation

### 4.3 Implementation

#### Dependencies

The following dependencies are added to `build.gradle`:

```gradle
// MapStruct for DTO-Entity mapping (Industry Standard)
implementation 'org.mapstruct:mapstruct:1.5.5.Final'
annotationProcessor 'org.mapstruct:mapstruct-processor:1.5.5.Final'
```

#### Mapper Interfaces

**FinancialGoalMapper**:
```java
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {

    FinancialGoalDTO toDTO(FinancialGoal entity);

    FinancialGoal toEntity(FinancialGoalDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FinancialGoal toEntityForCreate(FinancialGoalDTO dto);

    @Mapping(target = "createdAt", ignore = true)
    FinancialGoal toEntityForUpdate(FinancialGoalDTO dto);
}
```

**TransactionMapper**:
```java
@Mapper(componentModel = "spring", uses = {TransactionResponsiblesMapper.class})
public interface TransactionMapper {

    TransactionDTO toDTO(Transaction entity);

    Transaction toEntity(TransactionDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Transaction toEntityForCreate(TransactionDTO dto);

    @Mapping(target = "createdAt", ignore = true)
    Transaction toEntityForUpdate(TransactionDTO dto);
}
```

**UserMapper**:
```java
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User entity);

    User toEntity(UserDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntityForCreate(UserDTO dto);

    @Mapping(target = "createdAt", ignore = true)
    User toEntityForUpdate(UserDTO dto);
}
```

### 4.4 Usage in Services

#### Service Implementation

```java
@Service
@RequiredArgsConstructor
public class FinancialGoalServiceImpl implements FinancialGoalService {

    private final FinancialGoalRepository repository;
    private final FinancialGoalMapper mapper;

    @Override
    public FinancialGoalDTO create(FinancialGoalDTO dto) {
        FinancialGoal entity = mapper.toEntityForCreate(dto);
        FinancialGoal saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Override
    public FinancialGoalDTO update(Long id, FinancialGoalDTO dto) {
        FinancialGoal existing = repository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Goal not found"));

        FinancialGoal updated = mapper.toEntityForUpdate(dto);
        updated.setId(id);
        updated.setCreatedAt(existing.getCreatedAt());

        FinancialGoal saved = repository.save(updated);
        return mapper.toDTO(saved);
    }
}
```

### 4.5 Advanced Features

#### Custom Mapping Methods

```java
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {

    @Mapping(target = "completionPercentage", expression = "java(calculateCompletion(entity))")
    FinancialGoalDTO toDTO(FinancialGoal entity);

    default Double calculateCompletion(FinancialGoal entity) {
        if (entity.getTargetAmount() == null || entity.getTargetAmount().equals(BigDecimal.ZERO)) {
            return 0.0;
        }
        return entity.getCurrentAmount()
            .divide(entity.getTargetAmount(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }
}
```

#### Date and Time Mapping

```java
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {

    @Mapping(target = "targetDate", source = "targetDate", dateFormat = "yyyy-MM-dd")
    FinancialGoalDTO toDTO(FinancialGoal entity);

    @Mapping(target = "targetDate", source = "targetDate", dateFormat = "yyyy-MM-dd")
    FinancialGoal toEntity(FinancialGoalDTO dto);
}
```

#### Collection Mapping

```java
@Mapper(componentModel = "spring")
public interface TransactionMapper {

    List<TransactionDTO> toDTOList(List<Transaction> entities);

    List<Transaction> toEntityList(List<TransactionDTO> dtos);
}
```

### 4.6 Best Practices

#### 1. Use Separate Methods for Create and Update

```java
@Mapper(componentModel = "spring")
public interface EntityMapper {

    // For creating new entities
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Entity toEntityForCreate(EntityDTO dto);

    // For updating existing entities
    @Mapping(target = "createdAt", ignore = true)
    Entity toEntityForUpdate(EntityDTO dto);
}
```

#### 2. Handle Null Values

```java
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EntityMapper {
    // Mappings will ignore null values in source
}
```

#### 3. Use Component Model

Always use `componentModel = "spring"` to integrate with Spring's dependency injection:

```java
@Mapper(componentModel = "spring")
public interface EntityMapper {
    // Spring will manage this as a bean
}
```

#### 4. Custom Validation

```java
@Mapper(componentModel = "spring")
public interface EntityMapper {

    @Mapping(target = "email", source = "email", qualifiedByName = "validateEmail")
    Entity toEntity(EntityDTO dto);

    @Named("validateEmail")
    default String validateEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        return email.toLowerCase();
    }
}
```

### 4.7 Testing MapStruct Mappers

#### Unit Testing Mappers

```java
@ExtendWith(MockitoExtension.class)
class FinancialGoalMapperTest {

    @InjectMocks
    private FinancialGoalMapperImpl mapper;

    @Test
    void shouldMapEntityToDTO() {
        // Given
        FinancialGoal entity = new FinancialGoal();
        entity.setId(1L);
        entity.setName("Test Goal");
        entity.setTargetAmount(new BigDecimal("1000.00"));

        // When
        FinancialGoalDTO dto = mapper.toDTO(entity);

        // Then
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test Goal");
        assertThat(dto.getTargetAmount()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    void shouldMapDTOToEntityForCreate() {
        // Given
        FinancialGoalDTO dto = new FinancialGoalDTO();
        dto.setName("Test Goal");
        dto.setTargetAmount(new BigDecimal("1000.00"));

        // When
        FinancialGoal entity = mapper.toEntityForCreate(dto);

        // Then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isEqualTo("Test Goal");
        assertThat(entity.getTargetAmount()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }
}
```

### 4.8 Migration from Manual Mapping

#### Before (Manual Mapping)

```java
public FinancialGoalDTO toDTO(FinancialGoal entity) {
    FinancialGoalDTO dto = new FinancialGoalDTO();
    dto.setId(entity.getId());
    dto.setName(entity.getName());
    dto.setDescription(entity.getDescription());
    dto.setTargetAmount(entity.getTargetAmount());
    dto.setCurrentAmount(entity.getCurrentAmount());
    dto.setTargetDate(entity.getTargetDate());
    dto.setIsCompleted(entity.getIsCompleted());
    dto.setUserId(entity.getUser().getId());
    return dto;
}
```

#### After (MapStruct)

```java
@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {
    FinancialGoalDTO toDTO(FinancialGoal entity);
}
```

### 4.9 Troubleshooting

#### Common Issues

1. **Compilation Errors**: Ensure annotation processor is properly configured
2. **Null Pointer Exceptions**: Use null value mapping strategies
3. **Circular Dependencies**: Use `@Context` for complex mappings
4. **Performance Issues**: Avoid complex expressions in mappings

#### Debugging

Enable debug logging for MapStruct:

```properties
logging.level.org.mapstruct=DEBUG
```

---

## 5. Naming Conventions with Examples

This section shows examples of how to apply the naming conventions consistently across different modules.

> **Rule Reference**: For concise naming patterns and conventions, see `.cursor/rules/naming-conventions.mdc`

### 5.1 Transaction Module (Current)

#### Service Layer
- **Interface**: `TransactionService.java`
- **Implementation**: `DefaultTransactionService.java`

#### Controller Layer
- **Interface**: `TransactionController.java` (if needed)
- **Implementation**: `DefaultTransactionController.java` (if needed)

#### Repository Layer
- **Interface**: `TransactionRepository.java`
- **Custom Implementation**: `TransactionRepositoryImpl.java` (if needed)

#### DTOs
- **Create**: `TransactionCreateDTO.java`
- **Update**: `TransactionCreateDTO.java` (currently using same as create)
- **Response**: `TransactionDTO.java`

### 5.2 User Module (Future)

#### Service Layer
- **Interface**: `UserService.java`
- **Implementation**: `DefaultUserService.java`

#### Controller Layer
- **Interface**: `UserController.java`
- **Implementation**: `DefaultUserController.java`

#### Repository Layer
- **Interface**: `UserRepository.java`

#### DTOs
- **Create**: `UserCreateDTO.java`
- **Update**: `UserUpdateDTO.java`
- **Response**: `UserDTO.java`

### 5.3 Financial Goal Module (Future)

#### Service Layer
- **Interface**: `FinancialGoalService.java`
- **Implementation**: `DefaultFinancialGoalService.java`

#### Controller Layer
- **Interface**: `FinancialGoalController.java`
- **Implementation**: `DefaultFinancialGoalController.java`

#### Repository Layer
- **Interface**: `FinancialGoalRepository.java`

#### DTOs
- **Create**: `FinancialGoalCreateDTO.java`
- **Update**: `FinancialGoalUpdateDTO.java`
- **Response**: `FinancialGoalDTO.java`

### 5.4 Alternative Implementations

When you need multiple implementations of the same service:

#### Cached Implementation
- **Interface**: `TransactionService.java`
- **Default Implementation**: `DefaultTransactionService.java`
- **Cached Implementation**: `CachedTransactionService.java`

#### Mock Implementation (for testing)
- **Interface**: `TransactionService.java`
- **Default Implementation**: `DefaultTransactionService.java`
- **Mock Implementation**: `MockTransactionService.java`

### 5.5 Benefits of This Naming Convention

1. **Clarity**: It's immediately clear which file is the interface vs implementation
2. **Consistency**: All modules follow the same pattern
3. **Scalability**: Easy to add alternative implementations
4. **Spring Integration**: Works well with Spring's dependency injection
5. **IDE Support**: IDEs can easily distinguish between interfaces and implementations

### 5.6 File Structure Example

```
src/main/java/com/finance_control/
├── transactions/
│   ├── service/
│   │   ├── TransactionService.java          (interface)
│   │   └── DefaultTransactionService.java   (implementation)
│   ├── controller/
│   │   ├── TransactionController.java       (interface)
│   │   └── DefaultTransactionController.java (implementation)
│   ├── repository/
│   │   └── TransactionRepository.java       (interface)
│   └── dto/
│       ├── TransactionCreateDTO.java
│       ├── TransactionUpdateDTO.java
│       └── TransactionDTO.java
├── users/
│   ├── service/
│   │   ├── UserService.java                 (interface)
│   │   └── DefaultUserService.java          (implementation)
│   └── ...
└── goals/
    ├── service/
    │   ├── FinancialGoalService.java        (interface)
    │   └── DefaultFinancialGoalService.java (implementation)
    └── ...
```

---

## 6. Development Workflow

This section describes the recommended development workflow for the Finance Control application.

### 6.1 Local Development

#### Setup

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd finance-control
   ```

2. **Configure environment**:
   - Copy `.env.example` to `.env`
   - Update database credentials and other configuration

3. **Start database**:
   ```bash
   # Using Docker
   docker-compose up -d postgres
   ```

4. **Run the application**:
   ```bash
   ./gradlew bootRun
   ```

#### Development Cycle

1. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make changes following the coding standards**:
   - Follow naming conventions
   - Use base classes where appropriate
   - Implement MapStruct mappers for DTO-entity conversion
   - Write unit tests for new code

3. **Run quality checks locally**:
   ```bash
   # Code quality check
   ./scripts/modules/code-quality.sh

   # Security check
   ./scripts/modules/security-check.sh

   # Full quality analysis
   ./scripts/dev.sh quality-local
   ```

4. **Run tests**:
   ```bash
   ./gradlew clean test
   ```

5. **Check code coverage**:
   ```bash
   ./gradlew jacocoTestReport
   ./gradlew jacocoTestCoverageVerification
   ```

6. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

7. **Push to remote**:
   ```bash
   git push origin feature/your-feature-name
   ```

### 6.2 Code Review Process

1. **Create a pull request** on GitHub
2. **Ensure all CI checks pass**:
   - Code quality checks (Checkstyle, PMD, SpotBugs)
   - Test coverage verification
   - Security scans
3. **Address reviewer feedback**
4. **Merge after approval**

### 6.3 Continuous Integration

The CI/CD pipeline automatically runs:

1. **Code Quality Analysis**:
   - Checkstyle (fails on complexity violations)
   - PMD (fails on high-priority violations)
   - SpotBugs (fails on critical issues)

2. **Testing**:
   - Unit tests
   - Integration tests
   - Code coverage verification (80% minimum)

3. **Security Scanning**:
   - Dependency vulnerability checks
   - Code security analysis

4. **SonarQube Analysis**:
   - Comprehensive quality metrics
   - Technical debt tracking
   - Code smell detection

### 6.4 Database Migrations

#### Creating Migrations

1. **Create a new migration file**:
   ```sql
   -- src/main/resources/db/migration/V{version}__{description}.sql
   CREATE TABLE example (
       id BIGSERIAL PRIMARY KEY,
       name VARCHAR(100) NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
   );
   ```

2. **Run migrations**:
   ```bash
   ./gradlew flywayMigrate
   ```

3. **Verify migration status**:
   ```bash
   ./gradlew flywayInfo
   ```

### 6.5 Testing Strategy

#### Unit Tests

- Test individual components in isolation
- Use Mockito for mocking dependencies
- Follow AAA pattern (Arrange, Act, Assert)
- Aim for high code coverage (80%+)

#### Integration Tests

- Test component interactions
- Use TestContainers for database tests
- Test API endpoints with MockMvc

#### End-to-End Tests

- Test complete user workflows
- Use Selenium for browser automation
- Test critical business flows

### 6.6 Documentation

#### Code Documentation

- Use Javadoc for public APIs
- Document complex business logic
- Keep comments concise and relevant

#### API Documentation

- Use OpenAPI/Swagger annotations
- Document request/response models
- Provide example payloads

### 6.7 Release Process

1. **Version bump** in `build.gradle`
2. **Update CHANGELOG.md** with release notes
3. **Create release tag**:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```
4. **Deploy to production**

### 6.8 Monitoring and Maintenance

#### Regular Tasks

1. **Update dependencies** monthly
2. **Review and address technical debt**
3. **Monitor quality metrics** in SonarQube
4. **Review security vulnerabilities**
5. **Update documentation** as needed

#### Monitoring Tools

- **Spring Boot Actuator**: Health checks and metrics
- **SonarQube**: Code quality dashboard
- **Application logs**: Error tracking and debugging

---

---

## 7. Logging

This section covers logging configuration, implementation patterns, and log organization in the Finance Control application.

### 7.1 Overview

The application uses Logback as the logging framework with a comprehensive configuration that provides:

- **Console logging** with colored output for development
- **File logging** with rotation and size limits
- **Error-only logging** for critical issues
- **Profile-specific configurations** for different environments
- **Performance optimization** with async logging
- **Base class logging** through Lombok's `@Slf4j` annotation

### 7.2 Configuration Files

#### logback-spring.xml

The main logging configuration file that defines:

- **Appenders**: Console, File, Error File, and Async File
- **Loggers**: Package-specific log levels
- **Profiles**: Environment-specific configurations

#### application.yml

Contains basic logging level overrides that can be easily modified:

```properties
# Logging Configuration
logging.level.root=INFO
logging.level.com.finance_control=DEBUG
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
logging.level.org.springframework.security=DEBUG
logging.level.org.springframework.web=DEBUG
```

### 7.3 Log Levels

#### Application Logs (`com.finance_control`)
- **Level**: DEBUG
- **Purpose**: Detailed application logic, method entry/exit, business operations
- **Use cases**: Debugging business logic, tracking user actions, performance monitoring

#### Spring Security (`org.springframework.security`)
- **Level**: DEBUG
- **Purpose**: Authentication and authorization events
- **Use cases**: Debugging security issues, tracking login attempts, permission checks

#### Spring Web (`org.springframework.web`)
- **Level**: DEBUG
- **Purpose**: HTTP request/response handling, controller operations
- **Use cases**: Debugging REST API issues, tracking request flow

#### Hibernate SQL (`org.hibernate.SQL`)
- **Level**: WARN
- **Purpose**: SQL query execution
- **Use cases**: Performance monitoring, query optimization

#### Hibernate Parameter Binding (`org.hibernate.type.descriptor.sql.BasicBinder`)
- **Level**: WARN
- **Purpose**: SQL parameter values
- **Use cases**: Debugging data issues, security auditing

### 7.4 Log Output

#### Console Output
- **Format**: `HH:mm:ss.SSS [thread] LEVEL logger - message`
- **Features**: Colored output for better readability
- **Example**: `14:30:25.123 [http-nio-${APPLICATION_PORT}-exec-1] DEBUG c.f.t.s.TransactionService - Processing transaction for user: 123`

#### File Output
- **Location**: `logs/finance-control.log`
- **Rotation**: Daily with 10MB size limit
- **Retention**: 30 days
- **Format**: `yyyy-MM-dd HH:mm:ss.SSS [thread] LEVEL logger - message`

#### Error File
- **Location**: `logs/finance-control-error.log`
- **Content**: Only ERROR level messages
- **Purpose**: Quick access to critical issues

### 7.5 Environment Profiles

#### Development (`dev`)
- Application logs: DEBUG
- Spring Web: DEBUG
- Spring Security: DEBUG
- Console output: Enabled
- File output: Enabled

#### Production (`prod`)
- Application logs: INFO
- Spring Web: WARN
- Spring Security: WARN
- Root level: WARN
- Console output: Enabled
- File output: Enabled

#### Test (`test`)
- Application logs: DEBUG
- Spring Test: DEBUG
- Console output: Enabled
- File output: Disabled

### 7.6 Base Class Logging Implementation

The application implements SLF4J logging through Lombok's `@Slf4j` annotation in the base classes to avoid code duplication and provide consistent logging across all services and controllers.

#### BaseService<T, I, D>

**Location:** `src/main/java/com/finance_control/shared/service/BaseService.java`

**Annotation:** `@Slf4j`

**Purpose:** Provides logging functionality to all service classes that extend it.

**Key Logging Points:**
- **findAll()**: Logs search parameters, filters, and result counts
- **findById()**: Logs entity lookup attempts and results
- **create()**: Logs entity creation process and success
- **update()**: Logs entity update process and success
- **delete()**: Logs entity deletion process and success
- **User-aware operations**: Logs user context validation and ownership checks

**Log Levels Used:**
- `DEBUG`: Method entry, parameter values, intermediate steps
- `INFO`: Successful operations (create, update, delete)
- `WARN`: Entity not found, access denied scenarios
- `ERROR`: User context not available, critical failures

#### BaseController<T, I, D>

**Location:** `src/main/java/com/finance_control/shared/controller/BaseController.java`

**Annotation:** `@Slf4j`

**Purpose:** Provides logging functionality to all controller classes that extend it.

**Key Logging Points:**
- **findAll()**: Logs request parameters and result counts
- **findById()**: Logs entity lookup requests and results
- **create()**: Logs entity creation requests and success
- **update()**: Logs entity update requests and success
- **delete()**: Logs entity deletion requests and success

**Log Levels Used:**
- `DEBUG`: Request details, parameter values, response information
- `INFO`: Successful operations

#### GlobalExceptionHandler

**Location:** `src/main/java/com/finance_control/shared/exception/GlobalExceptionHandler.java`

**Annotation:** `@Slf4j`

**Purpose:** Centralized exception handling with logging.

**Key Logging Points:**
- **EntityNotFoundException**: Logs as WARN
- **IllegalArgumentException**: Logs as WARN
- **MethodArgumentNotValidException**: Logs as WARN
- **Generic Exception**: Logs as ERROR with stack trace

### 7.7 Log Organization by Type

Project logs are organized into specific subdirectories within `/logs`:

```
finance-control/
├── logs/                          # Root logs directory
│   ├── application/               # Spring Boot application logs
│   │   ├── finance-control.log    # Main application log
│   │   └── finance-control-error.log # Application error log
│   │
│   ├── checkstyle/                # Code style verification logs
│   ├── quality/                   # Code quality verification logs
│   ├── gradle/                    # Gradle task logs
│   ├── docker/                    # Docker operation logs
│   └── environment/               # Environment test logs
```

### 7.8 Logging Best Practices

1. **Use appropriate log levels**:
   - `DEBUG`: Detailed information for debugging
   - `INFO`: General information about application flow
   - `WARN`: Warning conditions that don't stop execution
   - `ERROR`: Error conditions that need attention

2. **Use parameterized logging**:
   ```java
   // Good
   logger.debug("Processing user: {}", userId);

   // Avoid
   logger.debug("Processing user: " + userId);
   ```

3. **Include context in log messages**:
   ```java
   logger.info("User {} created transaction {} with amount {}",
              userId, transactionId, amount);
   ```

4. **Log exceptions properly**:
   ```java
   try {
       // risky operation
   } catch (Exception e) {
       logger.error("Failed to process request for user: {}", userId, e);
       throw e;
   }
   ```

5. **Use Base Classes**: Always extend `BaseService` and `BaseController` for new services and controllers
6. **Leverage Existing Logging**: Don't add redundant logging statements for standard CRUD operations
7. **Add Domain-Specific Logging**: Only add additional logging for business-specific operations

### 7.9 Monitoring and Maintenance

#### Log File Management
- Log files are automatically rotated daily
- Maximum file size: 10MB
- Retention period: 30 days
- Old files are automatically deleted

#### Performance Considerations
- File logging uses async appenders for better performance
- Console logging is synchronous for immediate feedback
- Queue size for async logging: 512 messages

#### Viewing Logs

```bash
# View main logs
tail -f logs/application/finance-control.log

# View error logs
tail -f logs/application/finance-control-error.log

# Search for errors
grep -i error logs/*/*.log
```

---

## Conclusion

This comprehensive development guide provides all the information needed to maintain high code quality, follow best practices, and ensure consistent development patterns across the Finance Control application. By following these guidelines, developers can create maintainable, secure, and performant code.

For more specific information, refer to:
- `.cursor/rules/base-classes-usage.mdc` for base class patterns
- `.cursor/rules/naming-conventions.mdc` for naming patterns
- [Official MapStruct documentation](https://mapstruct.org/) for advanced mapping features
- `docs/TESTING.md` for detailed testing guidelines
