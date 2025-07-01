package com.finance_control.shared.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Generic CRUD API interface providing standard REST endpoints for entity management.
 * 
 * <p>This interface defines the contract for basic CRUD operations that can be implemented
 * by any controller managing domain entities. It provides a consistent API structure
 * across all controllers in the application.</p>
 * 
 * <p>The interface uses generic type parameters to ensure type safety:</p>
 * <ul>
 *   <li><strong>I</strong> - The ID type of the entity (e.g., Long, UUID)</li>
 *   <li><strong>D</strong> - The DTO type used for all operations (create, update, response)</li>
 * </ul>
 * 
 * <p>Controllers implementing this interface should also extend a base controller
 * class to inherit common functionality while providing OpenAPI documentation
 * through the interface methods.</p>
 * 
 * @param <I> the type of the entity's ID
 * @param <D> the type of the DTO used for all operations
 * 
 * @see BaseController
 * @since 1.0.0
 */
public interface CrudApi<I, D> {
    
    /**
     * Retrieves a paginated list of entities with optional search and sorting capabilities.
     * 
     * <p>This endpoint supports:</p>
     * <ul>
     *   <li>Pagination through Spring's Pageable interface</li>
     *   <li>Search functionality across entity fields</li>
     *   <li>Dynamic sorting by any entity field</li>
     *   <li>Sort direction control (ascending/descending)</li>
     * </ul>
     * 
     * @param search optional search term to filter entities
     * @param sortBy optional field name to sort by
     * @param sortDirection sort direction, defaults to "asc"
     * @param pageable pagination and sorting parameters
     * @param request the HTTP request object for context
     * @return a paginated response containing the filtered and sorted entities
     */
    @GetMapping
    ResponseEntity<Page<D>> findAll(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false, defaultValue = "asc") String sortDirection,
        Pageable pageable,
        HttpServletRequest request
    );

    /**
     * Retrieves a single entity by its unique identifier.
     * 
     * @param id the unique identifier of the entity to retrieve
     * @return the entity if found, or appropriate error response if not found
     * @throws EntityNotFoundException if the entity with the given ID does not exist
     */
    @GetMapping("/{id}")
    ResponseEntity<D> findById(@PathVariable I id);

    /**
     * Creates a new entity from the provided data.
     * 
     * <p>The request body is validated using Bean Validation annotations
     * before processing. Any validation errors will result in a 400 Bad Request
     * response with detailed error information.</p>
     * 
     * @param createDTO the data transfer object containing entity creation data
     * @return the created entity with generated ID and audit fields
     * @throws ValidationException if the create data fails validation
     */
    @PostMapping
    ResponseEntity<D> create(@Valid @RequestBody D createDTO);

    /**
     * Updates an existing entity with the provided data.
     * 
     * <p>The request body is validated using Bean Validation annotations
     * before processing. The entity must exist for the update to succeed.</p>
     * 
     * @param id the unique identifier of the entity to update
     * @param updateDTO the data transfer object containing entity update data
     * @return the updated entity
     * @throws EntityNotFoundException if the entity with the given ID does not exist
     * @throws ValidationException if the update data fails validation
     */
    @PutMapping("/{id}")
    ResponseEntity<D> update(@PathVariable I id, @Valid @RequestBody D updateDTO);

    /**
     * Deletes an entity by its unique identifier.
     * 
     * <p>This operation is typically soft-delete, marking the entity as inactive
     * rather than physically removing it from the database.</p>
     * 
     * @param id the unique identifier of the entity to delete
     * @return no content response (204) on successful deletion
     * @throws EntityNotFoundException if the entity with the given ID does not exist
     */
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable I id);
} 