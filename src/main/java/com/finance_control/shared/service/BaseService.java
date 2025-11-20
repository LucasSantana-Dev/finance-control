package com.finance_control.shared.service;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.model.BaseModel;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.service.helper.BaseServiceNameBasedHelper;
import com.finance_control.shared.service.helper.BaseServicePageableHelper;
import com.finance_control.shared.service.helper.BaseServiceReflectionHelper;
import com.finance_control.shared.service.helper.BaseServiceRepositoryHelper;
import com.finance_control.shared.service.helper.BaseServiceSpecificationBuilder;
import com.finance_control.shared.service.helper.BaseServiceValidationHelper;
import com.finance_control.shared.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base service providing common CRUD operations implementation.
 * This class is designed to be generic and reusable across different domains.
 * Supports user-aware operations and name-based operations through
 * configuration.
 *
 * @param <T> The entity type managed by this service
 * @param <I> The ID type of the entity (typically Long)
 * @param <D> The DTO type used for all operations (create, update, response)
 */
@Slf4j
public abstract class BaseService<T extends BaseModel<I>, I, D> {

    /** The repository for data access operations */
    protected final BaseRepository<T, I> repository;

    /** Helper for building specifications */
    private final BaseServiceSpecificationBuilder<T> specificationBuilder;

    /** Helper for name-based operations */
    private BaseServiceNameBasedHelper<T, I, D> nameBasedHelper;

    /** Helper for reflection utilities */
    private final BaseServiceReflectionHelper reflectionHelper;

    /** Common field names used across entities */
    protected static final String IS_ACTIVE_FIELD = "isActive";
    private static final String USER_ID_FIELD = "userId";

    /** Error message for missing user context */
    private static final String USER_CONTEXT_UNAVAILABLE = "User context not available for user-aware service";
    private static final String ACCESS_DENIED_MSG = "Access denied: entity {} does not belong to user {}";
    private static final String USER_OWNERSHIP_VERIFIED_MSG = "User ownership verified for entity {} and user {}";

    /**
     * Constructs a new BaseService with the specified repository.
     *
     * @param repository the repository to use for data access operations
     */
    protected BaseService(BaseRepository<T, I> repository) {
        this.repository = repository;
        this.specificationBuilder = new BaseServiceSpecificationBuilder<>(isUserAware());
        this.reflectionHelper = new BaseServiceReflectionHelper();
    }

    /**
     * Check if this service should be user-aware (filter by current user).
     * Override to return true for user-scoped entities.
     *
     * @return true if the service should filter by user, false otherwise
     */
    protected boolean isUserAware() {
        return false;
    }

    /**
     * Check if this service should support name-based operations.
     * Override to return true for entities with name fields.
     *
     * @return true if the service should support name operations, false otherwise
     */
    protected boolean isNameBased() {
        return false;
    }

    /**
     * Find all entities with optional filtering, searching, and pagination support.
     * This method provides a unified interface for all entity queries.
     *
     * @param search        optional search term to filter entities (searches across
     *                      searchable fields)
     * @param filters       optional map of field-specific filters
     * @param sortBy        optional field name to sort by
     * @param sortDirection optional sort direction ("asc" or "desc"), defaults to
     *                      "asc"
     * @param pageable      pagination parameters
     * @return a page of response DTOs
     */
    public Page<D> findAll(String search, Map<String, Object> filters, String sortBy, String sortDirection,
            Pageable pageable) {
        log.debug("Finding all entities with search present: {}, filters present: {}, sortBy present: {}, sortDirection present: {}, page: {}",
                search != null && !search.trim().isEmpty(), filters != null && !filters.isEmpty(),
                sortBy != null && !sortBy.trim().isEmpty(), sortDirection != null && !sortDirection.trim().isEmpty(),
                pageable.isUnpaged() ? "unpaged" : pageable.getPageNumber());

        Pageable finalPageable = BaseServicePageableHelper.createPageableWithSort(pageable, sortBy, sortDirection);
        addUserFilterIfNeeded(filters);

        if (hasNoFilters(filters)) {
            return findAllWithSearchOnly(search, finalPageable);
        }

        return findAllWithSpecifications(search, filters, finalPageable);
    }

    /**
     * Adds user filter to filters map if service is user-aware.
     *
     * @param filters the filters map to modify
     */
    private void addUserFilterIfNeeded(Map<String, Object> filters) {
        if (!isUserAware()) {
            return;
        }

        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            log.error(USER_CONTEXT_UNAVAILABLE);
            throw new SecurityException("User context not available");
        }

        log.debug("Adding user filter (user present: {})", currentUserId != null);
        if (filters != null && !filters.containsKey(USER_ID_FIELD)) {
            filters.put(USER_ID_FIELD, currentUserId);
        }
    }

    /**
     * Checks if filters map is null or empty.
     *
     * @param filters the filters map to check
     * @return true if filters is null or empty
     */
    private boolean hasNoFilters(Map<String, Object> filters) {
        return filters == null || filters.isEmpty();
    }

    /**
     * Finds entities using search-only approach (no specific filters).
     *
     * @param search the search term
     * @param pageable the pageable parameters
     * @return a page of response DTOs
     */
    private Page<D> findAllWithSearchOnly(String search, Pageable pageable) {
        log.debug("Using repository findAll with search");
        Page<T> entities = BaseServiceRepositoryHelper.executeFindAllWithSearch(repository, search, pageable, isUserAware());
        log.debug("Found {} entities", entities.getTotalElements());
        return entities.map(this::mapToResponseDTO);
    }


    /**
     * Finds entities using specifications approach.
     *
     * @param search the search term
     * @param filters the filters map
     * @param pageable the pageable parameters
     * @return a page of response DTOs
     */
    private Page<D> findAllWithSpecifications(String search, Map<String, Object> filters, Pageable pageable) {
        log.debug("Using specifications with filters");
        Specification<T> spec = createSpecificationFromFilters(search, filters);
        Page<T> entities = repository.findAll(spec, pageable);
        log.debug("Found {} entities with specifications", entities.getTotalElements());
        return entities.map(this::mapToResponseDTO);
    }

    /**
     * Find entity by ID.
     *
     * @param id the ID of the entity to find
     * @return an Optional containing the response DTO if found, empty otherwise
     */
    public Optional<D> findById(I id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        log.debug("Finding entity by ID (length: {})", String.valueOf(id).length());
        validateId(id);
        Optional<T> entity = repository.findById(id);

        // Check user ownership if user-aware
        if (isUserAware() && entity.isPresent()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                log.error(USER_CONTEXT_UNAVAILABLE);
                throw new SecurityException("User context not available");
            }

            if (!belongsToUser(entity.get(), currentUserId)) {
                log.warn(ACCESS_DENIED_MSG, String.valueOf(id).length(), String.valueOf(currentUserId).length());
                throw new SecurityException("Access denied: entity does not belong to current user");
            }
            log.debug(USER_OWNERSHIP_VERIFIED_MSG, String.valueOf(id).length(), String.valueOf(currentUserId).length());
        }

        if (entity.isPresent()) {
            log.debug("Entity found with ID (length: {})", String.valueOf(id).length());
        } else {
            log.debug("Entity not found with ID (length: {})", String.valueOf(id).length());
        }

        return entity.map(this::mapToResponseDTO);
    }

    /**
     * Create a new entity.
     *
     * @param createDTO the DTO containing data for the new entity
     * @return the created entity as a response DTO
     */
    public D create(D createDTO) {
        log.debug("Creating new entity (DTO present: {})", createDTO != null);
        validateCreateDTO(createDTO);
        T entity = mapToEntity(createDTO);

        // Set user ID if user-aware
        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                log.error(USER_CONTEXT_UNAVAILABLE);
                throw new SecurityException("User context not available");
            }
            log.debug("Setting user ID (length: {}) for new entity", String.valueOf(currentUserId).length());
            setUserId(entity, currentUserId);
        }

        validateEntity(entity);
        log.debug("Saving entity to repository");
        T savedEntity = repository.save(entity);
        log.info("Entity created successfully (ID length: {})", savedEntity.getId() != null ? String.valueOf(savedEntity.getId()).length() : 0);
        return mapToResponseDTO(savedEntity);
    }

    /**
     * Update an existing entity.
     *
     * @param id        the ID of the entity to update
     * @param updateDTO the DTO containing updated data
     * @return the updated entity as a response DTO
     * @throws EntityNotFoundException if the entity with the given ID is not found
     */
    public D update(I id, D updateDTO) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        log.debug("Updating entity (ID length: {}, DTO present: {})", String.valueOf(id).length(), updateDTO != null);
        validateId(id);
        validateUpdateDTO(updateDTO);

        T entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Entity not found for update (ID length: {})", String.valueOf(id).length());
                    return new EntityNotFoundException(getEntityName(), "id", id);
                });

        // Check user ownership if user-aware
        validateUserOwnership(entity, id);

        updateEntityFromDTO(entity, updateDTO);
        validateEntity(entity);
        log.debug("Saving updated entity to repository");
        T savedEntity = repository.save(entity);
        log.info("Entity updated successfully (ID length: {})", String.valueOf(id).length());
        D result = mapToResponseDTO(savedEntity);
        log.debug("Mapped result present: {}", result != null);
        return result;
    }

    /**
     * Delete an entity by ID.
     *
     * @param id the ID of the entity to delete
     * @throws EntityNotFoundException if the entity with the given ID is not found
     */
    public void delete(I id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        log.debug("Deleting entity (ID length: {})", String.valueOf(id).length());
        validateId(id);
        T entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Entity not found for deletion (ID length: {})", id != null ? String.valueOf(id).length() : 0);
                    return new EntityNotFoundException(getEntityName(), "id", id);
                });

        // Check user ownership if user-aware
        validateUserOwnership(entity, id);

        repository.deleteById(id);
        log.info("Entity deleted successfully (ID length: {})", String.valueOf(id).length());
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
     * Find entity by name (if name-based).
     *
     * @param name the name to search for
     * @return an Optional containing the response DTO if found, empty otherwise
     */
    public Optional<D> findByName(String name) {
        if (!isNameBased()) {
            throw new UnsupportedOperationException("This service does not support name-based operations");
        }
        return getNameBasedHelper().findByName(name);
    }

    /**
     * Check if entity exists by name (if name-based).
     *
     * @param name the name to check
     * @return true if entity exists, false otherwise
     */
    public boolean existsByName(String name) {
        if (!isNameBased()) {
            throw new UnsupportedOperationException("This service does not support name-based operations");
        }
        return getNameBasedHelper().existsByName(name);
    }

    /**
     * Find all entities ordered by name (if name-based).
     *
     * @return a list of response DTOs ordered by name
     */
    public List<D> findAllOrderedByName() {
        if (!isNameBased()) {
            throw new UnsupportedOperationException("This service does not support name-based operations");
        }
        return getNameBasedHelper().findAllOrderedByName();
    }

    /**
     * Count entities with optional filtering and searching.
     *
     * @param search  optional search term to filter entities
     * @param filters optional map of field-specific filters
     * @return the count of entities matching the criteria
     */
    public long count(String search, Map<String, Object> filters) {
        // createSpecificationFromFilters already handles user filter
        Specification<T> spec = createSpecificationFromFilters(search, filters);
        return repository.count(spec);
    }

    /**
     * Find all entities without pagination.
     *
     * @param search        optional search term to filter entities
     * @param filters       optional map of field-specific filters
     * @param sortBy        optional field name to sort by
     * @param sortDirection optional sort direction ("asc" or "desc"), defaults to
     *                      "asc"
     * @return a list of response DTOs
     */
    public List<D> findAll(String search, Map<String, Object> filters, String sortBy, String sortDirection) {
        Pageable pageable = Pageable.unpaged();
        return findAll(search, filters, sortBy, sortDirection, pageable).getContent();
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
     * @param dto    the DTO containing update data
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
     * Default implementation validates name if name-based.
     * Override this method to add custom validation logic.
     *
     * @param dto the DTO to validate
     */
    protected void validateCreateDTO(D dto) {
        if (isNameBased()) {
            String name = getNameFromDTO(dto);
            ValidationUtils.validateString(name, "Name");
            validateNameUnique(name);
        }
    }

    /**
     * Validates a DTO before entity update.
     * Default implementation validates name if name-based.
     * Override this method to add custom validation logic.
     *
     * @param dto the DTO to validate
     */
    protected void validateUpdateDTO(D dto) {
        if (isNameBased()) {
            String name = getNameFromDTO(dto);
            ValidationUtils.validateString(name, "Name");
            validateNameUnique(name);
        }
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
     * @throws IllegalArgumentException if the ID is invalid
     */
    protected void validateId(I id) {
        BaseServiceValidationHelper.validateId(id);
    }

    /**
     * Creates a specification from search term and filters.
     * Override this method to provide custom specification logic.
     *
     * @param search  the search term
     * @param filters the filters map
     * @return the specification
     */
    protected Specification<T> createSpecificationFromFilters(String search, Map<String, Object> filters) {
        return specificationBuilder.createSpecificationFromFilters(search, filters);
    }

    /**
     * Validates that the entity belongs to the current user.
     * Only performs validation if the service is user-aware.
     *
     * @param entity the entity to validate
     * @param id the entity ID for logging purposes
     * @throws SecurityException if user context is not available or entity doesn't belong to user
     */
    private void validateUserOwnership(T entity, I id) {
        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                log.error(USER_CONTEXT_UNAVAILABLE);
                throw new SecurityException("User context not available");
            }

            if (!belongsToUser(entity, currentUserId)) {
                log.warn(ACCESS_DENIED_MSG, String.valueOf(id).length(), String.valueOf(currentUserId).length());
                throw new SecurityException("Access denied: entity does not belong to current user");
            }
            log.debug(USER_OWNERSHIP_VERIFIED_MSG, String.valueOf(id).length(), String.valueOf(currentUserId).length());
        }
    }



    // Name-based operations (only used if isNameBased() returns true)

    /**
     * Validates that the name is unique before creating an entity.
     *
     * @param name the name to validate
     * @throws IllegalArgumentException if the name already exists
     */
    protected void validateNameUnique(String name) {
        if (existsByName(name)) {
            throw new IllegalArgumentException(getEntityName() + " with this name already exists");
        }
    }

    /**
     * Validates that the name is unique before updating an entity.
     *
     * @param name        the name to validate
     * @param currentName the current name of the entity being updated
     * @throws IllegalArgumentException if the name already exists
     */
    protected void validateNameUniqueForUpdate(String name, String currentName) {
        if (!name.equalsIgnoreCase(currentName) && existsByName(name)) {
            throw new IllegalArgumentException(getEntityName() + " with this name already exists");
        }
    }

    /**
     * Gets the name from a DTO using reflection.
     * Subclasses can override this if they need custom logic.
     *
     * @param dto the DTO
     * @return the name
     */
    protected String getNameFromDTO(Object dto) {
        return reflectionHelper.getNameFromDTO(dto);
    }

    /**
     * Generic method to get a field value using reflection.
     *
     * @param obj        the object to get the field from
     * @param methodName the getter method name
     * @param returnType the expected return type
     * @return the field value
     */
    protected <V> V getFieldValue(Object obj, String methodName, Class<V> returnType) {
        return reflectionHelper.getFieldValue(obj, methodName, returnType);
    }

    // User-aware operations (only used if isUserAware() returns true)

    /**
     * Checks if an entity belongs to the specified user.
     * This method must be implemented by subclasses if isUserAware() returns true.
     *
     * @param entity the entity to check
     * @param userId the user ID to check against
     * @return true if the entity belongs to the user, false otherwise
     */
    protected boolean belongsToUser(T entity, Long userId) {
        throw new UnsupportedOperationException("User ownership check not implemented");
    }

    /**
     * Sets the user ID on an entity during creation.
     * This method must be implemented by subclasses if isUserAware() returns true.
     *
     * @param entity the entity to set the user ID on
     * @param userId the user ID to set
     */
    protected void setUserId(T entity, Long userId) {
        throw new UnsupportedOperationException("User ID setting not implemented");
    }

    /**
     * Gets or creates the name-based helper.
     *
     * @return the name-based helper
     */
    private BaseServiceNameBasedHelper<T, I, D> getNameBasedHelper() {
        if (nameBasedHelper == null) {
            nameBasedHelper = new BaseServiceNameBasedHelper<>(repository, isUserAware(), this::mapToResponseDTO);
        }
        return nameBasedHelper;
    }

}
