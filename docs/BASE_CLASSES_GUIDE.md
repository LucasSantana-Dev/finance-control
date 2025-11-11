# Base Classes Architecture Guide

This document describes the base classes architecture for entities, DTOs, and controllers in the finance-control application.

> **Rule Reference**: For concise base class usage patterns, see `.cursor/rules/base-classes-usage.mdc`

## Overview

The base classes provide a consistent foundation for common patterns across the application, reducing code duplication and ensuring consistency.

## Base Entity Classes

### BaseEntity<T>

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

## Base DTO Classes

### BaseDTO<T>

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

### BaseCreateDTO

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

## Base API Response Classes

### ApiResponse<T>

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

### ErrorResponse

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

### ValidationError

**Location**: `src/main/java/com/finance_control/shared/dto/ErrorResponse.java` (inner class)

**Purpose**: Detailed validation error information.

**Features**:
- Field name that failed validation
- Descriptive error message
- Rejected value for debugging

## Base Controller Classes

### BaseController Interface

**Location**: `src/main/java/com/finance_control/shared/controller/BaseController.java`

**Purpose**: Defines the contract for standard CRUD operations.

**Features**:
- Standard CRUD endpoints: `findAll`, `findById`, `create`, `update`, `delete`
- Proper HTTP status codes and response types
- Generic type parameters for flexibility

### AbstractBaseController

**Location**: `src/main/java/com/finance_control/shared/controller/AbstractBaseController.java`

**Purpose**: Provides implementation of standard CRUD operations.

**Features**:
- Implements all BaseController methods
- Proper error handling and validation
- `@Valid` annotations for request body validation
- Consistent response patterns

### BaseRestController

**Location**: `src/main/java/com/finance_control/shared/controller/BaseRestController.java`

**Purpose**: Alternative base controller that doesn't implement the interface, allowing more flexibility.

**Features**:
- Same CRUD operations as AbstractBaseController
- Can be extended for controllers with custom operations
- No interface constraint

## Migration Guide

### Migrating Entities

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

### Migrating DTOs

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

### Migrating Controllers

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

## Best Practices

### 1. Entity Design
- Always extend `BaseEntity<T>` for new entities
- Use `@EqualsAndHashCode(callSuper = true)` to include parent fields in equals/hashCode
- Keep domain-specific fields only in the entity class

### 2. DTO Design
- Use `BaseDTO<T>` for response DTOs
- Use `BaseCreateDTO` for create DTOs
- Create separate `UpdateDTO` classes if update operations differ from create operations
- Use `@EqualsAndHashCode(callSuper = true)` for proper inheritance

### 3. Controller Design
- Choose between `AbstractBaseController` (interface-based) or `BaseRestController` (direct inheritance)
- Add custom endpoints as needed
- Override base methods if custom behavior is required

### 4. Service Design
- Services should extend `BaseService<T, I, C, U, R>` for standard CRUD operations
- Implement abstract methods: `mapToEntity`, `updateEntityFromDTO`, `mapToResponseDTO`
- Override validation methods if needed

## Benefits

1. **Consistency**: All entities and DTOs follow the same patterns
2. **Reduced Boilerplate**: Common fields and annotations are inherited
3. **Type Safety**: Generic types ensure proper ID handling
4. **Maintainability**: Changes to base classes automatically apply to all implementations
5. **Audit Support**: Automatic audit field management
6. **Validation**: Built-in validation support with `@Valid` annotations

## Considerations

1. **Database Schema**: Existing entities may need migration scripts if audit fields are added
2. **API Compatibility**: Changes to base classes may affect API contracts
3. **Testing**: Ensure tests account for inherited behavior
4. **Performance**: Audit field updates may have minimal performance impact

## Future Enhancements

1. **Soft Delete Support**: Add `deletedAt` field to BaseEntity
2. **Version Control**: Add `@Version` field for optimistic locking
3. **Tenant Support**: Add tenant-specific fields for multi-tenancy
4. **Audit Trail**: Enhanced audit trail with user tracking

### Migrating Controllers to Use ApiResponse

1. **Update Return Types**:
   ```java
   // Before
   @GetMapping("/{id}")
   public ResponseEntity<UserDTO> findById(@PathVariable Long id) {
       return service.findById(id)
               .map(ResponseEntity::ok)
               .orElse(ResponseEntity.notFound().build());
   }

   // After
   @GetMapping("/{id}")
   public ResponseEntity<ApiResponse<UserDTO>> findById(@PathVariable Long id) {
       return service.findById(id)
               .map(entity -> ResponseEntity.ok(ApiResponse.success(entity, "User found")))
               .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                   .body(ApiResponse.error("User not found with ID: " + id)));
   }
   ```

2. **Update List Endpoints**:
   ```java
   // Before
   @GetMapping
   public ResponseEntity<Page<UserDTO>> findAll(Pageable pageable) {
       Page<UserDTO> result = service.findAll(pageable);
       return ResponseEntity.ok(result);
   }

   // After
   @GetMapping
   public ResponseEntity<ApiResponse<Page<UserDTO>>> findAll(Pageable pageable) {
       Page<UserDTO> result = service.findAll(pageable);
       ApiResponse<Page<UserDTO>> response = ApiResponse.success(result, "Users retrieved successfully");
       return ResponseEntity.ok(response);
   }
   ```

## Best Practices

### 1. Response Wrapping
- Always wrap successful responses in `ApiResponse<T>`
- Use descriptive messages for each operation
- Include appropriate HTTP status codes

### 2. Error Handling
- Use `ErrorResponse` for all error scenarios
- Provide meaningful error messages
- Include validation details when applicable
- Use standardized error types

### 3. Controller Implementation
- Controllers extending `BaseController` automatically wrap responses
- Override base methods when custom response handling is needed
- Use `GlobalExceptionHandler` for centralized error handling

### 4. Message Guidelines
- Use present tense for success messages
- Be specific about the operation performed
- Include relevant context when helpful
- Keep messages concise but informative

## Benefits

1. **Consistency**: All API responses follow the same structure
2. **Error Handling**: Centralized error handling with detailed information
3. **Debugging**: Timestamp and path information for easier troubleshooting
4. **Frontend Integration**: Predictable response format for frontend consumption
5. **Documentation**: Clear API documentation with consistent examples
6. **Monitoring**: Structured responses enable better monitoring and logging

## Considerations

1. **Backward Compatibility**: Existing clients may need updates to handle new response structure
2. **Performance**: Minimal overhead from response wrapping
3. **Testing**: Update tests to account for new response structure
4. **Documentation**: Update API documentation to reflect new response format

## Future Enhancements

1. **Response Caching**: Add cache headers to response wrapper
2. **Request Tracing**: Include request ID in response for tracing
3. **Localization**: Support for localized error messages
4. **Rate Limiting**: Include rate limit information in response headers
