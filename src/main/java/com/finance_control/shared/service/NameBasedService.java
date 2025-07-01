package com.finance_control.shared.service;

import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.util.ValidationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base service for entities that have a name field.
 * This service provides common operations for name-based entities like categories, responsibles, etc.
 * Uses reflection to reduce boilerplate code in subclasses.
 * 
 * @param <T> The entity type managed by this service
 * @param <I> The ID type of the entity (typically Long)
 * @param <C> The DTO type used for creating new entities
 * @param <U> The DTO type used for updating existing entities
 * @param <R> The DTO type returned in responses
 */
public abstract class NameBasedService<T, I, D> extends BaseService<T, I, D> {
    
    /**
     * Constructs a new NameBasedService with the specified repository.
     * 
     * @param repository the repository to use for data access operations
     */
    protected NameBasedService(BaseRepository<T, I> repository) {
        super(repository);
    }
    
    /**
     * Find entity by name.
     * 
     * @param name the name to search for
     * @return an Optional containing the response DTO if found, empty otherwise
     */
    public Optional<D> findByName(String name) {
        ValidationUtils.validateString(name, "Name");
        return getRepository().findByNameIgnoreCase(name)
                .map(this::mapToResponseDTO);
    }
    
    /**
     * Check if entity exists by name.
     * 
     * @param name the name to check
     * @return true if entity exists, false otherwise
     */
    public boolean existsByName(String name) {
        ValidationUtils.validateString(name, "Name");
        return getRepository().existsByNameIgnoreCase(name);
    }
    
    /**
     * Find all entities ordered by name.
     * 
     * @return a list of response DTOs ordered by name
     */
    public List<D> findAllOrderedByName() {
        return getRepository().findAllByOrderByNameAsc()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }
    
    /**
     * Get the repository with name-based operations.
     * 
     * @return the repository
     */
    protected abstract NameBasedRepository<T, I> getRepository();
    
    /**
     * Validates that the name is unique before creating an entity.
     * 
     * @param name the name to validate
     * @throws IllegalArgumentException if the name already exists
     */
    protected void validateNameUnique(String name) {
        if (getRepository().existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(getEntityName() + " with this name already exists");
        }
    }
    
    /**
     * Validates that the name is unique before updating an entity.
     * 
     * @param name the name to validate
     * @param currentName the current name of the entity being updated
     * @throws IllegalArgumentException if the name already exists
     */
    protected void validateNameUniqueForUpdate(String name, String currentName) {
        if (!name.equalsIgnoreCase(currentName) && getRepository().existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(getEntityName() + " with this name already exists");
        }
    }
    
    /**
     * Validates a DTO before entity creation.
     * Default implementation validates the name field.
     * 
     * @param dto the DTO to validate
     */
    @Override
    protected void validateCreateDTO(D dto) {
        String name = getNameFromDTO(dto);
        ValidationUtils.validateString(name, "Name");
        validateNameUnique(name);
    }
    
    /**
     * Validates a DTO before entity update.
     * Default implementation validates the name field.
     * 
     * @param dto the DTO to validate
     */
    @Override
    protected void validateUpdateDTO(D dto) {
        String name = getNameFromDTO(dto);
        ValidationUtils.validateString(name, "Name");
    }
    
    /**
     * Maps a DTO to an entity.
     * Default implementation creates entity and sets the name.
     * 
     * @param dto the DTO to map
     * @return the mapped entity
     */
    @Override
    protected T mapToEntity(D dto) {
        T entity = createEntityInstance();
        String name = getNameFromDTO(dto);
        setNameOnEntity(entity, name);
        return entity;
    }
    
    /**
     * Updates an entity with data from a DTO.
     * Default implementation updates the name field.
     * 
     * @param entity the entity to update
     * @param dto the DTO containing update data
     */
    @Override
    protected void updateEntityFromDTO(T entity, D dto) {
        String name = getNameFromDTO(dto);
        String currentName = getNameFromEntity(entity);
        validateNameUniqueForUpdate(name, currentName);
        setNameOnEntity(entity, name);
    }
    
    /**
     * Maps an entity to a response DTO.
     * Default implementation maps common fields including name.
     * 
     * @param entity the entity to map
     * @return the mapped response DTO
     */
    @Override
    protected D mapToResponseDTO(T entity) {
        D dto = createResponseDTOInstance();
        setIdOnDTO(dto, getIdFromEntity(entity));
        setNameOnDTO(dto, getNameFromEntity(entity));
        setAuditFieldsOnDTO(dto, entity);
        return dto;
    }
    
    // Reflection-based implementations for common field mappings
    
    /**
     * Gets the name from a DTO using reflection.
     * Subclasses can override this if they need custom logic.
     * 
     * @param dto the DTO
     * @return the name
     */
    protected String getNameFromDTO(Object dto) {
        return getFieldValue(dto, "getName", String.class);
    }
    
    /**
     * Gets the name from an entity using reflection.
     * Subclasses can override this if they need custom logic.
     * 
     * @param entity the entity
     * @return the name
     */
    protected String getNameFromEntity(T entity) {
        return getFieldValue(entity, "getName", String.class);
    }
    
    /**
     * Sets the name on an entity using reflection.
     * Subclasses can override this if they need custom logic.
     * 
     * @param entity the entity
     * @param name the name to set
     */
    protected void setNameOnEntity(T entity, String name) {
        setFieldValue(entity, "setName", name);
    }
    
    /**
     * Sets the name on a DTO using reflection.
     * Subclasses can override this if they need custom logic.
     * 
     * @param dto the DTO
     * @param name the name to set
     */
    protected void setNameOnDTO(D dto, String name) {
        setFieldValue(dto, "setName", name);
    }
    
    /**
     * Gets the ID from an entity using reflection.
     * Subclasses can override this if they need custom logic.
     * 
     * @param entity the entity
     * @return the ID
     */
    protected I getIdFromEntity(T entity) {
        return getFieldValue(entity, "getId", (Class<I>) Long.class);
    }
    
    /**
     * Sets the ID on a DTO using reflection.
     * Subclasses can override this if they need custom logic.
     * 
     * @param dto the DTO
     * @param id the ID to set
     */
    protected void setIdOnDTO(D dto, I id) {
        setFieldValue(dto, "setId", id);
    }
    
    /**
     * Sets audit fields (createdAt, updatedAt) on a DTO using reflection.
     * Subclasses can override this if they need custom logic.
     * 
     * @param dto the DTO
     * @param entity the entity
     */
    protected void setAuditFieldsOnDTO(D dto, T entity) {
        try {
            // Set createdAt if both entity and DTO have the field
            Object createdAt = getFieldValue(entity, "getCreatedAt", Object.class);
            if (createdAt != null) {
                setFieldValue(dto, "setCreatedAt", createdAt);
            }
            
            // Set updatedAt if both entity and DTO have the field
            Object updatedAt = getFieldValue(entity, "getUpdatedAt", Object.class);
            if (updatedAt != null) {
                setFieldValue(dto, "setUpdatedAt", updatedAt);
            }
        } catch (Exception e) {
            // If audit fields don't exist, ignore the error
        }
    }
    
    /**
     * Generic method to get a field value using reflection.
     * 
     * @param obj the object to get the field from
     * @param methodName the getter method name
     * @param returnType the expected return type
     * @return the field value
     */
    @SuppressWarnings("unchecked")
    protected <V> V getFieldValue(Object obj, String methodName, Class<V> returnType) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            return (V) method.invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value using " + methodName, e);
        }
    }
    
    /**
     * Generic method to set a field value using reflection.
     * 
     * @param obj the object to set the field on
     * @param methodName the setter method name
     * @param value the value to set
     */
    protected <V> void setFieldValue(Object obj, String methodName, V value) {
        try {
            Method method = obj.getClass().getMethod(methodName, value.getClass());
            method.invoke(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value using " + methodName, e);
        }
    }
    
    // Abstract methods that subclasses must implement
    
    /**
     * Creates a new instance of the entity.
     * 
     * @return a new entity instance
     */
    protected abstract T createEntityInstance();
    
    /**
     * Creates a new instance of the response DTO.
     * 
     * @return a new response DTO instance
     */
    protected abstract D createResponseDTOInstance();
    
    /**
     * Repository interface for name-based operations.
     * 
     * @param <T> The entity type
     * @param <I> The ID type
     */
    public interface NameBasedRepository<T, I> extends BaseRepository<T, I> {
        
        /**
         * Find entity by name (case-insensitive).
         * 
         * @param name the name to search for
         * @return an Optional containing the entity if found, empty otherwise
         */
        Optional<T> findByNameIgnoreCase(String name);
        
        /**
         * Check if entity exists by name (case-insensitive).
         * 
         * @param name the name to check
         * @return true if entity exists, false otherwise
         */
        boolean existsByNameIgnoreCase(String name);
        
        /**
         * Find all entities ordered by name (ascending).
         * 
         * @return a list of entities ordered by name
         */
        List<T> findAllByOrderByNameAsc();
    }
} 