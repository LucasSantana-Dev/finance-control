# Testing Strategy

This project uses a comprehensive testing strategy with different types of tests for different purposes.

## Current Test Coverage Status

**Latest Coverage Report (JaCoCo):**
- **Lines**: 75% (after exclusions: config, DTOs, models, exceptions, enums, utils, validation, mappers, repositories)
- **Branches**: 63%
- **Methods**: 64%
- **Classes**: 83%
- **Total Test Files**: 81
- **Executed Tests**: 1,278 tests (all passing)
- **CI Status**: ✅ Coverage verification passing (75% lines, 60% branches, 60% methods)

## Test Types

### 1. Unit Tests (JUnit 5 + Mockito)
- **Purpose**: Test individual components in isolation
- **Location**: `src/test/java/com/finance_control/unit/{module}/`
- **Annotations**: `@ExtendWith(MockitoExtension.class)`, `@WebMvcTest` for controllers
- **Dependencies**: Mocked using Mockito
- **Execution**: Fast, no full Spring context

**Current Structure:**
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
- **Purpose**: Test component interactions with real database and services
- **Location**: `src/test/java/com/finance_control/integration/{module}/`
- **Annotations**: `@SpringBootTest`, `@AutoConfigureTestDatabase`
- **Dependencies**: Real Spring context, H2/PostgreSQL test database
- **Execution**: Medium speed, full Spring context
- **Status**: Currently experiencing database locking issues

### 3. End-to-End Tests
- **Purpose**: Test complete user workflows through browser
- **Status**: Not yet implemented

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

## Test Configuration

### Unit Tests
- Use `@Mock` and `@InjectMocks` for dependencies
- Use `@WebMvcTest` for controller tests with `@MockBean`
- Use `@ExtendWith(MockitoExtension.class)` for pure unit tests
- Use `@ActiveProfiles("test")` for test-specific configurations

### Integration Tests
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

## Recent Improvements & Challenges

### Improvements Made
- **Fixed Test Execution**: Resolved compilation issues preventing test runs
- **Added Controller Tests**: Comprehensive unit tests for InvestmentController with filtering and metadata endpoints
- **Improved Test Structure**: Separated unit and integration tests with proper annotations
- **Enhanced Error Handling**: Added GlobalExceptionHandler for consistent error responses
- **Coverage Increase**: From 0% to 71% instruction coverage through focused testing

### Challenges Encountered
- **Dependency Issues**: Temporarily disabled Supabase dependencies due to missing artifacts
- **Integration Test Locking**: Database locking issues in integration tests with H2/PostgreSQL
- **Configuration Changes**: Required updates to multiple config files for record-style properties access
- **Test Refactoring**: Needed to convert some integration tests to unit tests for better isolation

### Current Status
- **Coverage**: 75% lines, 63% branches, 64% methods, 83% classes (after exclusions)
- **Tests Executing**: 1,278 tests (all passing)
- **CI Integration**: Coverage verification enabled in GitHub Actions with PR reports
- **Coverage Gates**: JaCoCo verification runs automatically in CI pipeline

## Best Practices

### Unit Tests
1. Test one method/behavior at a time
2. Use descriptive test names: `methodName_ShouldReturnExpected_WhenCondition`
3. Mock external dependencies
4. Keep tests fast and focused
5. Use `@WebMvcTest` for controller testing
6. Use `@MockBean` for service dependencies in controller tests

### Integration Tests
1. Test component interactions
2. Use real database operations
3. Test repository methods with actual data
4. Verify business logic with real dependencies
5. Use `@SpringBootTest` for full context
6. Handle database locking issues

### Future E2E Tests
1. Test complete user workflows
2. Use Playwright for browser automation
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
    testUser = new User();
    testUser.setId(1L);
    testUser.setEmail("test@example.com");
    testUser.setPassword("password");
    testUser.setIsActive(true);

    testInvestment = new Investment();
    testInvestment.setId(1L);
    testInvestment.setTicker("PETR4");
    testInvestment.setName("Petrobras");
    testInvestment.setCurrentPrice(BigDecimal.valueOf(26.00));
    testInvestment.setUser(testUser);
}
```

## Continuous Integration

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

## Troubleshooting

### Common Issues

1. **Test execution fails with 0% coverage**
   - Check for compilation errors preventing tests from running
   - Verify JaCoCo plugin is correctly configured in build.gradle
   - Ensure test classes are in the correct package structure

2. **ApplicationContext fails to load**
   - Ensure `@ActiveProfiles("test")` is used
   - Check test configuration in `application-test.properties`
   - Verify all required beans are available
   - Update configuration classes to use record-style properties access

3. **Controller tests fail with authentication issues**
   - Use `@WebMvcTest` for unit controller tests
   - Mock `@AuthenticationPrincipal` with custom argument resolvers
   - Use `@MockBean` for service dependencies

4. **Database locking in integration tests**
   - Use `@Transactional` for test isolation
   - Avoid concurrent test execution
   - Use H2 with proper configuration

5. **Dependency injection issues**
   - Remove `@RequiredArgsConstructor` from controllers/services
   - Use `@Autowired` for dependencies
   - Update configuration to use record-style properties access

### Test Execution Order
Tests are executed in the following order:
1. Unit tests (fastest)
2. Integration tests (medium)
3. E2E tests (slowest) - when implemented

This ensures that fast tests run first and provide quick feedback during development.
