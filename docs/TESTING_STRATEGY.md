# Testing Strategy

This project uses a comprehensive testing strategy with different types of tests for different purposes.

## Test Types

### 1. Unit Tests (JUnit + Mockito)
- **Purpose**: Test individual components in isolation
- **Location**: `src/test/java/com/finance_control/{module}/`
- **Base Class**: `BaseUnitTest`
- **Dependencies**: Mocked using Mockito
- **Execution**: Fast, no Spring context

**Example Structure:**
```
src/test/java/com/finance_control/
├── transactions/
│   ├── service/
│   │   └── TransactionServiceUnitTest.java
│   └── controller/
│       └── TransactionControllerUnitTest.java
├── users/
│   └── service/
│       └── UserServiceUnitTest.java
└── BaseUnitTest.java
```

### 2. Integration Tests (JUnit + Spring Boot Test)
- **Purpose**: Test component interactions with real database
- **Location**: `src/test/java/com/finance_control/{module}/`
- **Base Class**: `BaseIntegrationTest`
- **Dependencies**: Real Spring context, H2 in-memory database
- **Execution**: Medium speed, full Spring context

**Example Structure:**
```
src/test/java/com/finance_control/
├── transactions/
│   ├── repository/
│   │   └── TransactionRepositoryIntegrationTest.java
│   └── service/
│       └── TransactionServiceIntegrationTest.java
├── users/
│   └── repository/
│       └── UserRepositoryIntegrationTest.java
└── BaseIntegrationTest.java
```

### 3. End-to-End Tests (Selenium)
- **Purpose**: Test complete user workflows through browser
- **Location**: `src/test/java/com/finance_control/selenium/`
- **Base Class**: `BaseSeleniumTest`
- **Dependencies**: Real application, Chrome browser
- **Execution**: Slow, full application stack

**Example Structure:**
```
src/test/java/com/finance_control/
└── selenium/
    ├── TransactionSeleniumTest.java
    ├── UserSeleniumTest.java
    └── BaseSeleniumTest.java
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Only Unit Tests
```bash
mvn test -Dtest="*UnitTest"
```

### Run Only Integration Tests
```bash
mvn test -Dtest="*IntegrationTest"
```

### Run Only Selenium Tests
```bash
mvn test -Dtest="*SeleniumTest"
```

### Run Tests by Module
```bash
mvn test -Dtest="com.finance_control.transactions.*"
```

## Test Configuration

### Unit Tests
- Use `@Mock` and `@InjectMocks` for dependencies
- Extend `BaseUnitTest`
- No Spring context loading
- Fast execution

### Integration Tests
- Use `@Autowired` for real dependencies
- Extend `BaseIntegrationTest`
- Use `@Transactional` for test isolation
- H2 in-memory database

### Selenium Tests
- Use `WebDriver` for browser automation
- Extend `BaseSeleniumTest`
- Real application with random port
- Headless Chrome for CI/CD

## Best Practices

### Unit Tests
1. Test one method/behavior at a time
2. Use descriptive test names: `methodName_ShouldReturnExpected_WhenCondition`
3. Mock external dependencies
4. Keep tests fast and focused

### Integration Tests
1. Test component interactions
2. Use real database operations
3. Test repository methods with actual data
4. Verify business logic with real dependencies

### Selenium Tests
1. Test complete user workflows
2. Use explicit waits for UI elements
3. Test critical user paths
4. Keep tests independent and isolated

## Test Data

### Test Data Setup
- Use `@BeforeEach` for test data setup
- Create test data in each test method
- Use builders or factory methods for complex objects
- Clean up data using `@Transactional` rollback

### Test Data Examples
```java
@BeforeEach
void setUp() {
    testUser = User.builder()
        .email("test@example.com")
        .fullName("Test User")
        .password("password123")
        .build();
    
    testTransaction = Transaction.builder()
        .description("Test transaction")
        .amount(new BigDecimal("100.00"))
        .type(TransactionType.EXPENSE)
        .user(testUser)
        .build();
}
```

## Continuous Integration

### GitHub Actions Example
```yaml
- name: Run Unit Tests
  run: mvn test -Dtest="*UnitTest"

- name: Run Integration Tests
  run: mvn test -Dtest="*IntegrationTest"

- name: Run Selenium Tests
  run: mvn test -Dtest="*SeleniumTest"
```

## Troubleshooting

### Common Issues

1. **ApplicationContext fails to load**
   - Ensure `@ActiveProfiles("test")` is used
   - Check test configuration in `application-test.properties`
   - Verify all required beans are available

2. **Database connection issues**
   - Use H2 for tests: `jdbc:h2:mem:testdb`
   - Disable Flyway for tests: `spring.flyway.enabled=false`
   - Use `@Transactional` for test isolation

3. **Selenium WebDriver issues**
   - Use headless mode for CI/CD
   - Add Chrome options for stability
   - Use explicit waits for UI elements

### Test Execution Order
Tests are executed in the following order:
1. Unit tests (fastest)
2. Integration tests (medium)
3. Selenium tests (slowest)

This ensures that fast tests run first and provide quick feedback during development. 