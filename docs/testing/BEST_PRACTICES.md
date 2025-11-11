# Testing Best Practices

This guide outlines best practices for writing, organizing, and maintaining tests in Spring Boot applications. Following these practices will help you build a robust, maintainable, and reliable test suite.

> **Rule Reference**: For concise testing patterns and conventions, see `.cursor/rules/testing-quality.mdc`

## When to Use Unit vs Integration Tests

### Unit Tests

Use unit tests when:
- Testing pure business logic without external dependencies
- Testing utility functions and helpers
- Testing complex algorithms or calculations
- Fast feedback is critical during development
- Testing error handling and edge cases in isolation

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

### Integration Tests

Use integration tests when:
- Testing database interactions and queries
- Testing REST API endpoints end-to-end
- Testing component interactions (Service + Repository)
- Validating configuration and dependency injection
- Testing transactions and data persistence

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

## Test Structure and Organization

### File Naming Conventions

- **Unit Tests**: `*Test.java` or `*UnitTest.java`
- **Integration Tests**: `*IntegrationTest.java`
- **E2E Tests**: `*E2ETest.java` or placed under `src/test/java/e2e/`

### Directory Structure

```
src/test/java/com/finance_control/
├── transactions/
│   ├── service/
│   │   ├── TransactionServiceTest.java        # Unit test
│   │   └── TransactionServiceIntegrationTest.java  # Integration test
│   ├── controller/
│   │   └── TransactionControllerTest.java
│   └── repository/
│       └── TransactionRepositoryIntegrationTest.java
├── shared/
│   └── test/
│       ├── BaseUnitTest.java
│       ├── BaseIntegrationTest.java
│       └── TestFixtures.java
```

### Test Class Organization

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

## Mocking Strategies for Spring Boot

### When to Mock

- **External Services**: REST clients, third-party APIs, file systems
- **Slow Dependencies**: Database operations (in unit tests), network calls
- **Unpredictable Dependencies**: Random generators, current time, file systems
- **Isolation**: When testing one component in isolation

### Mockito Patterns

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

## Test Data Management

### Test Fixtures Pattern

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

### Using Builders for Test Data

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

### Database Test Data

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

## Test Coverage Strategy

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

### Using JaCoCo for Coverage

```bash
# Generate coverage report
./gradlew test jacocoTestReport

# View report
open build/reports/jacoco/test/html/index.html

# Enforce coverage thresholds
./gradlew jacocoTestCoverageVerification
```

## Troubleshooting Common Test Issues

### Issue: Tests Failing Intermittently

**Causes:**
- Shared state between tests
- Non-deterministic behavior (random, time-dependent)
- Race conditions in async code

**Solutions:**
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

### Issue: Slow Integration Tests

**Solutions:**
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

### Issue: Tests Pass Locally but Fail in CI

**Common Causes:**
- Different database state
- Time zone differences
- File system path differences
- Missing test data

**Solutions:**
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

### Issue: Mock Not Working

**Solutions:**
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

## Performance Testing

### Testing Query Performance

```java
@Test
void shouldQueryTransactionsEfficiently() {
    // Create test data
    createLargeDataset(10000);

    // Measure performance
    long startTime = System.currentTimeMillis();
    Page<Transaction> results = repository.findAll(pageable);
    long duration = System.currentTimeMillis() - startTime;

    assertThat(duration).isLessThan(100); // Should be fast
    assertThat(results.getContent()).isNotEmpty();
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

## Best Practices Summary

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

Remember: Good tests are an investment in code quality and maintainability. They provide confidence during refactoring and help catch bugs early in the development cycle.
