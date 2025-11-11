# API Patterns and Conventions

This document outlines the REST API patterns and conventions used throughout the finance-control application.

> **Rule Reference**: For concise API design patterns and conventions, see `.cursor/rules/api-design.mdc`

## REST Endpoint Patterns

### Standard CRUD Endpoints
All entities follow the same REST pattern:

```
GET    /api/{resource}          - List all (with pagination)
GET    /api/{resource}/{id}     - Get by ID
POST   /api/{resource}          - Create new
PUT    /api/{resource}/{id}     - Update existing
DELETE /api/{resource}/{id}     - Delete
```

### Sub-resource Endpoints
For related entities, use sub-resource patterns:

```
GET    /api/transactions/{id}/responsibilities
POST   /api/transactions/{id}/responsibilities
PUT    /api/transactions/{id}/responsibilities/{respId}
DELETE /api/transactions/{id}/responsibilities/{respId}
```

## HTTP Status Codes

### Success Responses
- `200 OK` - Successful GET, PUT operations
- `201 Created` - Successful POST operations
- `204 No Content` - Successful DELETE operations

### Error Responses
- `400 Bad Request` - Validation errors, invalid input
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate name)
- `500 Internal Server Error` - Server errors

## Standardized Response Patterns

### Success Response Wrapper
All successful responses are wrapped in a standardized `ApiResponse<T>` structure:

```json
{
  "success": true,
  "data": {
    // Actual response data here
  },
  "message": "Operation completed successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}
```

### Pagination Response
Paginated responses include the pagination metadata within the data field:

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
    "totalElements": 100,
    "totalPages": 5,
    "last": false,
    "first": true,
    "numberOfElements": 20
  },
  "message": "Entities retrieved successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}
```

### Error Response Format
All error responses follow a standardized `ErrorResponse` structure:

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

### Common Error Types
- `VALIDATION_ERROR` - Bean validation failures
- `NOT_FOUND` - Entity not found
- `CONFLICT` - Business rule violations (e.g., duplicate names)
- `UNAUTHORIZED` - Authentication required
- `FORBIDDEN` - Insufficient permissions
- `INTERNAL_ERROR` - Unexpected server errors

## Authentication & Authorization

### JWT Token Authentication
- All endpoints require JWT token in Authorization header
- Format: `Authorization: Bearer {token}`
- Token obtained via `/api/auth/login` endpoint

### User Context
- All user-scoped operations automatically use the authenticated user
- User ID extracted from JWT token via `UserContext`

## Validation Patterns

### Request Validation
- Use `@Valid` annotation on request bodies
- Implement validation in DTO classes using Bean Validation annotations
- Custom validation in service layer for business rules

### Common Validation Annotations
```java
@NotNull(message = "Field is required")
@NotBlank(message = "Field cannot be empty")
@Size(min = 1, max = 255, message = "Field length must be between 1 and 255")
@Email(message = "Invalid email format")
@Min(value = 0, message = "Value must be greater than or equal to 0")
@Max(value = 100, message = "Value must be less than or equal to 100")
@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Field must contain only alphanumeric characters")
```

## Response DTO Patterns

### Standard Response Fields
All response DTOs extend `BaseDTO<T>` and include:
- `id` - Entity ID
- `createdAt` - Creation timestamp
- `updatedAt` - Last update timestamp

### Nested Object Patterns
```json
{
  "success": true,
  "data": {
    "id": 1,
    "description": "Groceries",
    "amount": 150.00,
    "type": "EXPENSE",
    "category": {
      "id": 1,
      "name": "Food"
    },
    "user": {
      "id": 1,
      "email": "user@example.com",
      "fullName": "John Doe"
    },
    "createdAt": "2024-01-15T10:30:00Z",
    "updatedAt": "2024-01-15T10:30:00Z"
  },
  "message": "Transaction retrieved successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}
```

## Query Parameters

### Filtering
Use query parameters for filtering:
```
GET /api/transactions?type=EXPENSE&categoryId=1&startDate=2024-01-01&endDate=2024-01-31
```

### Sorting
Use `sort` parameter for sorting:
```
GET /api/transactions?sort=date,desc&sort=amount,asc
```

### Pagination
Use `page` and `size` parameters:
```
GET /api/transactions?page=0&size=20
```

## API Versioning

### URL Versioning
Use version in URL path:
```
/api/v1/transactions
/api/v2/transactions
```

### Content Negotiation
Use Accept header for versioning:
```
Accept: application/vnd.finance-control.v1+json
```

## Rate Limiting

### Standard Limits
- 100 requests per minute per user
- 1000 requests per hour per user
- Burst limit: 10 requests per second

### Rate Limit Headers
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642234567
```

## CORS Configuration

### Allowed Origins
- Development: `http://localhost:3000`
- Production: `https://yourdomain.com`

### Allowed Methods
- GET, POST, PUT, DELETE, OPTIONS

### Allowed Headers
- Content-Type, Authorization, X-Requested-With

## Monitoring & Logging

### Request Logging
- Log all API requests with user context
- Include request ID for tracing
- Log response times for performance monitoring

### Error Logging
- Log all errors with stack traces
- Include request context in error logs
- Monitor error rates and patterns

## Security Headers

### Standard Headers
```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

## API Documentation

### OpenAPI/Swagger
- Auto-generated API documentation
- Available at `/swagger-ui.html`
- Include examples and descriptions for all endpoints

### Documentation Standards
- Use `@Operation` for endpoint descriptions
- Use `@Parameter` for parameter descriptions
- Use `@Schema` for model descriptions
- Include example requests and responses

## Implementation Guidelines

### Controller Response Wrapping
All controllers extending `BaseController` automatically wrap responses:

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<FinancialGoalDTO>> findById(@PathVariable Long id) {
    return service.findById(id)
            .map(entity -> ResponseEntity.ok(ApiResponse.success(entity, "Entity found")))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Entity not found with ID: " + id)));
}
```

### Global Exception Handling
All exceptions are handled centrally by `GlobalExceptionHandler`:

```java
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
```

### Benefits of Standardized Responses
1. **Consistency**: All endpoints return the same response structure
2. **Error Handling**: Centralized error handling with detailed information
3. **Validation**: Clear validation error messages with field details
4. **Debugging**: Timestamp and path information for easier debugging
5. **Frontend Integration**: Predictable response format for frontend consumption
6. **Documentation**: Clear API documentation with consistent examples
