# Finance Control API Documentation

This comprehensive guide covers all aspects of the Finance Control API, including design patterns, CRUD operations, unified endpoints, best practices, and examples.

## Table of Contents

1. [API Design Patterns](#api-design-patterns)
2. [CRUD Operations Guide](#crud-operations-guide)
3. [Unified Endpoints Reference](#unified-endpoints-reference)
4. [Best Practices](#best-practices)
5. [Examples](#examples)

---

## API Design Patterns

### REST Endpoint Patterns

#### Standard CRUD Endpoints
All entities follow the same REST pattern:

```
GET    /api/{resource}          - List all (with pagination)
GET    /api/{resource}/{id}     - Get by ID
POST   /api/{resource}          - Create new
PUT    /api/{resource}/{id}     - Update existing
DELETE /api/{resource}/{id}     - Delete
```

#### Sub-resource Endpoints
For related entities, use sub-resource patterns:

```
GET    /api/transactions/{id}/responsibilities
POST   /api/transactions/{id}/responsibilities
PUT    /api/transactions/{id}/responsibilities/{respId}
DELETE /api/transactions/{id}/responsibilities/{respId}
```

### HTTP Status Codes

#### Success Responses
- `200 OK` - Successful GET, PUT operations
- `201 Created` - Successful POST operations
- `204 No Content` - Successful DELETE operations

#### Error Responses
- `400 Bad Request` - Validation errors, invalid input
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., duplicate name)
- `500 Internal Server Error` - Server errors

### Standardized Response Patterns

#### Success Response Wrapper
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

#### Pagination Response
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

#### Error Response Format
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

#### Common Error Types
- `VALIDATION_ERROR` - Bean validation failures
- `NOT_FOUND` - Entity not found
- `CONFLICT` - Business rule violations (e.g., duplicate names)
- `UNAUTHORIZED` - Authentication required
- `FORBIDDEN` - Insufficient permissions
- `INTERNAL_ERROR` - Unexpected server errors

### Authentication & Authorization

#### JWT Token Authentication
- All endpoints require JWT token in Authorization header
- Format: `Authorization: Bearer {token}`
- Token obtained via `/api/auth/login` endpoint

#### User Context
- All user-scoped operations automatically use the authenticated user
- User ID extracted from JWT token via `UserContext`

### Validation Patterns

#### Request Validation
- Use `@Valid` annotation on request bodies
- Implement validation in DTO classes using Bean Validation annotations
- Custom validation in service layer for business rules

#### Common Validation Annotations
```java
@NotNull(message = "Field is required")
@NotBlank(message = "Field cannot be empty")
@Size(min = 1, max = 255, message = "Field length must be between 1 and 255")
@Email(message = "Invalid email format")
@Min(value = 0, message = "Value must be greater than or equal to 0")
@Max(value = 100, message = "Value must be less than or equal to 100")
@Pattern(regexp = "^[A-Za-z0-9]+$", message = "Field must contain only alphanumeric characters")
```

### Response DTO Patterns

#### Standard Response Fields
All response DTOs extend `BaseDTO<T>` and include:
- `id` - Entity ID
- `createdAt` - Creation timestamp
- `updatedAt` - Last update timestamp

#### Nested Object Patterns
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

### Query Parameters

#### Filtering
Use query parameters for filtering:
```
GET /api/transactions?type=EXPENSE&categoryId=1&startDate=2024-01-01&endDate=2024-01-31
```

#### Sorting
Use `sort` parameter for sorting:
```
GET /api/transactions?sort=date,desc&sort=amount,asc
```

#### Pagination
Use `page` and `size` parameters:
```
GET /api/transactions?page=0&size=20
```

### Security Headers

#### Standard Headers
```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

### API Documentation

#### OpenAPI/Swagger
- Auto-generated API documentation
- Available at `/swagger-ui.html`
- Include examples and descriptions for all endpoints

#### Documentation Standards
- Use `@Operation` for endpoint descriptions
- Use `@Parameter` for parameter descriptions
- Use `@Schema` for model descriptions
- Include example requests and responses

---

## CRUD Operations Guide

### Architecture Overview

The Finance Control API uses a consistent architecture across all modules to provide standardized CRUD operations with proper documentation and type safety.

### Core Components

#### 1. `CrudApi<I, D>` Interface
- Defines the contract for standard CRUD operations
- Contains OpenAPI annotations for documentation
- Provides consistent API structure across all controllers
- Uses a single DTO type for all operations (create, update, response)

#### 2. `BaseController<T, I, D>` Class
- Implements the `CrudApi` interface
- Provides concrete implementations of all CRUD operations
- Contains OpenAPI annotations for proper Swagger documentation
- Handles common functionality like filtering, pagination, and parameter conversion
- Single source of truth for CRUD operations with documentation
- Simplified generic type parameters for easier maintenance

### Generic Type Parameters

The architecture uses 3 generic type parameters:
- **T**: Entity type (e.g., `User`, `Transaction`)
- **I**: ID type (e.g., `Long`, `UUID`)
- **D**: DTO type used for all operations (create, update, response)

### Available Endpoints

All controllers extending `BaseController` automatically get these endpoints:

- `GET /{resource}` - List entities with pagination, search, and filtering
- `GET /{resource}/{id}` - Get entity by ID
- `POST /{resource}` - Create new entity
- `PUT /{resource}/{id}` - Update existing entity
- `DELETE /{resource}/{id}` - Delete entity

### Query Parameters

The list endpoint supports these query parameters:

- `search` - Search term to filter entities
- `sortBy` - Field name to sort by
- `sortDirection` - Sort direction (asc/desc, defaults to asc)
- `page` - Page number for pagination (0-based)
- `size` - Page size for pagination
- Additional custom filters can be added as query parameters

### Implementation Example

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController extends BaseController<User, Long, UserDTO> {

    public UserController(UserService userService) {
        super(userService);
    }

    // CRUD endpoints are inherited and properly documented
    // Additional custom endpoints can be added here
}
```

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

### Benefits of Standardized CRUD Operations

1. **Consistency**: All endpoints return the same response structure
2. **Error Handling**: Centralized error handling with detailed information
3. **Validation**: Clear validation error messages with field details
4. **Debugging**: Timestamp and path information for easier debugging
5. **Frontend Integration**: Predictable response format for frontend consumption
6. **Documentation**: Clear API documentation with consistent examples
7. **Maintainability**: Changes to CRUD operations only need to be made in one place
8. **Type Safety**: Generic type parameters ensure type safety across the API
9. **Flexibility**: Controllers can still add custom endpoints while inheriting standard CRUD operations

### Security Considerations

The JWT filter is configured to skip Swagger UI and API documentation endpoints:

- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/webjars/**`
- `/auth/**`
- `/public/**`
- `/actuator/**`

This ensures that API documentation is accessible without authentication while maintaining security for actual API endpoints.

---

## Unified Endpoints Reference

The Finance Control API uses a unified endpoint approach that provides flexibility, consistency, and extensibility across all modules. Instead of having multiple specific endpoints for each operation, we use flexible, query-parameter-based endpoints.

### Key Benefits

1. **Flexibility**: Single endpoint can handle multiple use cases including metadata
2. **Consistency**: Uniform parameter naming and response format
3. **Extensibility**: Easy to add new sorting/filtering options
4. **Maintainability**: Less code duplication
5. **API Discoverability**: Clear parameter documentation
6. **Simplicity**: No need for separate metadata endpoints

### Module Endpoints

#### 1. Transactions Module

**Endpoint**: `GET /api/transactions`

Retrieve transactions with flexible filtering, sorting, and pagination options, or metadata.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `userId` | Long | No | User ID for filtering | `1` |
| `type` | TransactionType | No | Transaction type filter | `INCOME`, `EXPENSE` |
| `categoryId` | Long | No | Category ID filter | `1` |
| `subcategoryId` | Long | No | Subcategory ID filter | `1` |
| `sourceEntityId` | Long | No | Source entity ID filter | `1` |
| `search` | String | No | Search term for description | `grocery` |
| `sortBy` | String | No | Sort field | `createdAt`, `amount`, `date` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `minAmount` | BigDecimal | No | Minimum amount filter | `100.00` |
| `maxAmount` | BigDecimal | No | Maximum amount filter | `1000.00` |
| `startDate` | LocalDate | No | Start date filter | `2024-01-01` |
| `endDate` | LocalDate | No | End date filter | `2024-12-31` |
| `data` | String | No | Type of data to retrieve (metadata) | `categories`, `types`, `monthly-summary` |

**Examples:**
```bash
# Get all transactions for user 1
GET /api/transactions?userId=1

# Get expense transactions sorted by amount descending
GET /api/transactions?userId=1&type=EXPENSE&sortBy=amount&sortDirection=desc

# Get transactions in a date range
GET /api/transactions?userId=1&startDate=2024-01-01&endDate=2024-01-31

# Search for transactions containing "grocery"
GET /api/transactions?userId=1&search=grocery

# Get transaction categories (metadata)
GET /api/transactions?userId=1&data=categories

# Get transaction types (metadata)
GET /api/transactions?data=types

# Get monthly summary (metadata)
GET /api/transactions?userId=1&data=monthly-summary&startDate=2024-01-01&endDate=2024-12-31
```

#### 2. Financial Goals Module

**Endpoint**: `GET /api/financial-goals`

Retrieve financial goals with flexible filtering, sorting, and pagination options, or metadata.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `userId` | Long | No | User ID for filtering | `1` |
| `goalType` | GoalType | No | Goal type filter | `SAVINGS`, `INVESTMENT`, `DEBT_PAYOFF` |
| `status` | String | No | Status filter | `active`, `completed` |
| `search` | String | No | Search term for name or description | `vacation` |
| `sortBy` | String | No | Sort field | `deadline`, `name`, `targetAmount` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `minTargetAmount` | BigDecimal | No | Minimum target amount filter | `1000.00` |
| `maxTargetAmount` | BigDecimal | No | Maximum target amount filter | `50000.00` |
| `deadlineStart` | LocalDate | No | Deadline start date filter | `2024-01-01` |
| `deadlineEnd` | LocalDate | No | Deadline end date filter | `2024-12-31` |
| `data` | String | No | Type of data to retrieve (metadata) | `types`, `status-summary`, `progress-summary`, `deadline-alerts` |

**Examples:**
```bash
# Get all financial goals for user 1
GET /api/financial-goals?userId=1

# Get active savings goals sorted by deadline
GET /api/financial-goals?userId=1&goalType=SAVINGS&status=active&sortBy=deadline&sortDirection=asc

# Get goals with target amount between 1000 and 10000
GET /api/financial-goals?userId=1&minTargetAmount=1000&maxTargetAmount=10000

# Get goal types (metadata)
GET /api/financial-goals?data=types

# Get status summary (metadata)
GET /api/financial-goals?userId=1&data=status-summary
```

**Metadata Endpoint**: `GET /api/financial-goals/metadata`

Retrieve financial goals metadata and analytics data.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `userId` | Long | No | User ID for filtering | `1` |
| `data` | String | Yes | Type of metadata to retrieve | `types`, `status-summary`, `progress-summary`, `deadline-alerts`, `completion-rate`, `average-completion-time` |
| `goalType` | GoalType | No | Goal type filter | `SAVINGS`, `INVESTMENT` |
| `status` | String | No | Status filter | `active`, `completed` |

**Examples:**
```bash
# Get all goal types
GET /api/financial-goals/metadata?data=types

# Get status summary for user
GET /api/financial-goals/metadata?userId=1&data=status-summary

# Get deadline alerts for user
GET /api/financial-goals/metadata?userId=1&data=deadline-alerts
```

#### 3. Investments Module

**Endpoint**: `GET /api/investments`

Retrieve investments with flexible filtering, sorting, and pagination options, or metadata.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `type` | InvestmentType | No | Investment type filter | `STOCK`, `FII`, `BOND`, `ETF`, `CRYPTO` |
| `subtype` | InvestmentSubtype | No | Investment subtype filter | `ORDINARY`, `PREFERRED`, `TIJOLO`, `PAPEL` |
| `sector` | String | No | Sector filter | `Energy`, `Technology`, `Finance` |
| `industry` | String | No | Industry filter | `Oil & Gas`, `Software`, `Banking` |
| `exchange` | String | No | Exchange filter | `B3`, `NYSE`, `NASDAQ` |
| `search` | String | No | Search term for ticker or name | `PETR4`, `VALE3` |
| `sortBy` | String | No | Sort field | `createdAt`, `currentPrice`, `dayChangePercent`, `dividendYield` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `minPrice` | BigDecimal | No | Minimum current price filter | `10.00` |
| `maxPrice` | BigDecimal | No | Maximum current price filter | `100.00` |
| `minDividendYield` | BigDecimal | No | Minimum dividend yield filter | `5.0` |
| `maxDividendYield` | BigDecimal | No | Maximum dividend yield filter | `15.0` |
| `data` | String | No | Type of data to retrieve (metadata) | `sectors`, `industries`, `types`, `subtypes`, `exchanges`, `top-performers`, `worst-performers`, `top-dividend-yield`, `portfolio-summary` |

**Examples:**
```bash
# Get all investments for authenticated user
GET /api/investments

# Get stock investments sorted by current price descending
GET /api/investments?type=STOCK&sortBy=currentPrice&sortDirection=desc

# Get investments with high dividend yield
GET /api/investments?minDividendYield=8.0&sortBy=dividendYield&sortDirection=desc

# Search for specific ticker
GET /api/investments?search=PETR4

# Get investments by sector with price range
GET /api/investments?sector=Energy&minPrice=20.00&maxPrice=50.00

# Get portfolio summary metadata
GET /api/investments?data=portfolio-summary

# Get top performing investments
GET /api/investments?data=top-performers&page=0&size=10

# Get available sectors
GET /api/investments?data=sectors

# Get investment subtypes for stocks
GET /api/investments?data=subtypes&type=STOCK

# Get supported exchanges
GET /api/investments?data=exchanges
```

**Metadata Types:**
- `sectors`: Returns list of available sectors
- `industries`: Returns list of available industries
- `types`: Returns list of available investment types
- `subtypes`: Returns list of available subtypes (requires `type` parameter)
- `exchanges`: Returns map of supported exchanges
- `top-performers`: Returns investments with highest day change
- `worst-performers`: Returns investments with lowest day change
- `top-dividend-yield`: Returns investments with highest dividend yield
- `portfolio-summary`: Returns portfolio summary with total market value and breakdown

#### 4. Dashboard Module

**Endpoint**: `GET /api/dashboard`

Retrieve dashboard data with flexible filtering and date range options.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `data` | String | Yes | Type of dashboard data to retrieve | `summary`, `metrics`, `spending-categories`, `monthly-trends`, `current-month-metrics`, `year-to-date-metrics` |
| `startDate` | LocalDate | No | Start date (required for metrics) | `2024-01-01` |
| `endDate` | LocalDate | No | End date (required for metrics) | `2024-12-31` |
| `limit` | Integer | No | Number of items to return (for lists) | `5`, `10`, `20` |
| `months` | Integer | No | Number of months for trends | `6`, `12`, `24` |

**Examples:**
```bash
# Get dashboard summary
GET /api/dashboard?data=summary

# Get financial metrics for date range
GET /api/dashboard?data=metrics&startDate=2024-01-01&endDate=2024-12-31

# Get top 10 spending categories
GET /api/dashboard?data=spending-categories&limit=10

# Get monthly trends for last 6 months
GET /api/dashboard?data=monthly-trends&months=6
```

#### 5. Transaction Categories Module

**Endpoint**: `GET /api/transaction-categories`

Retrieve transaction categories with flexible filtering, sorting, and pagination options.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `search` | String | No | Search term for name | `food` |
| `sortBy` | String | No | Sort field | `name`, `createdAt` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |

**Examples:**
```bash
# Get all categories sorted by name
GET /api/transaction-categories?sortBy=name&sortDirection=asc

# Search for categories containing "food"
GET /api/transaction-categories?search=food
```

**Metadata Endpoint**: `GET /api/transaction-categories/metadata`

Retrieve transaction categories metadata and analytics data.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `data` | String | Yes | Type of metadata to retrieve | `all`, `count`, `usage-stats` |

**Examples:**
```bash
# Get all active categories
GET /api/transaction-categories/metadata?data=all

# Get total count of categories
GET /api/transaction-categories/metadata?data=count

# Get usage statistics
GET /api/transaction-categories/metadata?data=usage-stats
```

#### 6. Transaction Subcategories Module

**Endpoint**: `GET /api/transaction-subcategories`

Retrieve transaction subcategories with flexible filtering, sorting, and pagination options.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `categoryId` | Long | No | Category ID filter | `1` |
| `search` | String | No | Search term for name | `restaurant` |
| `sortBy` | String | No | Sort field | `name`, `createdAt` |
| `sortDirection` | String | No | Sort direction | `asc`, `desc` |
| `page` | Integer | No | Page number (0-based) | `0`, `1`, `2` |
| `size` | Integer | No | Page size | `10`, `20`, `50` |
| `sortByUsage` | Boolean | No | Sort by usage (true/false) | `true`, `false` |

**Examples:**
```bash
# Get all subcategories for category 1
GET /api/transaction-subcategories?categoryId=1

# Get subcategories sorted by usage
GET /api/transaction-subcategories?categoryId=1&sortByUsage=true

# Search for subcategories containing "restaurant"
GET /api/transaction-subcategories?search=restaurant
```

**Metadata Endpoint**: `GET /api/transaction-subcategories/metadata`

Retrieve transaction subcategories metadata and analytics data.

**Query Parameters:**

| Parameter | Type | Required | Description | Example Values |
|-----------|------|----------|-------------|----------------|
| `data` | String | Yes | Type of metadata to retrieve | `all`, `by-category`, `by-category-usage`, `count`, `count-by-category` |
| `categoryId` | Long | No | Required for category-specific data | `1` |

**Examples:**
```bash
# Get all active subcategories
GET /api/transaction-subcategories/metadata?data=all

# Get subcategories for category 1
GET /api/transaction-subcategories/metadata?data=by-category&categoryId=1

# Get subcategories for category 1 ordered by usage
GET /api/transaction-subcategories/metadata?data=by-category-usage&categoryId=1
```

### Response Formats

#### Paginated Response
```json
{
  "content": [...],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false
    },
    "pageNumber": 0,
    "pageSize": 20,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0,
  "sort": {
    "sorted": true,
    "unsorted": false
  }
}
```

#### Metadata Response Examples

**Categories List:**
```json
[
  {
    "id": 1,
    "name": "Food & Dining",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

**Usage Statistics:**
```json
{
  "totalCategories": 10,
  "usedCategories": 8
}
```

**Status Summary:**
```json
{
  "activeCount": 5,
  "completedCount": 3,
  "totalCount": 8
}
```

### Error Handling

The unified endpoints provide clear error messages for invalid parameters:

```json
{
  "error": "Invalid data type: invalid-type"
}
```

```json
{
  "error": "Category ID is required for subcategories data"
}
```

```json
{
  "error": "Start date and end date are required for metrics data"
}
```

---

## Best Practices

### API Design

1. **Use Query Parameters for Filtering**
   - Use query parameters instead of path parameters for filtering
   - Provides flexibility and maintains clean URLs
   - Example: `GET /api/transactions?type=EXPENSE&categoryId=1`

2. **Provide Default Values**
   - Always provide sensible defaults for optional parameters
   - Example: Default page size of 20, default sort direction of ascending

3. **Use Consistent Naming**
   - Maintain consistent parameter names across all modules
   - Use camelCase for parameter names
   - Use plural nouns for collection endpoints

4. **Document All Parameters**
   - Provide clear descriptions for all parameters
   - Include example values and validation rules
   - Use OpenAPI/Swagger annotations

5. **Handle Validation Errors Gracefully**
   - Return detailed error messages with field information
   - Use appropriate HTTP status codes
   - Include validation constraints in error responses

6. **Support Pagination**
   - Always paginate list endpoints to prevent performance issues
   - Use consistent pagination parameters (page, size)
   - Include total count and page metadata in responses

7. **Use Appropriate HTTP Status Codes**
   - 200 for successful GET/PUT
   - 201 for successful POST
   - 204 for successful DELETE
   - 400 for validation errors
   - 404 for not found
   - 409 for conflicts
   - 500 for server errors

8. **Provide Meaningful Error Messages**
   - Include context about what went wrong
   - Provide guidance on how to fix the issue
   - Include request path and timestamp

### Security

1. **JWT Authentication**
   - Require JWT token for all protected endpoints
   - Extract user context from JWT token
   - Use consistent Authorization header format

2. **Input Validation**
   - Validate all user input at the controller level
   - Use Bean Validation annotations
   - Implement custom validation for business rules

3. **Security Headers**
   - Include security headers in all responses
   - Implement CORS configuration
   - Use HTTPS in production

4. **Rate Limiting**
   - Implement rate limiting to prevent abuse
   - Use reasonable limits (100 req/min, 1000 req/hour)
   - Return rate limit information in headers

### Performance

1. **Caching**
   - Implement response caching for frequently accessed data
   - Use appropriate cache expiration policies
   - Consider using ETags for conditional requests

2. **Database Optimization**
   - Use indexed fields for filtering and sorting
   - Implement pagination to limit result sets
   - Use database-level filtering instead of application-level

3. **Response Compression**
   - Enable GZIP compression for responses
   - Reduces bandwidth and improves response times

### Monitoring & Logging

1. **Request Logging**
   - Log all API requests with user context
   - Include request ID for tracing
   - Log response times for performance monitoring

2. **Error Logging**
   - Log all errors with stack traces
   - Include request context in error logs
   - Monitor error rates and patterns

3. **Metrics**
   - Track API usage metrics
   - Monitor endpoint performance
   - Alert on anomalies

### Testing

All endpoints should be tested with:

1. **Valid Parameters** - Ensure correct responses
2. **Invalid Parameters** - Ensure proper error handling
3. **Edge Cases** - Empty results, large datasets
4. **Pagination** - Different page sizes and numbers
5. **Sorting** - All sort fields and directions
6. **Filtering** - All filter combinations
7. **Performance** - Response times with large datasets

---

## Examples

### Complete CRUD Operations Example

```bash
# Create a new financial goal
POST /api/financial-goals
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "Emergency Fund",
  "description": "Save 6 months of expenses",
  "targetAmount": 30000.00,
  "currentAmount": 5000.00,
  "deadline": "2024-12-31",
  "goalType": "SAVINGS",
  "status": "ACTIVE",
  "priority": "HIGH"
}

# Response (201 Created)
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Emergency Fund",
    "description": "Save 6 months of expenses",
    "targetAmount": 30000.00,
    "currentAmount": 5000.00,
    "deadline": "2024-12-31",
    "goalType": "SAVINGS",
    "status": "ACTIVE",
    "priority": "HIGH",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "message": "Financial goal created successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}

# Get financial goal by ID
GET /api/financial-goals/1
Authorization: Bearer {token}

# Response (200 OK)
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Emergency Fund",
    "description": "Save 6 months of expenses",
    "targetAmount": 30000.00,
    "currentAmount": 5000.00,
    "deadline": "2024-12-31",
    "goalType": "SAVINGS",
    "status": "ACTIVE",
    "priority": "HIGH",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "message": "Financial goal retrieved successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}

# Update financial goal
PUT /api/financial-goals/1
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "Emergency Fund",
  "description": "Save 6 months of expenses",
  "targetAmount": 30000.00,
  "currentAmount": 10000.00,
  "deadline": "2024-12-31",
  "goalType": "SAVINGS",
  "status": "ACTIVE",
  "priority": "HIGH"
}

# Response (200 OK)
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Emergency Fund",
    "description": "Save 6 months of expenses",
    "targetAmount": 30000.00,
    "currentAmount": 10000.00,
    "deadline": "2024-12-31",
    "goalType": "SAVINGS",
    "status": "ACTIVE",
    "priority": "HIGH",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T11:00:00"
  },
  "message": "Financial goal updated successfully",
  "timestamp": "2024-01-15T11:00:00",
  "path": null
}

# Delete financial goal
DELETE /api/financial-goals/1
Authorization: Bearer {token}

# Response (204 No Content)
```

### Filtering and Pagination Example

```bash
# List all active financial goals with pagination
GET /api/financial-goals?status=active&page=0&size=10&sortBy=deadline&sortDirection=asc
Authorization: Bearer {token}

# Response (200 OK)
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Emergency Fund",
        "targetAmount": 30000.00,
        "currentAmount": 10000.00,
        "deadline": "2024-06-30",
        "goalType": "SAVINGS",
        "status": "ACTIVE"
      },
      {
        "id": 2,
        "name": "Vacation Fund",
        "targetAmount": 5000.00,
        "currentAmount": 2500.00,
        "deadline": "2024-08-15",
        "goalType": "SAVINGS",
        "status": "ACTIVE"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false
      }
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "first": true,
    "numberOfElements": 2
  },
  "message": "Financial goals retrieved successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}
```

### Metadata and Analytics Example

```bash
# Get investment portfolio summary
GET /api/investments?data=portfolio-summary
Authorization: Bearer {token}

# Response (200 OK)
{
  "success": true,
  "data": {
    "totalMarketValue": 150000.00,
    "totalCost": 120000.00,
    "totalProfit": 30000.00,
    "profitPercentage": 25.0,
    "byType": {
      "STOCK": {
        "marketValue": 80000.00,
        "cost": 65000.00,
        "profit": 15000.00,
        "profitPercentage": 23.08
      },
      "FII": {
        "marketValue": 50000.00,
        "cost": 40000.00,
        "profit": 10000.00,
        "profitPercentage": 25.0
      },
      "CRYPTO": {
        "marketValue": 20000.00,
        "cost": 15000.00,
        "profit": 5000.00,
        "profitPercentage": 33.33
      }
    }
  },
  "message": "Portfolio summary retrieved successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}

# Get dashboard summary
GET /api/dashboard?data=summary
Authorization: Bearer {token}

# Response (200 OK)
{
  "success": true,
  "data": {
    "totalIncome": 50000.00,
    "totalExpenses": 30000.00,
    "netCashFlow": 20000.00,
    "savingsRate": 40.0,
    "totalInvestments": 150000.00,
    "activeGoalsCount": 5,
    "completedGoalsCount": 3
  },
  "message": "Dashboard summary retrieved successfully",
  "timestamp": "2024-01-15T10:30:00",
  "path": null
}
```

### Error Handling Example

```bash
# Attempt to create financial goal with validation errors
POST /api/financial-goals
Content-Type: application/json
Authorization: Bearer {token}

{
  "name": "",
  "targetAmount": -100.00,
  "currentAmount": 1000.00
}

# Response (400 Bad Request)
{
  "error": "VALIDATION_ERROR",
  "message": "Validation failed",
  "path": "/api/financial-goals",
  "timestamp": "2024-01-15T10:30:00",
  "validationErrors": [
    {
      "field": "name",
      "message": "Name is required",
      "rejectedValue": ""
    },
    {
      "field": "targetAmount",
      "message": "Target amount must be greater than 0",
      "rejectedValue": -100.00
    }
  ]
}

# Attempt to access non-existent resource
GET /api/financial-goals/999
Authorization: Bearer {token}

# Response (404 Not Found)
{
  "error": "NOT_FOUND",
  "message": "Financial goal not found with ID: 999",
  "path": "/api/financial-goals/999",
  "timestamp": "2024-01-15T10:30:00",
  "validationErrors": null
}
```

---

## Migration Guide

### From Old to New Approach

**Old Approach (Multiple Specific Endpoints):**
```bash
GET /api/transactions/performance/top?userId=1
GET /api/transactions/performance/worst?userId=1
GET /api/transactions/dividend-yield/top?userId=1
GET /api/transactions/sectors?userId=1
GET /api/financial-goals/active
GET /api/financial-goals/completed
GET /api/dashboard/summary
GET /api/dashboard/metrics?startDate=2024-01-01&endDate=2024-12-31
```

**New Approach (Unified Endpoints):**
```bash
GET /api/transactions?userId=1&data=top-performers
GET /api/transactions?userId=1&data=worst-performers
GET /api/transactions?userId=1&data=top-dividend-yield
GET /api/transactions?userId=1&data=sectors
GET /api/financial-goals?status=active
GET /api/financial-goals?status=completed
GET /api/dashboard?data=summary
GET /api/dashboard?data=metrics&startDate=2024-01-01&endDate=2024-12-31
```

---

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

---

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

---

## CORS Configuration

### Allowed Origins
- Development: `http://localhost:3000`
- Production: `https://yourdomain.com`

### Allowed Methods
- GET, POST, PUT, DELETE, OPTIONS

### Allowed Headers
- Content-Type, Authorization, X-Requested-With

---

## Future Enhancements

1. **Caching** - Add response caching for frequently accessed data
2. **Rate Limiting** - Implement rate limiting for API endpoints
3. **Bulk Operations** - Support bulk create/update/delete operations
4. **Export Functionality** - Add data export capabilities (CSV, Excel, PDF)
5. **Real-time Updates** - WebSocket support for real-time data
6. **Advanced Filtering** - Support for complex filter expressions
7. **GraphQL Support** - Alternative query language for complex queries
8. **API Gateway** - Centralized API management and routing
9. **Webhooks** - Event-driven notifications for external systems
10. **API Versioning** - Proper versioning strategy for backward compatibility

---

## Quick Reference

### Rule References
- For concise API design patterns: `.cursor/rules/api-design.mdc`
- For BaseController usage patterns: `.cursor/rules/base-classes-usage.mdc`

### Common Endpoints
- Authentication: `/api/auth/login`, `/api/auth/register`
- Swagger UI: `/swagger-ui.html`
- API Docs: `/v3/api-docs`

### Common Headers
```
Authorization: Bearer {token}
Content-Type: application/json
Accept: application/json
```

### Pagination Defaults
- Default page: 0
- Default size: 20
- Maximum size: 100

### Date Format
- ISO 8601: `yyyy-MM-dd` (e.g., `2024-01-15`)
- DateTime: `yyyy-MM-dd'T'HH:mm:ss` (e.g., `2024-01-15T10:30:00`)
