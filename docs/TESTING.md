# Finance Control API - Testing Documentation

This comprehensive guide covers all aspects of testing for the Finance Control API, including testing strategy, best practices, patterns, and practical examples for running tests.

## Table of Contents

1. [Testing Strategy Overview](#testing-strategy-overview)
2. [Test Types and Coverage Requirements](#test-types-and-coverage-requirements)
3. [Best Practices](#best-practices)
4. [Test Patterns and Examples](#test-patterns-and-examples)
5. [Running Tests](#running-tests)
6. [CI/CD Integration](#cicd-integration)
7. [API Testing Guide](#api-testing-guide)
8. [Troubleshooting](#troubleshooting)

---

## Testing Strategy Overview

This project uses a comprehensive testing strategy with different types of tests for different purposes. The goal is to ensure high code quality, reliability, and maintainability through a well-designed test suite.

### Current Test Coverage Status

**Latest Coverage Report (JaCoCo):**
- **Lines**: 75% (after exclusions: config, DTOs, models, exceptions, enums, utils, validation, mappers, repositories)
- **Branches**: 63%
- **Methods**: 64%
- **Classes**: 83%
- **Total Test Files**: ~100 (after cleanup)
- **Target**: 85% coverage threshold
- **CI Status**: Coverage verification passing (75% lines, 60% branches, 60% methods)

### Test Infrastructure

**TestContainers Integration:**
- Integration tests use TestContainers PostgreSQL for full database compatibility
- BaseIntegrationTest provides TestContainers setup for all integration tests
- Fixes H2 compatibility issues with JSONB and ENUM types
- See [TestContainers Setup](#testcontainers-setup) for details

### Testing Philosophy

Good tests are:
- **Fast**: Run quickly to provide immediate feedback
- **Isolated**: Don't depend on other tests or external state
- **Repeatable**: Same result every time they run
- **Self-validating**: Clear pass/fail criteria
- **Timely**: Written close to the code they test
- **Readable**: Clear intent and structure

> **Rule Reference**: For concise testing patterns and conventions, see `.cursor/rules/testing-quality.mdc`

---

## Test Types and Coverage Requirements

### 1. Unit Tests (JUnit 5 + Mockito)

**Purpose**: Test individual components in isolation

**Location**: `src/test/java/com/finance_control/unit/{module}/`

**Annotations**:
- `@ExtendWith(MockitoExtension.class)`
- `@WebMvcTest` for controllers

**Dependencies**: Mocked using Mockito

**Execution**: Fast, no full Spring context

**When to Use**:
- Testing pure business logic without external dependencies
- Testing utility functions and helpers
- Testing complex algorithms or calculations
- Fast feedback is critical during development
- Testing error handling and edge cases in isolation

**Example**:
```java
@ExtendWith(MockitoExtension.class)
class TransactionCalculatorTest {

    @InjectMocks
    private TransactionCalculator calculator;

    @Test
    void shouldCalculateTotalCorrectly() {
        List<BigDecimal> amounts = Arrays.asList(
            new BigDecimal("100.00"),
            new BigDecimal("200.50"),
            new BigDecimal("50.25")
        );

        BigDecimal total = calculator.calculateTotal(amounts);

        assertThat(total).isEqualTo(new BigDecimal("350.75"));
    }
}
```

**Current Structure**:
```
src/test/java/com/finance_control/
├── unit/
│   ├── shared/
│   │   ├── service/
│   │   │   ├── ConfigurationServiceTest.java
│   │   │   └── SupabaseRealtimeServiceTest.java.disabled
│   │   └── util/
│   │       └── RangeUtilsTest.java
│   ├── brazilian_market/
│   │   ├── controller/
│   │   │   └── InvestmentControllerTest.java
│   │   └── client/
│   │       └── BCBApiClientTest.java
│   ├── transactions/
│   │   ├── controller/
│   │   │   ├── TransactionControllerTest.java
│   │   │   ├── category/
│   │   │   └── subcategory/
│   │   └── service/
│   └── users/
└── integration/
    └── brazilian_market/
        └── controller/
            └── InvestmentControllerIntegrationTest.java
```

### 2. Integration Tests (JUnit 5 + Spring Boot Test)

**Purpose**: Test component interactions with real database and services

**Location**: `src/test/java/com/finance_control/integration/{module}/`

**Annotations**:
- `@SpringBootTest`
- `@AutoConfigureTestDatabase`

**Dependencies**: Real Spring context, TestContainers PostgreSQL database

**Execution**: Medium speed, full Spring context

**TestContainers Setup**: All integration tests use TestContainers PostgreSQL for full database compatibility (JSONB, ENUM types). See [TestContainers Setup](#testcontainers-setup) section below.

**When to Use**:
- Testing database interactions and queries
- Testing REST API endpoints end-to-end
- Testing component interactions (Service + Repository)
- Validating configuration and dependency injection
- Testing transactions and data persistence

**Example**:
```java
@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository repository;

    @Test
    void shouldCreateAndRetrieveTransaction() throws Exception {
        // Test complete flow from API to database
        TransactionDTO dto = createTransactionDTO();

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isCreated());

        // Verify in database
        Optional<Transaction> saved = repository.findByDescription(dto.getDescription());
        assertThat(saved).isPresent();
    }
}
```

### 3. TestContainers Setup

**Purpose**: Use real PostgreSQL database for integration tests to avoid H2 compatibility issues

**Location**: `src/test/java/com/finance_control/integration/BaseIntegrationTest.java`

**Setup**:
```java
@Testcontainers
public abstract class BaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("finance_control_test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
```

**Usage**:
```java
@Transactional
class TransactionServiceIntegrationTest extends BaseIntegrationTest {
    // Tests automatically use TestContainers PostgreSQL
}
```

**Benefits**:
- Full PostgreSQL compatibility (JSONB, ENUM types)
- No H2 compatibility issues
- Real database behavior for integration tests
- Container reuse for faster test execution

**Dependencies** (in `build.gradle`):
```gradle
testImplementation 'org.testcontainers:testcontainers:1.19.3'
testImplementation 'org.testcontainers:postgresql:1.19.3'
testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
```

### 4. End-to-End Tests

**Purpose**: Test complete user workflows through browser

**Status**: Not yet implemented

### Coverage Targets

- **Overall Coverage**: Minimum 80%
- **Critical Business Logic**: 95%+
- **Service Layer**: 85%+
- **Controller Layer**: 80%+
- **Repository Layer**: 75%+ (focus on custom queries)

### What to Test

**Must Test:**
- Business logic and calculations
- Validation rules
- Error handling and exceptions
- Edge cases and boundary conditions
- Complex algorithms
- Security checks
- Integration points

**Optional to Test:**
- Simple getters/setters (unless they contain logic)
- DTOs and data classes
- Configuration classes
- Auto-generated code

**Don't Test:**
- Framework code
- Third-party library code
- Generated code (unless critical)

### File Naming Conventions

- **Unit Tests**: `*Test.java` or `*UnitTest.java`
- **Integration Tests**: `*IntegrationTest.java`
- **E2E Tests**: `*E2ETest.java` or placed under `src/test/java/e2e/`

---

## Best Practices

### 1. Test Behavior, Not Implementation

Focus on what the system does and what the user experiences, rather than the internal workings of your code. This makes your tests more resilient to refactoring.

```java
// ❌ Bad - Testing implementation
@Test
void testInternalState() {
    UserService service = new UserService();
    // Accessing private/internal state
    assertThat(service.getInternalCounter()).isEqualTo(0);
}

// ✅ Good - Testing behavior
@Test
void shouldCreateUserWhenValidDataProvided() {
    // Arrange
    UserDTO userDTO = new UserDTO("john@example.com", "John Doe");

    // Act
    UserDTO created = userService.createUser(userDTO);

    // Assert
    assertThat(created).isNotNull();
    assertThat(created.getEmail()).isEqualTo("john@example.com");
}
```

### 2. Use Descriptive Test Names

Test names should clearly explain what is being tested, including the component/function, the action, and the expected outcome.

```java
// ❌ Bad
@Test
void test1() {}

@Test
void testUser() {}

// ✅ Good
@Test
void shouldReturnUserWhenValidIdProvided() {}

@Test
void shouldThrowExceptionWhenUserNotFound() {}

@Test
void shouldCreateTransactionWithValidData() {}
```

Follow the naming convention: `should[ExpectedBehavior]When[StateUnderTest]`

### 3. Follow AAA Pattern (Arrange, Act, Assert)

Organize your tests into three distinct phases:

- **Arrange**: Set up the test environment, mock data, and prepare test objects
- **Act**: Perform the action being tested (method call, API request, etc.)
- **Assert**: Verify the expected outcome using assertions

```java
@Test
void shouldUpdateTransactionWhenValidIdAndDataProvided() {
    // Arrange
    Long transactionId = 1L;
    TransactionDTO updateData = new TransactionDTO();
    updateData.setAmount(new BigDecimal("100.00"));

    when(transactionRepository.findById(transactionId))
        .thenReturn(Optional.of(existingTransaction));
    when(transactionRepository.save(any(Transaction.class)))
        .thenReturn(updatedTransaction);

    // Act
    TransactionDTO result = transactionService.updateTransaction(transactionId, updateData);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getAmount()).isEqualTo(new BigDecimal("100.00"));
    verify(transactionRepository).save(any(Transaction.class));
}
```

### 4. Mock External Dependencies

Isolate your tests by mocking external dependencies like database access, REST clients, file systems, or third-party services.

**When to Mock**:
- External Services: REST clients, third-party APIs, file systems
- Slow Dependencies: Database operations (in unit tests), network calls
- Unpredictable Dependencies: Random generators, current time, file systems
- Isolation: When testing one component in isolation

**Mockito Patterns**:
```java
// Basic mocking
@Mock
private UserRepository userRepository;

// Stubbing behavior
when(userRepository.findById(1L))
    .thenReturn(Optional.of(testUser));

// Verifying interactions
verify(userRepository, times(1)).save(any(User.class));
verify(userRepository, never()).delete(any(User.class));

// Argument captors for complex assertions
ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
verify(userRepository).save(userCaptor.capture());
User savedUser = userCaptor.getValue();
assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
```

### 5. Test Class Organization

```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    // 1. Fields
    @Mock
    private TransactionRepository repository;

    @InjectMocks
    private TransactionService service;

    // 2. Setup methods
    @BeforeEach
    void setUp() {
        // Common test setup
    }

    // 3. Test methods grouped by functionality
    @Nested
    class CreateTransaction {
        @Test
        void shouldCreateTransactionWhenValid() {}

        @Test
        void shouldThrowExceptionWhenInvalid() {}
    }

    @Nested
    class UpdateTransaction {
        @Test
        void shouldUpdateTransactionWhenExists() {}

        @Test
        void shouldThrowExceptionWhenNotFound() {}
    }
}
```

### 6. Test Error States

Ensure your components and functions handle error conditions gracefully.

```java
@Test
void shouldThrowNotFoundExceptionWhenUserNotFound() {
    // Arrange
    Long userId = 999L;
    when(userRepository.findById(userId))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> userService.findById(userId))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("User not found");
}
```

### 7. Test Data Management

**Test Fixtures Pattern**:
```java
class TransactionTestFixtures {
    static Transaction createTransaction() {
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setDescription("Test transaction");
        transaction.setDate(LocalDate.now());
        return transaction;
    }

    static Transaction createTransactionWithUser(User user) {
        Transaction transaction = createTransaction();
        transaction.setUser(user);
        return transaction;
    }

    static TransactionDTO createTransactionDTO() {
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(new BigDecimal("100.00"));
        dto.setDescription("Test transaction");
        return dto;
    }
}
```

**Using Builders for Test Data**:
```java
class TransactionTestBuilder {
    private BigDecimal amount = new BigDecimal("100.00");
    private String description = "Test transaction";
    private LocalDate date = LocalDate.now();
    private User user;

    TransactionTestBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    TransactionTestBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    TransactionTestBuilder withUser(User user) {
        this.user = user;
        return this;
    }

    Transaction build() {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setDate(date);
        transaction.setUser(user);
        return transaction;
    }
}

// Usage
Transaction transaction = new TransactionTestBuilder()
    .withAmount(new BigDecimal("500.00"))
    .withDescription("Large transaction")
    .build();
```

**Database Test Data**:
```java
@SpringBootTest
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/clean-test-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TransactionRepositoryIntegrationTest {
    // Tests will have test data loaded and cleaned automatically
}

// Or use TestEntityManager
@Autowired
private TestEntityManager entityManager;

@Test
void shouldFindTransactionsByDateRange() {
    Transaction transaction = TransactionTestFixtures.createTransaction();
    entityManager.persistAndFlush(transaction);

    // Test query...
}
```

### 8. Keep Tests Independent

```java
// ❌ Bad - Tests depend on each other
@Test
void test1() {
    // Modifies shared state
}

@Test
void test2() {
    // Depends on test1's modifications
}

// ✅ Good - Each test is independent
@Test
void test1() {
    // Sets up its own state
}

@Test
void test2() {
    // Sets up its own state
}
```

### 9. Test Asynchronous Operations

Use proper async testing patterns for Spring WebFlux or `@Async` methods.

```java
// For Spring WebFlux reactive streams
@Test
void shouldReturnMonoWhenFetchingData() {
    // Arrange
    when(reactiveRepository.findAll())
        .thenReturn(Flux.just(item1, item2));

    // Act
    StepVerifier.create(service.getAllItems())
        // Assert
        .expectNext(item1)
        .expectNext(item2)
        .verifyComplete();
}

// For @Async methods
@Test
void shouldCompleteAsyncOperation() throws Exception {
    // Arrange
    CompletableFuture<String> future = asyncService.performAsyncOperation();

    // Act & Assert
    String result = future.get(5, TimeUnit.SECONDS);
    assertThat(result).isNotNull();
}
```

### 10. Best Practices Summary

1. **Write tests first** (TDD) or alongside code (TAD)
2. **Keep tests simple** - one assertion per test concept
3. **Use descriptive names** - should[ExpectedBehavior]When[StateUnderTest]
4. **Test behavior, not implementation** - refactoring shouldn't break tests
5. **Keep tests independent** - no shared state between tests
6. **Mock external dependencies** - keep tests fast and isolated
7. **Use appropriate test types** - unit for logic, integration for interactions
8. **Maintain test data** - use fixtures and builders
9. **Test edge cases** - null values, empty lists, boundary conditions
10. **Review test coverage** - aim for quality over quantity

---

## Test Patterns and Examples

### Spring Boot Test Annotations

```java
// For controller tests
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @MockBean  // Replaces bean in Spring context
    private TransactionService service;
}

// For service integration tests
@SpringBootTest
class TransactionServiceIntegrationTest {
    @MockBean  // Mock external dependency
    private ExternalApiClient apiClient;

    @Autowired  // Real service
    private TransactionService service;

    @Autowired  // Real repository
    private TransactionRepository repository;
}
```

### MockMvc for Controller Testing

```java
@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionService transactionService;

    @Test
    void shouldReturnTransactionWhenIdExists() throws Exception {
        // Arrange
        TransactionDTO transaction = new TransactionDTO();
        transaction.setId(1L);
        when(transactionService.findById(1L))
            .thenReturn(Optional.of(transaction));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.amount").exists());
    }
}
```

### Repository Test Pattern

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindByUserIdAndDateRange() {
        // Arrange
        User user = createTestUser();
        Transaction transaction = createTestTransaction(user, LocalDate.now());
        entityManager.persistAndFlush(transaction);

        // Act
        List<Transaction> results = repository.findByUserIdAndDateBetween(
            user.getId(),
            LocalDate.now().minusDays(1),
            LocalDate.now().plusDays(1)
        );

        // Assert
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(transaction.getId());
    }
}
```

### Testing with Security Context

```java
@Test
void shouldReturnUserTransactionsWhenAuthenticated() throws Exception {
    // Arrange
    UserDetails userDetails = User.builder()
        .username("testuser")
        .password("password")
        .authorities("ROLE_USER")
        .build();

    // Act & Assert
    mockMvc.perform(get("/api/transactions")
            .with(user(userDetails)))
        .andExpect(status().isOk());
}
```

### Performance Testing Pattern

```java
@Test
void shouldHandleLargeDatasetEfficiently() {
    // Arrange
    List<Transaction> largeDataset = IntStream.range(0, 10000)
        .mapToObj(i -> createTestTransaction(user, BigDecimal.valueOf(i)))
        .collect(Collectors.toList());
    transactionRepository.saveAll(largeDataset);

    // Act
    long startTime = System.currentTimeMillis();
    Page<TransactionDTO> results = transactionService.findAll(pageable);
    long duration = System.currentTimeMillis() - startTime;

    // Assert
    assertThat(results.getContent()).hasSize(pageable.getPageSize());
    assertThat(duration).isLessThan(1000); // Should complete in under 1 second
}
```

### Testing Concurrent Operations

```java
@Test
void shouldHandleConcurrentTransactions() throws Exception {
    int threadCount = 10;
    ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
        executor.submit(() -> {
            try {
                transactionService.createTransaction(transactionDTO);
            } finally {
                latch.countDown();
            }
        });
    }

    assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();

    // Verify all transactions created
    long count = transactionRepository.count();
    assertThat(count).isEqualTo(threadCount);
}
```

### Common Anti-patterns to Avoid

**❌ Testing Multiple Things in One Test**:
```java
// ❌ Bad
@Test
void testEverything() {
    service.createUser();
    service.updateUser();
    service.deleteUser();
}
```

**❌ Over-Mocking**:
```java
// ❌ Bad - Mocking everything
@Mock private List<String> list;
when(list.size()).thenReturn(1);
// Just use a real ArrayList!

// ✅ Good - Mock only external dependencies
@Mock private UserRepository userRepository;
// Use real domain objects
```

**❌ Testing Implementation Details**:
```java
// ❌ Bad
@Test
void testPrivateMethod() {
    // Using reflection to test private methods
}

// ✅ Good
@Test
void testPublicBehavior() {
    // Test through public interface
}
```

---

## Running Tests

### Run All Tests

```bash
./gradlew test
```

### Run Only Unit Tests

```bash
./gradlew test --tests "*Test" --exclude-tests "*IntegrationTest"
```

### Run Specific Test Class

```bash
./gradlew test --tests "*InvestmentControllerTest*"
```

### Run Tests by Module

```bash
./gradlew test --tests "com.finance_control.unit.transactions.*"
```

### Generate Coverage Report

```bash
./gradlew test jacocoTestReport
```

View the coverage report at: `build/reports/jacoco/test/html/index.html`

### Check Coverage Thresholds

```bash
./gradlew jacocoTestCoverageVerification
```

### Test Configuration

**Unit Tests**:
- Use `@Mock` and `@InjectMocks` for dependencies
- Use `@WebMvcTest` for controller tests with `@MockBean`
- Use `@ExtendWith(MockitoExtension.class)` for pure unit tests
- Use `@ActiveProfiles("test")` for test-specific configurations

**Integration Tests**:
- Use `@SpringBootTest` with `@AutoConfigureTestDatabase`
- Use H2/PostgreSQL test databases
- Use `@TestPropertySource` for custom properties

### Coverage Configuration

- JaCoCo plugin for coverage measurement
- Coverage thresholds (adjusted for current coverage levels):
  - Lines: 75% minimum
  - Branches: 60% minimum
  - Methods: 60% minimum
  - Classes: 80% minimum
- Excluded from coverage calculation:
  - `**/FinanceControlApplication.class` (main application class)
  - `**/config/**` (configuration classes)
  - `**/dto/**` (data transfer objects)
  - `**/model/**` (entity classes)
  - `**/exception/**` (exception classes)
  - `**/enums/**` (enum classes)
  - `**/util/**` (utility classes)
  - `**/validation/**` (validation classes)
  - `**/mapper/**` (generated MapStruct mappers)
  - `**/repository/**` (Spring Data repository interfaces)
- Coverage reports generated in `build/reports/jacoco/test/html/`
- CI verification runs `jacocoTestCoverageVerification` task

---

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Run Unit Tests
  run: ./gradlew test --tests "*Test" --exclude-tests "*IntegrationTest"

- name: Run Integration Tests
  run: ./gradlew test --tests "*IntegrationTest"

- name: Generate Coverage Report
  run: ./gradlew jacocoTestReport

- name: Check Coverage
  run: ./gradlew jacocoTestCoverageVerification
```

### Test Execution Order

Tests are executed in the following order:
1. Unit tests (fastest)
2. Integration tests (medium)
3. E2E tests (slowest) - when implemented

This ensures that fast tests run first and provide quick feedback during development.

### Recent Improvements & Challenges

**Improvements Made**:
- Fixed Test Execution: Resolved compilation issues preventing test runs
- Added Controller Tests: Comprehensive unit tests for InvestmentController with filtering and metadata endpoints
- Improved Test Structure: Separated unit and integration tests with proper annotations
- Enhanced Error Handling: Added GlobalExceptionHandler for consistent error responses
- Coverage Increase: From 0% to 71% instruction coverage through focused testing

**Challenges Encountered**:
- Dependency Issues: Temporarily disabled Supabase dependencies due to missing artifacts
- Integration Test Locking: Database locking issues in integration tests with H2/PostgreSQL
- Configuration Changes: Required updates to multiple config files for record-style properties access
- Test Refactoring: Needed to convert some integration tests to unit tests for better isolation

**Current Status**:
- Coverage: 75% lines, 63% branches, 64% methods, 83% classes (after exclusions)
- Tests Executing: 1,278 tests (all passing)
- CI Integration: Coverage verification enabled in GitHub Actions with PR reports
- Coverage Gates: JaCoCo verification runs automatically in CI pipeline

---

## API Testing Guide

### Prerequisites

1. **Application Running**: Start the Spring Boot application:
   ```bash
   ./gradlew bootRun --no-configuration-cache --no-daemon
   ```

2. **Health Check**: Verify the application is running:
   ```bash
   curl http://localhost:8080/actuator/health
   ```

3. **Environment Variables**: Ensure `.env` file contains:
   ```env
   SUPABASE_ENABLED=true
   SUPABASE_URL=your-supabase-url
   SUPABASE_ANON_KEY=your-anon-key
   SUPABASE_JWT_SIGNER=your-jwt-signer
   SUPABASE_SERVICE_ROLE_KEY=your-service-role-key
   SUPABASE_DATABASE_ENABLED=false  # Keep false for local testing
   ```

### Testing Categories

#### 1. Health & Monitoring

**Endpoint**: `GET /actuator/health`
```bash
curl http://localhost:8080/actuator/health
```

**Expected Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    }
  }
}
```

#### 2. Authentication (Local)

**Register User**:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

**Login**:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

#### 3. Supabase Authentication

**Supabase Signup**:
```bash
curl -X POST http://localhost:8080/auth/supabase/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supabase-test@example.com",
    "password": "password123"
  }'
```

**Supabase Login**:
```bash
curl -X POST http://localhost:8080/auth/supabase/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "supabase-test@example.com",
    "password": "password123"
  }'
```

#### 4. Transaction Management

**Get All Transactions**:
```bash
curl -X GET "http://localhost:8080/transactions?page=0&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Create Transaction**:
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Grocery shopping",
    "amount": 150.50,
    "type": "EXPENSE",
    "categoryId": 1,
    "date": "2024-01-15",
    "responsibles": [
      {
        "responsibleId": 1,
        "percentage": 100
      }
    ]
  }'
```

**Update Transaction**:
```bash
curl -X PUT http://localhost:8080/transactions/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Updated grocery shopping",
    "amount": 175.00,
    "type": "EXPENSE",
    "categoryId": 1,
    "date": "2024-01-15",
    "responsibles": [
      {
        "responsibleId": 1,
        "percentage": 100
      }
    ]
  }'
```

**Delete Transaction**:
```bash
curl -X DELETE http://localhost:8080/transactions/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

#### 5. Financial Goals

**Get All Goals**:
```bash
curl -X GET http://localhost:8080/goals \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Create Goal**:
```bash
curl -X POST http://localhost:8080/goals \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emergency Fund",
    "description": "Save for emergencies",
    "targetAmount": 10000.00,
    "currentAmount": 2500.00,
    "targetDate": "2024-12-31",
    "category": "SAVINGS"
  }'
```

#### 6. Dashboard & Analytics

**Get Dashboard Data**:
```bash
curl -X GET http://localhost:8080/dashboard \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Get Transaction Summary**:
```bash
curl -X GET "http://localhost:8080/dashboard/transactions/summary?period=MONTH" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### API Documentation

Access the complete API documentation at:
```
http://localhost:8080/swagger-ui.html
```

### Testing Checklist

- [ ] Health check passes
- [ ] Local authentication works (register/login)
- [ ] Supabase authentication works
- [ ] Profile CRUD operations work
- [ ] Transaction management works
- [ ] Category management works
- [ ] Financial goals work
- [ ] Dashboard data loads
- [ ] Storage operations work
- [ ] Real-time subscriptions work
- [ ] Market data loads
- [ ] All endpoints return expected status codes
- [ ] Error handling works correctly

---

## Troubleshooting

### Common Test Issues

#### Issue: Tests Failing Intermittently

**Causes**:
- Shared state between tests
- Non-deterministic behavior (random, time-dependent)
- Race conditions in async code

**Solutions**:
```java
// Ensure test isolation
@BeforeEach
void setUp() {
    // Clean state before each test
    repository.deleteAll();
}

// Use fixed values instead of random/time
@Mock
private Clock clock;

@Test
void shouldProcessTransaction() {
    LocalDateTime fixedTime = LocalDateTime.of(2024, 1, 1, 12, 0);
    when(clock.now()).thenReturn(fixedTime);
    // ...
}
```

#### Issue: Slow Integration Tests

**Solutions**:
```java
// Use @DataJpaTest for repository-only tests
@DataJpaTest
class TransactionRepositoryTest {
    // Faster than @SpringBootTest
}

// Mock external slow dependencies
@MockBean
private ExternalApiClient apiClient;  // Don't make real HTTP calls

// Use test slices appropriately
@WebMvcTest  // Only loads web layer
@JsonTest    // Only loads JSON components
```

#### Issue: Tests Pass Locally but Fail in CI

**Common Causes**:
- Different database state
- Time zone differences
- File system path differences
- Missing test data

**Solutions**:
```java
// Use profiles
@ActiveProfiles("test")

// Ensure database is reset
@DirtiesContext
@Test
void shouldTestSomething() {
    // Context reloaded after test
}

// Use absolute paths or test resources
String testFile = getClass().getResource("/test-data.json").getFile();
```

#### Issue: Mock Not Working

**Solutions**:
```java
// Ensure @ExtendWith is present for JUnit 5
@ExtendWith(MockitoExtension.class)
class ServiceTest {
    @Mock
    private Repository repository;
}

// For Spring tests, use @MockBean instead of @Mock
@SpringBootTest
class ServiceIntegrationTest {
    @MockBean  // Correct for Spring context
    private ExternalService externalService;
}
```

### Common Application Issues

1. **403 Forbidden**: Check your authorization token
2. **401 Unauthorized**: Token expired, login again
3. **404 Not Found**: Verify endpoint URL
4. **400 Bad Request**: Check request body format
5. **500 Internal Server Error**: Check application logs

### Database Issues

If using Supabase database:
1. Ensure `SUPABASE_DATABASE_ENABLED=true` in `.env`
2. Verify database credentials are correct
3. Check Supabase project status

### Specific Test Issues

**Test execution fails with 0% coverage**:
- Check for compilation errors preventing tests from running
- Verify JaCoCo plugin is correctly configured in build.gradle
- Ensure test classes are in the correct package structure

**ApplicationContext fails to load**:
- Ensure `@ActiveProfiles("test")` is used
- Check test configuration in `application-test.properties`
- Verify all required beans are available
- Update configuration classes to use record-style properties access

**Controller tests fail with authentication issues**:
- Use `@WebMvcTest` for unit controller tests
- Mock `@AuthenticationPrincipal` with custom argument resolvers
- Use `@MockBean` for service dependencies

**Database locking in integration tests**:
- Use `@Transactional` for test isolation
- Avoid concurrent test execution
- Use H2 with proper configuration

**Dependency injection issues**:
- Remove `@RequiredArgsConstructor` from controllers/services
- Use `@Autowired` for dependencies
- Update configuration to use record-style properties access

---

## Next Steps

1. **Frontend Integration**: Use these endpoints in your frontend application
2. **Load Testing**: Test with multiple concurrent users
3. **Security Testing**: Verify authentication and authorization
4. **Performance Testing**: Check response times and database performance
5. **Integration Testing**: Test end-to-end workflows
6. **E2E Test Implementation**: Implement browser-based end-to-end tests

Remember: Good tests are an investment in code quality and maintainability. They provide confidence during refactoring and help catch bugs early in the development cycle.

---

## Postman Testing Guide

This section provides comprehensive instructions for testing the Finance Control API using the Postman collection.

### Collection File Location

The comprehensive Postman collection is located at:
```
postman/FinanceControl-Comprehensive.postman_collection.json
```

### Prerequisites

1. **Postman Desktop Application** installed
2. **Application Running** on `http://localhost:8080`
3. **Database Access** - Supabase is configured as the database

### Starting the Application

#### Option 1: Using Gradle (Recommended for Testing)

```bash
cd /Users/lucassantana/Desenvolvimento/finance-control
./gradlew bootRun --args='--spring.profiles.active=dev'
```

#### Option 2: Using Docker Compose

```bash
cd /Users/lucassantana/Desenvolvimento/finance-control
docker compose up app -d
```

#### Verify Application is Running

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Importing the Postman Collection

1. Open Postman
2. Click **Import** button (top left)
3. Select **File** tab
4. Navigate to: `postman/FinanceControl-Comprehensive.postman_collection.json`
5. Click **Import**

### Collection Structure

The collection is organized into the following folders:

1. **Authentication** - Register, Login, Validate Token, Change Password
2. **Users** - User CRUD operations and management
3. **Dashboard** - Financial metrics, summaries, predictions
4. **Transactions** - Transaction management and bank statement import
5. **Financial Goals** - Goal tracking and progress updates
6. **Brazilian Market** - Economic indicators (Selic, CDI, IPCA)
7. **Monitoring** - Health checks, alerts, metrics
8. **Data Export** - Export user data in CSV/JSON formats

### Collection Variables

The collection includes pre-configured variables:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `baseUrl` | `http://localhost:8080` | API base URL |
| `authToken` | (auto-populated) | JWT authentication token |
| `userId` | (auto-populated) | Authenticated user ID |
| `testEmail` | `test@example.com` | Test user email |
| `testPassword` | `TestPassword123!` | Test user password |
| `transactionId` | (auto-populated) | Created transaction ID |
| `goalId` | (auto-populated) | Created financial goal ID |
| `timestamp` | (auto-populated) | Current timestamp |

### Sequential Testing Flow

#### 1. Authentication Flow

Execute requests in this order:

**1.1 Register User**
- **Request**: `POST /auth/register`
- **Body**:
```json
{
  "email": "{{testEmail}}",
  "password": "{{testPassword}}",
  "isActive": true
}
```
- **Expected**: HTTP 200, user ID auto-saved to `userId` variable

**1.2 Login**
- **Request**: `POST /auth/login`
- **Body**:
```json
{
  "email": "{{testEmail}}",
  "password": "{{testPassword}}"
}
```
- **Expected**: HTTP 200, token and userId auto-saved
- **Post-test Script**: Automatically extracts token and saves to collection variables

**1.3 Validate Token**
- **Request**: `POST /auth/validate`
- **Headers**: `Authorization: Bearer {{authToken}}`
- **Expected**: HTTP 200, validates the JWT token

#### 2. Transaction Management

**Create Transaction**
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EXPENSE",
    "subtype": "VARIABLE",
    "source": "CREDIT_CARD",
    "description": "Test transaction",
    "amount": 100.00,
    "date": "2024-11-15T10:00:00",
    "categoryId": 1,
    "userId": {{userId}},
    "responsibilities": [
      {
        "responsibleId": {{userId}},
        "percentage": 100
      }
    ]
  }'
```

#### 3. Financial Goals

**Create Financial Goal**
```bash
curl -X POST http://localhost:8080/financial-goals \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Emergency Fund",
    "description": "Build emergency reserve fund",
    "goalType": "SAVINGS",
    "targetAmount": 10000.00,
    "currentAmount": 0,
    "isActive": true,
    "deadline": "2025-12-31T23:59:59"
  }'
```

### Running Collection with Newman (CLI)

You can also run the entire collection from the command line using Newman:

#### Install Newman

```bash
npm install -g newman
```

#### Run Collection

```bash
newman run postman/FinanceControl-Comprehensive.postman_collection.json \
  --environment postman/dev-environment.json \
  --reporters cli,html \
  --reporter-html-export newman-report.html
```

### Automated Test Scripts

The collection includes automated test scripts that:

1. **Auto-extract tokens** from login responses
2. **Auto-save IDs** for created resources (transactions, goals)
3. **Validate response status codes**
4. **Validate response structure**

### Common Issues and Solutions

#### Issue 1: Authentication Token Expired

**Solution**: Re-run the Login request to get a new token.

#### Issue 2: User Already Exists

**Solution**: Either:
- Delete the existing user first
- Change `testEmail` variable to a different email
- Use Login instead of Register

#### Issue 3: Category/Subcategory Not Found

**Solution**: Ensure you have seed data populated in the database, or create categories first.

#### Issue 4: Connection Refused

**Solution**: Verify the application is running:
```bash
curl http://localhost:8080/actuator/health
```

If not running, start the application using one of the methods in "Starting the Application" section.

### Expected Test Results

When running the entire collection sequentially, you should see:

- Authentication: 4 requests, all passing
- Users: 7 requests, all passing
- Dashboard: 7 requests, all passing
- Transactions: 7 requests, all passing
- Financial Goals: 9 requests, all passing
- Brazilian Market: 5 requests, all passing
- Monitoring: 5 requests, all passing
- Data Export: 4 requests, all passing

**Total**: 48+ requests

### API Documentation

For complete API documentation, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/v3/api-docs
