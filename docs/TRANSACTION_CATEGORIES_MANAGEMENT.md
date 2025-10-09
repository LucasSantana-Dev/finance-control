# Transaction Categories Management

This document describes the Transaction Categories Management feature in the Finance Control application, which provides comprehensive CRUD operations for managing transaction categories and subcategories.

## Overview

The Transaction Categories Management system allows users to organize their financial transactions into hierarchical categories and subcategories, providing better organization and reporting capabilities.

## Architecture

### Entities

#### TransactionCategory
- **Table**: `transaction_categories`
- **Purpose**: Main categories for organizing transactions
- **Fields**:
  - `id`: Primary key (auto-generated)
  - `name`: Category name (required, unique)
  - `created_at`: Creation timestamp
  - `updated_at`: Last modification timestamp

#### TransactionSubcategory
- **Table**: `transaction_subcategories`
- **Purpose**: Subcategories within main categories
- **Fields**:
  - `id`: Primary key (auto-generated)
  - `name`: Subcategory name (required, unique)
  - `description`: Optional description
  - `is_active`: Active status flag
  - `category_id`: Foreign key to TransactionCategory
  - `created_at`: Creation timestamp
  - `updated_at`: Last modification timestamp

### Relationships

```
TransactionCategory (1) -----> (N) TransactionSubcategory
TransactionCategory (1) -----> (N) Transaction
TransactionSubcategory (1) -----> (N) Transaction
```

## API Endpoints

### Transaction Categories

#### Base URL: `/transaction-categories`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/transaction-categories` | Get paginated list of categories |
| GET | `/transaction-categories/{id}` | Get category by ID |
| POST | `/transaction-categories` | Create new category |
| PUT | `/transaction-categories/{id}` | Update category |
| DELETE | `/transaction-categories/{id}` | Delete category |

#### Query Parameters (GET /transaction-categories)
- `search`: Search term for filtering categories
- `sortBy`: Field to sort by (default: name)
- `sortDirection`: Sort direction (asc/desc, default: asc)
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)

### Transaction Subcategories

#### Base URL: `/transaction-subcategories`

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/transaction-subcategories` | Get paginated list of subcategories |
| GET | `/transaction-subcategories/{id}` | Get subcategory by ID |
| POST | `/transaction-subcategories` | Create new subcategory |
| PUT | `/transaction-subcategories/{id}` | Update subcategory |
| DELETE | `/transaction-subcategories/{id}` | Delete subcategory |
| GET | `/transaction-subcategories/category/{categoryId}` | Get subcategories by category |
| GET | `/transaction-subcategories/category/{categoryId}/usage` | Get subcategories ordered by usage |
| GET | `/transaction-subcategories/category/{categoryId}/count` | Get subcategory count by category |

## Data Transfer Objects (DTOs)

### TransactionCategoryDTO
```json
{
  "id": 1,
  "name": "Food & Dining",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### TransactionSubcategoryDTO
```json
{
  "id": 1,
  "name": "Restaurants",
  "description": "Dining out at restaurants",
  "isActive": true,
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

## Business Logic

### Validation Rules

#### TransactionCategory
- Name is required and must be unique (case-insensitive)
- Name cannot be empty or contain only whitespace
- Maximum name length: 255 characters

#### TransactionSubcategory
- Name is required and must be unique (case-insensitive)
- Description is optional (max 500 characters)
- Category relationship is required
- isActive defaults to true

### Service Methods

#### TransactionCategoryService
- `create(TransactionCategoryDTO)`: Create new category with duplicate name validation
- `findById(Long)`: Find category by ID
- `findAll(Pageable)`: Get paginated list of categories
- `update(Long, TransactionCategoryDTO)`: Update category with validation
- `delete(Long)`: Delete category (soft delete if transactions exist)
- `findByName(String)`: Find category by name (case-insensitive)
- `existsByName(String)`: Check if category exists by name

#### TransactionSubcategoryService
- `create(TransactionSubcategoryDTO)`: Create new subcategory
- `findById(Long)`: Find subcategory by ID
- `findAll(Pageable)`: Get paginated list of subcategories
- `update(Long, TransactionSubcategoryDTO)`: Update subcategory
- `delete(Long)`: Delete subcategory
- `findByCategoryId(Long)`: Get subcategories by category ID
- `findByCategoryIdOrderByUsage(Long)`: Get subcategories ordered by transaction count
- `countByCategoryId(Long)`: Count subcategories by category ID

## Database Schema

### transaction_categories
```sql
CREATE TABLE transaction_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_transaction_category_name ON transaction_categories(name);
```

### transaction_subcategories
```sql
CREATE TABLE transaction_subcategories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    category_id BIGINT REFERENCES transaction_categories(id)
);

CREATE INDEX idx_transaction_subcategory_category ON transaction_subcategories(category_id);
CREATE INDEX idx_transaction_subcategory_name ON transaction_subcategories(name);
```

## Testing

### Unit Tests
- **Controller Tests**: Test all REST endpoints with mocked services
- **Service Tests**: Test business logic with mocked repositories
- **Repository Tests**: Test JPA operations with in-memory H2 database

### Integration Tests
- **TestContainers Tests**: Test full application context with real PostgreSQL
- **End-to-End Tests**: Test complete workflows from API to database

### Test Coverage
- **Target**: 80% minimum code coverage
- **Focus Areas**: Business logic, validation, error handling
- **Test Types**: Unit, integration, and repository tests

## Usage Examples

### Creating a Category
```bash
curl -X POST http://localhost:8080/transaction-categories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Food & Dining"
  }'
```

### Creating a Subcategory
```bash
curl -X POST http://localhost:8080/transaction-subcategories \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Restaurants",
    "description": "Dining out at restaurants",
    "isActive": true
  }'
```

### Getting Categories with Pagination
```bash
curl "http://localhost:8080/transaction-categories?page=0&size=10&sortBy=name&sortDirection=asc"
```

### Getting Subcategories by Category
```bash
curl "http://localhost:8080/transaction-subcategories/category/1"
```

## Error Handling

### Common Error Responses

#### 400 Bad Request
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Category name is required",
  "path": "/transaction-categories"
}
```

#### 409 Conflict
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "Category with name 'Food & Dining' already exists",
  "path": "/transaction-categories"
}
```

#### 404 Not Found
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Category with ID 999 not found",
  "path": "/transaction-categories/999"
}
```

## Performance Considerations

### Database Indexes
- Primary key indexes on all tables
- Index on category name for fast lookups
- Index on subcategory category_id for relationship queries
- Index on subcategory name for search operations

### Caching Strategy
- Consider caching frequently accessed categories
- Implement cache invalidation on category updates
- Use Redis for distributed caching in production

### Query Optimization
- Use pagination for large result sets
- Implement lazy loading for relationships
- Consider read replicas for reporting queries

## Security Considerations

### Access Control
- Implement role-based access control
- Validate user permissions for category operations
- Audit category changes for compliance

### Data Validation
- Sanitize input data to prevent injection attacks
- Validate file uploads if category icons are supported
- Implement rate limiting for API endpoints

## Future Enhancements

### Planned Features
- **Category Icons**: Support for category icons/images
- **Category Colors**: Color coding for visual organization
- **Category Templates**: Predefined category sets for different use cases
- **Category Analytics**: Usage statistics and insights
- **Category Import/Export**: Bulk operations for category management
- **Category Hierarchy**: Support for multi-level category nesting

### Technical Improvements
- **Caching**: Implement Redis caching for better performance
- **Search**: Full-text search capabilities
- **Bulk Operations**: Batch create/update/delete operations
- **API Versioning**: Support for API versioning
- **GraphQL**: Alternative API interface for complex queries

## Related Documentation

- [API Documentation](API_PATTERNS.md)
- [Database Schema](database-schema.uml)
- [Testing Strategy](TESTING_STRATEGY.md)
- [MapStruct Integration](MAPSTRUCT_INTEGRATION.md)
