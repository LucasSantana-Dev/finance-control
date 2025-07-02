# Logging Implementation in Base Classes

## Overview

The application implements SLF4J logging through Lombok's `@Slf4j` annotation in the base classes to avoid code duplication and provide consistent logging across all services and controllers.

## Base Classes with Logging

### 1. BaseService<T, I, D>

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

### 2. BaseController<T, I, D>

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

### 3. GlobalExceptionHandler

**Location:** `src/main/java/com/finance_control/shared/exception/GlobalExceptionHandler.java`

**Annotation:** `@Slf4j`

**Purpose:** Centralized exception handling with logging.

**Key Logging Points:**
- **EntityNotFoundException**: Logs as WARN
- **IllegalArgumentException**: Logs as WARN
- **MethodArgumentNotValidException**: Logs as WARN
- **Generic Exception**: Logs as ERROR with stack trace

## Benefits of Base Class Logging

### 1. Code Reuse
- All services and controllers automatically get logging without additional code
- Consistent logging patterns across the application
- No need to add `@Slf4j` to individual service/controller classes

### 2. Consistent Logging
- Standardized log messages and levels
- Uniform approach to logging user-aware operations
- Consistent error handling and logging

### 3. Maintainability
- Centralized logging configuration
- Easy to modify logging behavior for all services/controllers
- Reduced code duplication

## Usage Examples

### Service Classes
```java
@Service
public class TransactionService extends BaseService<Transaction, Long, TransactionDTO> {
    
    public TransactionService(TransactionRepository repository) {
        super(repository);
    }
    
    // All CRUD operations automatically have logging
    // No need to add @Slf4j or logging statements
}
```

### Controller Classes
```java
@RestController
@RequestMapping("/api/transactions")
public class TransactionController extends BaseController<Transaction, Long, TransactionDTO> {
    
    public TransactionController(TransactionService service) {
        super(service);
    }
    
    // All REST endpoints automatically have logging
    // No need to add @Slf4j or logging statements
}
```

## Log Output Examples

### Service Logs
```
DEBUG - Finding all entities with search: 'transfer', filters: {}, sortBy: 'createdAt', sortDirection: 'desc', page: 0
DEBUG - Using repository findAll with search
DEBUG - Found 15 entities
INFO - Entity created successfully with ID: 123
WARN - Entity not found for update with ID: 999
ERROR - User context not available for user-aware service
```

### Controller Logs
```
DEBUG - GET request to list entities - search: 'transfer', sortBy: 'createdAt', sortDirection: 'desc', page: 0
DEBUG - Returning 15 entities out of 25 total
DEBUG - POST request to create entity: TransactionDTO{...}
INFO - Entity created successfully
DEBUG - DELETE request to delete entity with ID: 123
INFO - Entity deleted successfully with ID: 123
```

## Configuration

The logging configuration is defined in:
- `src/main/resources/logback-spring.xml`
- `src/main/resources/application.properties` (logging levels)

## Best Practices

1. **Use Base Classes**: Always extend `BaseService` and `BaseController` for new services and controllers
2. **Leverage Existing Logging**: Don't add redundant logging statements for standard CRUD operations
3. **Add Domain-Specific Logging**: Only add additional logging for business-specific operations
4. **Use Appropriate Log Levels**: Follow the established pattern (DEBUG for details, INFO for success, WARN for issues, ERROR for failures)
5. **Include Context**: Log relevant IDs, user information, and operation details

## Extending Logging

If you need additional logging in a specific service or controller:

```java
@Service
public class CustomService extends BaseService<Entity, Long, EntityDTO> {
    
    public void customBusinessMethod() {
        log.debug("Starting custom business method");
        // Business logic
        log.info("Custom business method completed successfully");
    }
}
```

The base class logging will handle all standard CRUD operations, while you can add domain-specific logging as needed. 