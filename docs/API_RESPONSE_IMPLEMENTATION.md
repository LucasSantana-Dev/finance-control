# API Response Implementation Guide

This document provides detailed implementation guidance for the standardized API response pattern used throughout the finance-control application.

## Overview

The standardized API response pattern ensures consistency across all endpoints by wrapping responses in a predictable structure and providing detailed error information when needed.

## Response Classes

### ApiResponse<T>

**Purpose**: Wrapper for all successful API responses.

**Location**: `src/main/java/com/finance_control/shared/dto/ApiResponse.java`

**Structure**:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    
    // Static factory methods
    public static <T> ApiResponse<T> success(T data) { ... }
    public static <T> ApiResponse<T> success(T data, String message) { ... }
    public static <T> ApiResponse<T> error(String message) { ... }
    public static <T> ApiResponse<T> error(String message, String path) { ... }
}
```

**Usage Examples**:
```java
// Simple success response
ApiResponse<UserDTO> response = ApiResponse.success(userDTO);

// Success with custom message
ApiResponse<UserDTO> response = ApiResponse.success(userDTO, "User created successfully");

// Error response
ApiResponse<UserDTO> error = ApiResponse.error("User not found");

// Error with path
ApiResponse<UserDTO> error = ApiResponse.error("Validation failed", "/api/users");
```

### ErrorResponse

**Purpose**: Standardized structure for all error responses.

**Location**: `src/main/java/com/finance_control/shared/dto/ErrorResponse.java`

**Structure**:
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<ValidationError> validationErrors;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
```

## Implementation in Controllers

### BaseController Integration

Controllers extending `BaseController` automatically wrap responses:

```java
@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<User, Long, UserCreateDTO, UserUpdateDTO, UserDTO> {
    
    public UserController(UserService userService) {
        super(userService);
    }
    
    // All CRUD operations automatically return ApiResponse<T>
    // GET /api/users returns ApiResponse<Page<UserDTO>>
    // GET /api/users/{id} returns ApiResponse<UserDTO>
    // POST /api/users returns ApiResponse<UserDTO>
    // PUT /api/users/{id} returns ApiResponse<UserDTO>
    // DELETE /api/users/{id} returns ApiResponse<Void>
}
```

### Custom Controller Methods

For custom endpoints, manually wrap responses:

```java
@GetMapping("/by-email/{email}")
public ResponseEntity<ApiResponse<UserDTO>> findByEmail(@PathVariable String email) {
    return userService.findByEmail(email)
            .map(user -> ResponseEntity.ok(ApiResponse.success(user, "User found by email")))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found with email: " + email)));
}

@PostMapping("/{id}/activate")
public ResponseEntity<ApiResponse<UserDTO>> activateUser(@PathVariable Long id) {
    UserDTO activatedUser = userService.activateUser(id);
    return ResponseEntity.ok(ApiResponse.success(activatedUser, "User activated successfully"));
}
```

## Global Exception Handling

### GlobalExceptionHandler

**Location**: `src/main/java/com/finance_control/shared/exception/GlobalExceptionHandler.java`

**Purpose**: Centralized exception handling that returns standardized ErrorResponse objects.

**Key Exception Handlers**:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            "NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI(),
            LocalDateTime.now(),
            null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.ValidationError(
                error.getField(),
                error.getDefaultMessage(),
                error.getRejectedValue()
            ))
            .collect(Collectors.toList());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Validation failed",
            request.getRequestURI(),
            LocalDateTime.now(),
            validationErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericError(Exception ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            request.getRequestURI(),
            LocalDateTime.now(),
            null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

## Response Examples

### Success Responses

**Single Entity**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Vacation Fund",
    "targetAmount": 5000.00,
    "currentAmount": 2500.00,
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "message": "Financial goal retrieved successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}
```

**Paginated List**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Vacation Fund",
        "targetAmount": 5000.00,
        "currentAmount": 2500.00
      },
      {
        "id": 2,
        "name": "Emergency Fund",
        "targetAmount": 10000.00,
        "currentAmount": 7500.00
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 20,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 25,
    "totalPages": 2,
    "last": false,
    "first": true,
    "numberOfElements": 20
  },
  "message": "Financial goals retrieved successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}
```

### Error Responses

**Validation Error**:
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/financial-goals",
  "timestamp": "2024-01-15T10:30:00",
  "validationErrors": [
    {
      "field": "targetAmount",
      "message": "Target amount must be greater than 0",
      "rejectedValue": -100
    },
    {
      "field": "name",
      "message": "Name is required",
      "rejectedValue": null
    }
  ]
}
```

**Not Found Error**:
```json
{
  "error": "NOT_FOUND",
  "message": "Financial goal not found with ID: 999",
  "path": "/api/financial-goals/999",
  "timestamp": "2024-01-15T10:30:00",
  "validationErrors": null
}
```

**Business Rule Violation**:
```json
{
  "error": "CONFLICT",
  "message": "A financial goal with this name already exists",
  "path": "/api/financial-goals",
  "timestamp": "2024-01-15T10:30:00",
  "validationErrors": null
}
```

## Error Types

### Standard Error Types

1. **VALIDATION_ERROR**
   - Bean validation failures
   - Field-level validation errors
   - Includes validation details

2. **NOT_FOUND**
   - Entity not found
   - Resource doesn't exist
   - Clear error message with context

3. **CONFLICT**
   - Business rule violations
   - Duplicate entries
   - Constraint violations

4. **UNAUTHORIZED**
   - Authentication required
   - Invalid credentials
   - Missing authentication

5. **FORBIDDEN**
   - Insufficient permissions
   - Access denied
   - Role-based restrictions

6. **INTERNAL_ERROR**
   - Unexpected server errors
   - System failures
   - Generic error handling

## Testing

### Unit Tests

```java
@Test
void findById_ShouldReturnWrappedResponse_WhenEntityExists() {
    // Given
    UserDTO userDTO = new UserDTO();
    userDTO.setId(1L);
    userDTO.setEmail("test@example.com");
    
    when(userService.findById(1L)).thenReturn(Optional.of(userDTO));
    
    // When
    ResponseEntity<ApiResponse<UserDTO>> response = userController.findById(1L);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData()).isEqualTo(userDTO);
    assertThat(response.getBody().getMessage()).isEqualTo("User found");
}

@Test
void findById_ShouldReturnErrorResponse_WhenEntityNotFound() {
    // Given
    when(userService.findById(1L)).thenReturn(Optional.empty());
    
    // When
    ResponseEntity<ApiResponse<UserDTO>> response = userController.findById(1L);
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody().isSuccess()).isFalse();
    assertThat(response.getBody().getMessage()).contains("User not found");
}
```

### Integration Tests

```java
@Test
void createUser_ShouldReturnWrappedResponse_WhenValidData() {
    // Given
    UserCreateDTO createDTO = new UserCreateDTO();
    createDTO.setEmail("test@example.com");
    createDTO.setPassword("password123");
    
    // When
    ResponseEntity<ApiResponse<UserDTO>> response = restTemplate.postForEntity(
        "/api/users", createDTO, new ParameterizedTypeReference<ApiResponse<UserDTO>>() {}
    );
    
    // Then
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().isSuccess()).isTrue();
    assertThat(response.getBody().getData().getEmail()).isEqualTo("test@example.com");
}
```

## Migration Guide

### Step 1: Create Response Classes

1. Create `ApiResponse.java` in `shared/dto/`
2. Create `ErrorResponse.java` in `shared/dto/`
3. Update `GlobalExceptionHandler` to use `ErrorResponse`

### Step 2: Update BaseController

1. Modify `BaseController` to wrap responses in `ApiResponse<T>`
2. Update return types for all CRUD methods
3. Add appropriate success messages

### Step 3: Update Existing Controllers

1. Update return types to use `ApiResponse<T>`
2. Wrap custom endpoint responses
3. Update tests to account for new response structure

### Step 4: Update Tests

1. Update unit tests to expect wrapped responses
2. Update integration tests for new response format
3. Add tests for error scenarios

### Step 5: Update Documentation

1. Update API documentation to reflect new response format
2. Update client examples
3. Update OpenAPI/Swagger documentation

## Benefits

1. **Consistency**: All endpoints return the same response structure
2. **Error Handling**: Centralized error handling with detailed information
3. **Debugging**: Timestamp and path information for easier troubleshooting
4. **Frontend Integration**: Predictable response format for frontend consumption
5. **Documentation**: Clear API documentation with consistent examples
6. **Monitoring**: Structured responses enable better monitoring and logging

## Best Practices

1. **Message Guidelines**
   - Use present tense for success messages
   - Be specific about the operation performed
   - Include relevant context when helpful
   - Keep messages concise but informative

2. **Error Handling**
   - Use appropriate error types
   - Provide meaningful error messages
   - Include validation details when applicable
   - Log errors appropriately

3. **Performance**
   - Response wrapping has minimal overhead
   - Consider caching for frequently accessed data
   - Monitor response times

4. **Security**
   - Don't expose sensitive information in error messages
   - Sanitize error details in production
   - Use appropriate HTTP status codes 