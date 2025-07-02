package com.finance_control.shared.controller;

import com.finance_control.shared.service.BaseService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base controller providing default implementations for common REST
 * operations with OpenAPI documentation.
 * 
 * This class implements the CrudApi interface to provide consistent API documentation
 * while offering concrete implementations of all CRUD operations. Controllers extending
 * this class automatically get both functionality and proper Swagger documentation.
 * 
 * @param <T> The entity type managed by this controller
 * @param <I> The ID type of the entity (typically Long)
 * @param <D> The DTO type used for all operations (create, update, response)
 */
@Slf4j
public abstract class BaseController<T, I, D> implements CrudApi<I, D> {

    /** The service for business logic operations */
    protected final BaseService<T, I, D> service;

    /**
     * Constructs a new BaseController with the specified service.
     * 
     * @param service the service to use for business logic operations
     */
    protected BaseController(BaseService<T, I, D> service) {
        this.service = service;
    }

    /**
     * Retrieves a paginated list of entities with optional search, filtering, and
     * sorting.
     * Supports multiple query parameters for flexible filtering.
     * 
     * @param search        optional search term to filter entities across
     *                      searchable fields
     * @param sortBy        optional field name to sort by
     * @param sortDirection optional sort direction ("asc" or "desc"), defaults to
     *                      "asc"
     * @param pageable      pagination parameters
     * @param request       the HTTP request containing additional filter parameters
     * @return a ResponseEntity containing a page of response DTOs
     */
    @Override
    @GetMapping
    @Operation(summary = "List entities", description = "Retrieve a paginated list of entities with optional search, filtering, and sorting.")
    public ResponseEntity<Page<D>> findAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            Pageable pageable,
            HttpServletRequest request) {

        log.debug("GET request to list entities - search: '{}', sortBy: '{}', sortDirection: '{}', page: {}", 
                 search, sortBy, sortDirection, pageable.getPageNumber());

        // Extract all query parameters as filters (excluding standard ones)
        Map<String, Object> filters = extractFiltersFromRequest(request, search, sortBy, sortDirection);

        Page<D> result = service.findAll(search, filters, sortBy, sortDirection, pageable);
        log.debug("Returning {} entities out of {} total", result.getNumberOfElements(), result.getTotalElements());
        return ResponseEntity.ok(result);
    }

    /**
     * Extracts filter parameters from the HTTP request.
     * Excludes standard parameters like search, sortBy, sortDirection, page, size.
     * 
     * @param request       the HTTP request
     * @param search        the search parameter
     * @param sortBy        the sort by parameter
     * @param sortDirection the sort direction parameter
     * @return a map of filter parameters
     */
    protected Map<String, Object> extractFiltersFromRequest(HttpServletRequest request, String search, String sortBy,
            String sortDirection) {
        Map<String, Object> filters = new HashMap<>();

        // Standard parameters to exclude
        String[] excludeParams = { "search", "sortBy", "sortDirection", "page", "size", "sort" };

        request.getParameterMap().forEach((key, values) -> {
            // Skip excluded parameters
            boolean shouldExclude = false;
            for (String excludeParam : excludeParams) {
                if (key.equals(excludeParam)) {
                    shouldExclude = true;
                    break;
                }
            }

            if (!shouldExclude && values != null && values.length > 0) {
                String value = values[0];
                if (value != null && !value.trim().isEmpty()) {
                    // Try to convert to appropriate type
                    Object convertedValue = convertParameterValue(value);
                    filters.put(key, convertedValue);
                }
            }
        });

        return filters;
    }

    /**
     * Converts string parameter values to appropriate types.
     * 
     * @param value the string value
     * @return the converted value
     */
    protected Object convertParameterValue(String value) {
        // Try to convert to boolean
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
            return Boolean.valueOf(value);
        }

        // Try to convert to Long
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException _) {
            // Not a number, return as string
        }

        // Try to convert to Double
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException _) {
            // Not a number, return as string
        }

        return value;
    }

    /**
     * Retrieves a single entity by its ID.
     * 
     * @param id the ID of the entity to retrieve
     * @return a ResponseEntity containing the response DTO if found, or 404 if not
     *         found
     */
    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID", description = "Retrieve a single entity by its unique identifier.")
    public ResponseEntity<D> findById(@PathVariable I id) {
        log.debug("GET request to find entity by ID: {}", id);
        return service.findById(id)
                .map(entity -> {
                    log.debug("Entity found with ID: {}", id);
                    return ResponseEntity.ok(entity);
                })
                .orElseGet(() -> {
                    log.debug("Entity not found with ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Creates a new entity.
     * 
     * @param createDTO the DTO containing data for the new entity
     * @return a ResponseEntity containing the created entity as a response DTO
     */
    @Override
    @PostMapping
    @Operation(summary = "Create entity", description = "Create a new entity.")
    public ResponseEntity<D> create(@Valid @RequestBody D createDTO) {
        log.debug("POST request to create entity: {}", createDTO);
        D result = service.create(createDTO);
        log.info("Entity created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * Updates an existing entity by its ID.
     * 
     * @param id        the ID of the entity to update
     * @param updateDTO the DTO containing updated data
     * @return a ResponseEntity containing the updated entity as a response DTO
     */
    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Update entity", description = "Update an existing entity by its ID.")
    public ResponseEntity<D> update(@PathVariable I id, @Valid @RequestBody D updateDTO) {
        log.debug("PUT request to update entity with ID: {} and DTO: {}", id, updateDTO);
        D result = service.update(id, updateDTO);
        log.info("Entity updated successfully with ID: {}", id);
        return ResponseEntity.ok(result);
    }

    /**
     * Deletes an entity by its ID.
     * 
     * @param id the ID of the entity to delete
     * @return a ResponseEntity with no content if successful
     */
    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete entity", description = "Delete an entity by its ID.")
    public ResponseEntity<Void> delete(@PathVariable I id) {
        log.debug("DELETE request to delete entity with ID: {}", id);
        service.delete(id);
        log.info("Entity deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}