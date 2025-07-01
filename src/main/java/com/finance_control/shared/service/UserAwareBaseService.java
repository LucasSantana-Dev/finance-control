package com.finance_control.shared.service;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;
import java.util.Optional;

/**
 * Base service that automatically filters entities by the current user's ID.
 * This ensures that users can only access their own data.
 * 
 * @param <T> The entity type managed by this service
 * @param <I> The ID type of the entity (typically Long)
 * @param <C> The DTO type used for creating new entities
 * @param <U> The DTO type used for updating existing entities
 * @param <R> The DTO type returned in responses
 */
public abstract class UserAwareBaseService<T, I, D> extends BaseService<T, I, D> {
    
    protected UserAwareBaseService(BaseRepository<T, I> repository) {
        super(repository);
    }
    
    @Override
    public Page<D> findAll(String search, Map<String, Object> filters, String sortBy, String sortDirection, Pageable pageable) {
        // Add user filter to ensure users only see their own data
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User context not available");
        }
        
        // Add userId to filters if not already present
        if (filters != null) {
            filters.put("userId", currentUserId);
        } else {
            filters = Map.of("userId", currentUserId);
        }
        
        return super.findAll(search, filters, sortBy, sortDirection, pageable);
    }
    
    @Override
    public Optional<D> findById(I id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User context not available");
        }
        
        // Get the entity and verify it belongs to the current user
        Optional<T> entity = repository.findById(id);
        if (entity.isPresent() && !belongsToUser(entity.get(), currentUserId)) {
            throw new SecurityException("Access denied: entity does not belong to current user");
        }
        
        return entity.map(this::mapToResponseDTO);
    }
    
    @Override
    public D create(D createDTO) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User context not available");
        }
        
        T entity = mapToEntity(createDTO);
        setUserId(entity, currentUserId);
        validateEntity(entity);
        T savedEntity = repository.save(entity);
        return mapToResponseDTO(savedEntity);
    }
    
    @Override
    public D update(I id, D updateDTO) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User context not available");
        }
        
        T entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityName(), "id", id));
        
        // Verify the entity belongs to the current user
        if (!belongsToUser(entity, currentUserId)) {
            throw new SecurityException("Access denied: entity does not belong to current user");
        }
        
        updateEntityFromDTO(entity, updateDTO);
        validateEntity(entity);
        T savedEntity = repository.save(entity);
        return mapToResponseDTO(savedEntity);
    }
    
    @Override
    public void delete(I id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User context not available");
        }
        
        T entity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(getEntityName(), "id", id));
        
        // Verify the entity belongs to the current user
        if (!belongsToUser(entity, currentUserId)) {
            throw new SecurityException("Access denied: entity does not belong to current user");
        }
        
        repository.deleteById(id);
    }
    
    @Override
    protected Specification<T> createSpecificationFromFilters(String search, Map<String, Object> filters) {
        // Ensure userId filter is always included
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User context not available");
        }
        
        if (filters != null && !filters.containsKey("userId")) {
            filters.put("userId", currentUserId);
        }
        
        return super.createSpecificationFromFilters(search, filters);
    }
    
    /**
     * Checks if an entity belongs to the specified user.
     * This method must be implemented by subclasses to define how to check user ownership.
     * 
     * @param entity the entity to check
     * @param userId the user ID to check against
     * @return true if the entity belongs to the user, false otherwise
     */
    protected abstract boolean belongsToUser(T entity, Long userId);
    
    /**
     * Sets the user ID on an entity during creation.
     * This method must be implemented by subclasses to define how to set the user ID.
     * 
     * @param entity the entity to set the user ID on
     * @param userId the user ID to set
     */
    protected abstract void setUserId(T entity, Long userId);
} 