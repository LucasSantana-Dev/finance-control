# Test Patterns and Best Practices

Writing effective and maintainable tests requires adherence to certain patterns and best practices. This guide outlines common testing patterns, strategies for different scenarios, and general guidelines to improve your test suite's quality and efficiency, specifically for Java and Spring Boot applications.

> **Rule Reference**: For concise testing patterns and quality gates, see `.cursor/rules/testing-quality.mdc`

## General Best Practices

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

```java
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldCreateTransactionWhenCategoryExists() {
        // Mock external dependency
        when(categoryService.findById(anyLong()))
            .thenReturn(Optional.of(new CategoryDTO()));

        // Test the service logic
        // ...
    }
}
```

### 5. Test Asynchronous Operations

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

### 7. Integration Test Patterns

For integration tests, use Spring Boot test annotations and test containers or embedded databases.

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldCreateTransactionViaApi() throws Exception {
        // Arrange
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(new BigDecimal("100.00"));
        transactionDTO.setDescription("Test transaction");

        // Act
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transactionDTO)))
            // Assert
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount").value("100.00"));

        // Verify in database
        Optional<Transaction> saved = transactionRepository.findByDescription("Test transaction");
        assertThat(saved).isPresent();
    }
}
```

### 8. Repository Test Patterns

Use `@DataJpaTest` for repository layer tests with embedded database.

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

## Spring Boot Specific Patterns

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

### Testing Service Layer with Spring Context

```java
@SpringBootTest
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private ExternalApiClient externalApiClient;

    @Test
    void shouldProcessTransactionWithExternalApiCall() {
        // Arrange
        when(externalApiClient.fetchData(anyString()))
            .thenReturn(CompletableFuture.completedFuture("data"));

        // Act
        TransactionDTO result = transactionService.processTransaction(transactionDTO);

        // Assert
        assertThat(result).isNotNull();
        verify(externalApiClient).fetchData(anyString());
    }
}
```

## Test Data Management

### Using Test Fixtures

```java
class TestFixtures {
    static User createTestUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setName("Test User");
        return user;
    }

    static Transaction createTestTransaction(User user, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(amount);
        transaction.setDescription("Test transaction");
        return transaction;
    }
}
```

### Using @Sql for Database Setup

```java
@SpringBootTest
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/clean-test-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TransactionRepositoryIntegrationTest {
    // Tests will have test data loaded before each test
}
```

## Performance Testing Patterns

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

## Test Coverage Best Practices

- **Aim for high coverage** but focus on covering business logic and edge cases
- **Don't test getters/setters** unless they contain business logic
- **Test error paths** - exceptions, validation failures, null checks
- **Test boundary conditions** - empty lists, null values, max limits
- **Test integration points** - database, external APIs, file systems

## Common Anti-patterns to Avoid

### ❌ Testing Multiple Things in One Test

```java
// ❌ Bad
@Test
void testEverything() {
    service.createUser();
    service.updateUser();
    service.deleteUser();
}
```

### ❌ Over-Mocking

```java
// ❌ Bad - Mocking everything
@Mock private List<String> list;
when(list.size()).thenReturn(1);
// Just use a real ArrayList!

// ✅ Good - Mock only external dependencies
@Mock private UserRepository userRepository;
// Use real domain objects
```

### ❌ Testing Implementation Details

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

### ❌ Ignoring Test Isolation

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

## Summary

Good tests are:
- **Fast**: Run quickly to provide immediate feedback
- **Isolated**: Don't depend on other tests or external state
- **Repeatable**: Same result every time they run
- **Self-validating**: Clear pass/fail criteria
- **Timely**: Written close to the code they test
- **Readable**: Clear intent and structure

Remember: A well-designed test suite provides confidence, speeds up development, and reduces bugs. Focus on testing user-facing behavior rather than implementation details.
