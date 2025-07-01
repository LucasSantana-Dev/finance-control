# Shared Classes and Super Classes

This document describes the shared classes and super classes that provide common functionality across different modules in the finance control application.

## Base Service Layer

### BaseService Interface
- **Location**: `src/main/java/com/finance_control/shared/service/BaseService.java`
- **Purpose**: Defines common CRUD operations interface
- **Generic Types**: `<T, ID, CreateDTO, UpdateDTO, ResponseDTO>`
- **Methods**:
  - `findAll(Pageable pageable)`
  - `findById(ID id)`
  - `create(CreateDTO createDTO)`
  - `update(ID id, UpdateDTO updateDTO)`
  - `delete(ID id)`

### AbstractBaseService Class
- **Location**: `src/main/java/com/finance_control/shared/service/AbstractBaseService.java`
- **Purpose**: Provides default implementation of common CRUD operations
- **Extends**: `BaseService`
- **Abstract Methods**:
  - `mapToEntity(CreateDTO createDTO)`
  - `updateEntityFromDTO(T entity, UpdateDTO updateDTO)`
  - `mapToResponseDTO(T entity)`

## Base Controller Layer

### BaseController Interface
- **Location**: `src/main/java/com/finance_control/shared/controller/BaseController.java`
- **Purpose**: Defines common REST endpoints interface
- **Generic Types**: `<T, ID, CreateDTO, UpdateDTO, ResponseDTO>`
- **Endpoints**:
  - `GET /` - Find all with pagination
  - `GET /{id}` - Find by ID
  - `POST /` - Create new entity
  - `PUT /{id}` - Update entity
  - `DELETE /{id}` - Delete entity

### AbstractBaseController Class
- **Location**: `src/main/java/com/finance_control/shared/controller/AbstractBaseController.java`
- **Purpose**: Provides default implementation of common REST endpoints
- **Extends**: `BaseController`
- **Features**:
  - Automatic HTTP status codes
  - Error handling for not found entities
  - Standard REST response patterns

## Base Repository Layer

### BaseRepository Interface
- **Location**: `src/main/java/com/finance_control/shared/repository/BaseRepository.java`
- **Purpose**: Extends JpaRepository with common query methods
- **Features**:
  - Date range queries for `createdAt` and `updatedAt`
  - Count methods for date ranges
  - Common audit field operations

## Utility Classes

### ValidationUtils
- **Location**: `src/main/java/com/finance_control/shared/util/ValidationUtils.java`
- **Purpose**: Common validation operations
- **Methods**:
  - `isValidPercentage(BigDecimal percentage)`
  - `isValidDateRange(LocalDateTime startDate, LocalDateTime endDate)`
  - `isValidAmount(BigDecimal amount)`
  - `isValidId(Long id)`
  - `isValidString(String value)`
  - `isValidCollection(Collection<?> collection)`
  - Corresponding `validate*` methods that throw exceptions

## Exception Classes

### EntityNotFoundException
- **Location**: `src/main/java/com/finance_control/shared/exception/EntityNotFoundException.java`
- **Purpose**: Custom exception for entity not found scenarios
- **Constructors**:
  - `EntityNotFoundException(String message)`
  - `EntityNotFoundException(String entityName, Long id)`
  - `EntityNotFoundException(String entityName, String field, Object value)`

## Usage Examples

### Creating a Service Implementation

```java
@Service
@Transactional
public class DefaultMyService extends AbstractBaseService<MyEntity, Long, MyCreateDTO, MyUpdateDTO, MyResponseDTO> 
        implements MyService {
    
    public DefaultMyService(MyRepository repository) {
        super(repository);
    }
    
    @Override
    protected MyEntity mapToEntity(MyCreateDTO createDTO) {
        // Implementation
    }
    
    @Override
    protected void updateEntityFromDTO(MyEntity entity, MyUpdateDTO updateDTO) {
        // Implementation
    }
    
    @Override
    protected MyResponseDTO mapToResponseDTO(MyEntity entity) {
        // Implementation
    }
}
```

### Creating a Controller Implementation

```java
@RestController
@RequestMapping("/api/my-entities")
public class MyController extends AbstractBaseController<MyEntity, Long, MyCreateDTO, MyUpdateDTO, MyResponseDTO> {
    
    public MyController(MyService service) {
        super(service);
    }
    
    // Additional custom endpoints can be added here
}
```

### Using ValidationUtils

```java
public void validateMyEntity(MyEntity entity) {
    ValidationUtils.validateAmount(entity.getAmount());
    ValidationUtils.validateString(entity.getName(), "Name");
    ValidationUtils.validateId(entity.getId());
}
```

## Benefits

1. **Code Reuse**: Common CRUD operations are implemented once and reused
2. **Consistency**: All modules follow the same patterns for service and controller layers
3. **Maintainability**: Changes to common functionality only need to be made in one place
4. **Type Safety**: Generic types ensure compile-time type checking
5. **Standardization**: Consistent REST API patterns across all modules
6. **Validation**: Centralized validation utilities prevent code duplication

## Naming Conventions

### Service Layer
- **Interface**: `{Domain}Service.java` (e.g., `TransactionService.java`)
- **Implementation**: `Default{Domain}Service.java` (e.g., `DefaultTransactionService.java`)
- **Alternative implementations**: `{Feature}{Domain}Service.java` (e.g., `CachedTransactionService.java`)

### Controller Layer
- **Interface**: `{Domain}Controller.java` (e.g., `TransactionController.java`)
- **Implementation**: `Default{Domain}Controller.java` (e.g., `DefaultTransactionController.java`)

### Repository Layer
- **Interface**: `{Domain}Repository.java` (e.g., `TransactionRepository.java`)
- **Custom implementations**: `{Domain}RepositoryImpl.java` (e.g., `TransactionRepositoryImpl.java`)

### DTOs
- **Create DTO**: `{Domain}CreateDTO.java` (e.g., `TransactionCreateDTO.java`)
- **Update DTO**: `{Domain}UpdateDTO.java` (e.g., `TransactionUpdateDTO.java`)
- **Response DTO**: `{Domain}DTO.java` (e.g., `TransactionDTO.java`)

## Best Practices

1. Always extend the appropriate base class when creating new services or controllers
2. Use ValidationUtils for common validation operations
3. Throw EntityNotFoundException instead of generic RuntimeException for not found scenarios
4. Implement the abstract methods in service implementations
5. Use the base repository interface for common audit field operations
6. Follow the naming conventions for consistency across modules 