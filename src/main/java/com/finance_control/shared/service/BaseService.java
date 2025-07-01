package com.finance_control.shared.service;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.util.ValidationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base service providing common CRUD operations implementation.
 * This class is designed to be generic and reusable across different domains.
 * Extend this class to avoid code duplication in service implementations.
 * 
 * @param <T> The entity type managed by this service
 * @param <I> The ID type of the entity (typically Long)
 * @param <D> The DTO type used for all operations (create, update, response)
 */
public abstract class BaseService<T, I, D> {
    
    /** The repository for data access operations */
    protected final BaseRepository<T, I> repository;
    
    /**
     * Constructs a new BaseService with the specified repository.
     * 
     * @param repository the repository to use for data access operations
     */
    protected BaseService(BaseRepository<T, I> repository) {
        this.repository = repository;
    }
    
    /**
     * Find all entities with optional filtering, searching, and pagination support.
     * This method provides a unified interface for all entity queries.
     * 
     * @param search optional search term to filter entities (searches across searchable fields)
     * @param filters optional map of field-specific filters
     * @param sortBy optional field name to sort by
     * @param sortDirection optional sort direction ("asc" or "desc"), defaults to "asc"
     * @param pageable pagination parameters
     * @return a page of response DTOs
     */
    public Page<D> findAll(String search, java.util.Map<String, Object> filters, String sortBy, String sortDirection, Pageable pageable) {
        Pageable finalPageable = createPageableWithSort(pageable, sortBy, sortDirection);
        
        // Use the repository's findAll method with search if no specific filters are provided
        if (filters == null || filters.isEmpty()) {
            Page<T> entities = repository.findAll(search, finalPageable);
            return entities.map(this::mapToResponseDTO);
        }
        
        // If specific filters are provided, use specifications
        org.springframework.data.jpa.domain.Specification<T> spec = createSpecificationFromFilters(search, filters);
        Page<T> entities = repository.findAll(spec, finalPageable);
        return entities.map(this::mapToResponseDTO);
    }
    
    /**
     * Legacy method for backward compatibility.
     */
    public Page<D> findAll(String search, String sortBy, String sortDirection, Pageable pageable) {
        return findAll(search, null, sortBy, sortDirection, pageable);
    }
    
    /**
     * Find entity by ID.
     * 
     * @param id the ID of the entity to find
     * @return an Optional containing the response DTO if found, empty otherwise
     */
    public Optional<D> findById(I id) {
        validateId(id);
        return repository.findById(id)
                .map(this::mapToResponseDTO);
    }
    
    /**
     * Create a new entity.
     * 
     * @param createDTO the DTO containing data for the new entity
     * @return the created entity as a response DTO
     */
    public D create(D createDTO) {
        validateCreateDTO(createDTO);
        T entity = mapToEntity(createDTO);
        validateEntity(entity);
        T savedEntity = repository.save(entity);
        return mapToResponseDTO(savedEntity);
    }
    
    /**
     * Update an existing entity.
     * 
     * @param id the ID of the entity to update
     * @param updateDTO the DTO containing updated data
     * @return the updated entity as a response DTO
     * @throws EntityNotFoundException if the entity with the given ID is not found
     */
    public D update(I id, D updateDTO) {
        validateId(id);
        validateUpdateDTO(updateDTO);
        
        T entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityName(), "id", id));
        
        updateEntityFromDTO(entity, updateDTO);
        validateEntity(entity);
        T savedEntity = repository.save(entity);
        return mapToResponseDTO(savedEntity);
    }
    
    /**
     * Delete an entity by ID.
     * 
     * @param id the ID of the entity to delete
     * @throws EntityNotFoundException if the entity with the given ID is not found
     */
    public void delete(I id) {
        validateId(id);
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException(getEntityName(), "id", id);
        }
        repository.deleteById(id);
    }
    
    /**
     * Check if entity exists by ID.
     * 
     * @param id the ID to check
     * @return true if entity exists, false otherwise
     */
    public boolean existsById(I id) {
        validateId(id);
        return repository.existsById(id);
    }
    
    /**
     * Count entities with optional filtering and searching.
     * 
     * @param search optional search term to filter entities
     * @param filters optional map of field-specific filters
     * @return the count of entities matching the criteria
     */
    public long count(String search, java.util.Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            // For search-only counting, we need to use specifications
            org.springframework.data.jpa.domain.Specification<T> spec = createSpecificationFromFilters(search, null);
            return repository.count(spec);
        }
        
        org.springframework.data.jpa.domain.Specification<T> spec = createSpecificationFromFilters(search, filters);
        return repository.count(spec);
    }
    
    /**
     * Find all entities without pagination.
     * 
     * @param search optional search term to filter entities
     * @param filters optional map of field-specific filters
     * @param sortBy optional field name to sort by
     * @param sortDirection optional sort direction ("asc" or "desc"), defaults to "asc"
     * @return a list of response DTOs
     */
    public List<D> findAll(String search, java.util.Map<String, Object> filters, String sortBy, String sortDirection) {
        Pageable pageable = Pageable.unpaged();
        return findAll(search, filters, sortBy, sortDirection, pageable).getContent();
    }
    
    /**
     * Legacy method for backward compatibility.
     */
    public List<D> findAll(String search, String sortBy, String sortDirection) {
        return findAll(search, null, sortBy, sortDirection);
    }
    
    /**
     * Find entity by name (if the entity has a name field and repository supports it).
     * 
     * @param name the name to search for
     * @return an Optional containing the response DTO if found, empty otherwise
     */
    public Optional<D> findByName(String name) {
        ValidationUtils.validateString(name, "Name");
        try {
            Method findByNameMethod = repository.getClass().getMethod("findByNameIgnoreCase", String.class);
            @SuppressWarnings("unchecked")
            Optional<T> entity = (Optional<T>) findByNameMethod.invoke(repository, name);
            return entity.map(this::mapToResponseDTO);
        } catch (NoSuchMethodException _) {
            throw new UnsupportedOperationException("Repository does not support findByNameIgnoreCase method");
        } catch (Exception e) {
            throw new RuntimeException("Error calling findByNameIgnoreCase method", e);
        }
    }
    
    /**
     * Check if entity exists by name (if the entity has a name field and repository supports it).
     * 
     * @param name the name to check
     * @return true if entity exists, false otherwise
     */
    public boolean existsByName(String name) {
        ValidationUtils.validateString(name, "Name");
        try {
            Method existsByNameMethod = repository.getClass().getMethod("existsByNameIgnoreCase", String.class);
            return (Boolean) existsByNameMethod.invoke(repository, name);
        } catch (NoSuchMethodException _) {
            throw new UnsupportedOperationException("Repository does not support existsByNameIgnoreCase method");
        } catch (Exception e) {
            throw new RuntimeException("Error calling existsByNameIgnoreCase method", e);
        }
    }
    
    /**
     * Find all entities ordered by name (if the entity has a name field and repository supports it).
     * 
     * @return a list of response DTOs ordered by name
     */
    public List<D> findAllOrderedByName() {
        try {
            Method findAllOrderedByNameMethod = repository.getClass().getMethod("findAllByOrderByNameAsc");
            @SuppressWarnings("unchecked")
            List<T> entities = (List<T>) findAllOrderedByNameMethod.invoke(repository);
            return entities.stream().map(this::mapToResponseDTO).toList();
        } catch (NoSuchMethodException _) {
            throw new UnsupportedOperationException("Repository does not support findAllByOrderByNameAsc method");
        } catch (Exception e) {
            throw new RuntimeException("Error calling findAllByOrderByNameAsc method", e);
        }
    }
    
    /**
     * Get entity by ID for internal use (returns entity, not DTO).
     * 
     * @param id the ID of the entity to find
     * @return the entity
     * @throws EntityNotFoundException if the entity is not found
     */
    protected T getEntityById(I id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityName(), "id", id));
    }
    
    /**
     * Maps a DTO to an entity.
     * 
     * @param dto the DTO to map
     * @return the mapped entity
     */
    protected abstract T mapToEntity(D dto);
    
    /**
     * Updates an entity with data from a DTO.
     * 
     * @param entity the entity to update
     * @param dto the DTO containing update data
     */
    protected abstract void updateEntityFromDTO(T entity, D dto);
    
    /**
     * Maps an entity to a response DTO.
     * 
     * @param entity the entity to map
     * @return the mapped response DTO
     */
    protected abstract D mapToResponseDTO(T entity);
    
    /**
     * Validates a DTO before entity creation.
     * Default implementation validates common fields like name.
     * Override this method to add custom validation logic.
     * 
     * @param dto the DTO to validate
     */
    protected void validateCreateDTO(D dto) {
        // Default implementation - override if needed
    }
    
    /**
     * Validates a DTO before entity update.
     * Default implementation validates common fields like name.
     * Override this method to add custom validation logic.
     * 
     * @param dto the DTO to validate
     */
    protected void validateUpdateDTO(D dto) {
        // Default implementation - override if needed
    }
    
    /**
     * Validates an entity before saving.
     * Default implementation does nothing.
     * Override this method to add custom validation logic.
     * 
     * @param entity the entity to validate
     */
    protected void validateEntity(T entity) {
        // Default implementation - override if needed
    }
    
    /**
     * Returns the entity name for error messages.
     * Override this method to provide a more specific entity name.
     * 
     * @return the entity name
     */
    protected String getEntityName() {
        return "Entity";
    }
    
    /**
     * Validates an ID value.
     * 
     * @param id the ID to validate
     * @throws IllegalArgumentException if the ID is null or invalid
     */
    protected void validateId(I id) {
        if (id instanceof Long longId) {
            ValidationUtils.validateId(longId);
        } else if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
    }
    
    /**
     * Creates a specification from search term and filters.
     * This method should be overridden by subclasses to provide entity-specific filtering logic.
     * 
     * @param search optional search term
     * @param filters optional map of field-specific filters
     * @return a specification for filtering entities
     */
    protected org.springframework.data.jpa.domain.Specification<T> createSpecificationFromFilters(String search, java.util.Map<String, Object> filters) {
        return org.springframework.data.jpa.domain.Specification.where(null);
    }
    
    /**
     * Creates a Pageable object that combines the existing pageable with custom sorting parameters.
     * If sortBy is provided, it overrides any existing sort in the pageable.
     * 
     * @param pageable the original pageable object
     * @param sortBy optional field name to sort by
     * @param sortDirection optional sort direction ("asc" or "desc")
     * @return a new Pageable with custom sorting applied
     */
    private Pageable createPageableWithSort(Pageable pageable, String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return pageable;
        }
        
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
} 