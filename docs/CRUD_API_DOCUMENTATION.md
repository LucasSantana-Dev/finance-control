# CRUD API Documentation Approach

## Overview

This document explains the approach used to provide consistent CRUD API documentation across all controllers while maintaining clean separation between documentation and implementation.

## Architecture

### Components

1. **`CrudApi<I, D>` Interface**
   - Defines the contract for standard CRUD operations
   - Contains OpenAPI annotations for documentation
   - Provides consistent API structure across all controllers
   - Uses a single DTO type for all operations (create, update, response)

2. **`BaseController<T, I, D>` Class**
   - Implements the `CrudApi` interface
   - Provides concrete implementations of all CRUD operations
   - Contains OpenAPI annotations for proper Swagger documentation
   - Handles common functionality like filtering, pagination, and parameter conversion
   - Single source of truth for CRUD operations with documentation
   - Simplified generic type parameters for easier maintenance

## Usage

### For New Controllers

Controllers should extend `BaseController` to get both the CRUD functionality and proper OpenAPI documentation:

```java
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController extends BaseController<User, Long, UserDTO, UserDTO, UserDTO> {

    public UserController(UserService userService) {
        super(userService);
    }

    // Additional custom endpoints...
}
```

### Benefits

1. **Consistent Documentation**: All controllers implementing `CrudApi` will have consistent OpenAPI documentation
2. **No Duplication**: No need to redeclare common CRUD endpoints in each controller
3. **Type Safety**: Generic type parameters ensure type safety across the API
4. **Maintainability**: Changes to CRUD operations only need to be made in one place
5. **Flexibility**: Controllers can still add custom endpoints while inheriting standard CRUD operations

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
- `page` - Page number for pagination
- `size` - Page size for pagination
- Additional custom filters can be added as query parameters

## Migration

The `BaseController` now implements the `CrudApi` interface, so all existing controllers automatically get the benefits of proper OpenAPI documentation without any changes needed.

## Simplified Generic Types

The architecture has been simplified to use only 3 generic type parameters instead of 5:

- **Before**: `<T, I, C, U, R>` (Entity, ID, CreateDTO, UpdateDTO, ResponseDTO)
- **After**: `<T, I, D>` (Entity, ID, DTO)

This simplification is possible because most controllers use the same DTO type for create, update, and response operations. This reduces complexity and makes the code easier to maintain.

## Example

```java
// All controllers now automatically get proper OpenAPI documentation
public class UserController extends BaseController<User, Long, UserDTO, UserDTO, UserDTO> {
    // CRUD endpoints are available AND properly documented in OpenAPI
}
```

## Security Considerations

The JWT filter has been configured to skip Swagger UI and API documentation endpoints:

- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/webjars/**`
- `/auth/**`
- `/public/**`
- `/actuator/**`

This ensures that API documentation is accessible without authentication while maintaining security for actual API endpoints. 