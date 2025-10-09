package com.finance_control.shared.service;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.exception.ReflectionException;
import com.finance_control.shared.model.BaseModel;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.util.ValidationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

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

    /** Common field names used across entities */
    protected static final String IS_ACTIVE_FIELD = "isActive";

    /** Error message for missing user context */
    private static final String USER_CONTEXT_UNAVAILABLE = "User context not available for user-aware service";
    private static final String USER_ID_FIELD = "userId";
    private static final String ACCESS_DENIED_MSG = "Access denied: entity {} does not belong to user {}";
    private static final String USER_OWNERSHIP_VERIFIED_MSG = "User ownership verified for entity {} and user {}";

    /**
     * Constructs a new BaseService with the specified repository.
     *
     * @param repository the repository to use for data access operations
     */
    protected BaseService(BaseRepository<T, I> repository) {
        this.repository = repository;
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
        log.debug("Finding all entities with search: '{}', filters: {}, sortBy: '{}', sortDirection: '{}', page: {}",
                search, filters, sortBy, sortDirection,
                pageable.isUnpaged() ? "unpaged" : pageable.getPageNumber());

        Pageable finalPageable = createPageableWithSort(pageable, sortBy, sortDirection);
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

        log.debug("Adding user filter for user ID: {}", currentUserId);
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
        Page<T> entities = executeFindAllWithSearch(search, pageable);
        log.debug("Found {} entities", entities.getTotalElements());
        return entities.map(this::mapToResponseDTO);
    }

    /**
     * Executes the findAll method with search, handling user-aware repositories.
     *
     * @param search the search term
     * @param pageable the pageable parameters
     * @return a page of entities
     */
    @SuppressWarnings("unchecked")
    private Page<T> executeFindAllWithSearch(String search, Pageable pageable) {
        if (!isUserAware()) {
            return repository.findAll(search, pageable);
        }

        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            log.error(USER_CONTEXT_UNAVAILABLE);
            throw new SecurityException("User context not available");
        }

        try {
            return (Page<T>) repository.getClass()
                    .getMethod("findAll", String.class, Long.class, Pageable.class)
                    .invoke(repository, search, currentUserId, pageable);
        } catch (NoSuchMethodException e) {
            return repository.findAll(search, pageable);
        } catch (Exception e) {
            log.warn("Error calling findAll with userId, falling back to standard method: {}", e.getMessage());
            return repository.findAll(search, pageable);
        }
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
        log.debug("Finding entity by ID: {}", id);
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
                log.warn(ACCESS_DENIED_MSG, id, currentUserId);
                throw new SecurityException("Access denied: entity does not belong to current user");
            }
            log.debug(USER_OWNERSHIP_VERIFIED_MSG, id, currentUserId);
        }

        if (entity.isPresent()) {
            log.debug("Entity found with ID: {}", id);
        } else {
            log.debug("Entity not found with ID: {}", id);
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
        log.debug("Creating new entity with DTO: {}", createDTO);
        validateCreateDTO(createDTO);
        T entity = mapToEntity(createDTO);

        // Set user ID if user-aware
        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                log.error(USER_CONTEXT_UNAVAILABLE);
                throw new SecurityException("User context not available");
            }
            log.debug("Setting user ID {} for new entity", currentUserId);
            setUserId(entity, currentUserId);
        }

        validateEntity(entity);
        log.debug("Saving entity to repository");
        T savedEntity = repository.save(entity);
        log.info("Entity created successfully with ID: {}", savedEntity.getId());
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
        log.debug("Updating entity with ID: {} and DTO: {}", id, updateDTO);
        validateId(id);
        validateUpdateDTO(updateDTO);

        T entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Entity not found for update with ID: {}", id);
                    return new EntityNotFoundException(getEntityName(), "id", id);
                });

        // Check user ownership if user-aware
        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                log.error(USER_CONTEXT_UNAVAILABLE);
                throw new SecurityException("User context not available");
            }

            if (!belongsToUser(entity, currentUserId)) {
                log.warn(ACCESS_DENIED_MSG, id, currentUserId);
                throw new SecurityException("Access denied: entity does not belong to current user");
            }
            log.debug(USER_OWNERSHIP_VERIFIED_MSG, id, currentUserId);
        }

        updateEntityFromDTO(entity, updateDTO);
        validateEntity(entity);
        log.debug("Saving updated entity to repository");
        T savedEntity = repository.save(entity);
        log.info("Entity updated successfully with ID: {}", id);
        D result = mapToResponseDTO(savedEntity);
        log.debug("Mapped result: {}", result);
        return result;
    }

    /**
     * Delete an entity by ID.
     *
     * @param id the ID of the entity to delete
     * @throws EntityNotFoundException if the entity with the given ID is not found
     */
    public void delete(I id) {
        log.debug("Deleting entity with ID: {}", id);
        validateId(id);
        T entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Entity not found for deletion with ID: {}", id);
                    return new EntityNotFoundException(getEntityName(), "id", id);
                });

        // Check user ownership if user-aware
        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                log.error(USER_CONTEXT_UNAVAILABLE);
                throw new SecurityException("User context not available");
            }

            if (!belongsToUser(entity, currentUserId)) {
                log.warn(ACCESS_DENIED_MSG, id, currentUserId);
                throw new SecurityException("Access denied: entity does not belong to current user");
            }
            log.debug(USER_OWNERSHIP_VERIFIED_MSG, id, currentUserId);
        }

        repository.deleteById(id);
        log.info("Entity deleted successfully with ID: {}", id);
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

        ValidationUtils.validateString(name, "Name");

        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new SecurityException("User context not available");
            }

            if (repository instanceof NameBasedRepository) {
                return ((NameBasedRepository<T, I>) repository).findByNameIgnoreCaseAndUserId(name, currentUserId)
                        .map(this::mapToResponseDTO);
            }
        } else {
            if (repository instanceof NameBasedRepository) {
                return ((NameBasedRepository<T, I>) repository).findByNameIgnoreCase(name)
                        .map(this::mapToResponseDTO);
            }
        }

        throw new UnsupportedOperationException("Repository does not support name-based operations");
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

        ValidationUtils.validateString(name, "Name");

        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new SecurityException("User context not available");
            }

            // Use reflection to check if the repository has the method
            try {
                Method method = repository.getClass().getMethod("existsByNameIgnoreCaseAndUserId", String.class, Long.class);
                return (Boolean) method.invoke(repository, name, currentUserId);
            } catch (Exception e) {
                throw new UnsupportedOperationException("Repository does not support name-based operations");
            }
        } else {
            // Use reflection to check if the repository has the method
            try {
                Method method = repository.getClass().getMethod("existsByNameIgnoreCase", String.class);
                return (Boolean) method.invoke(repository, name);
            } catch (Exception e) {
                throw new UnsupportedOperationException("Repository does not support name-based operations");
            }
        }
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

        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new SecurityException("User context not available");
            }

            if (repository instanceof NameBasedRepository) {
                return ((NameBasedRepository<T, I>) repository).findAllByUserIdOrderByNameAsc(currentUserId)
                        .stream()
                        .map(this::mapToResponseDTO)
                        .toList();
            }
        } else {
            if (repository instanceof NameBasedRepository) {
                return ((NameBasedRepository<T, I>) repository).findAllByOrderByNameAsc()
                        .stream()
                        .map(this::mapToResponseDTO)
                        .toList();
            }
        }

        throw new UnsupportedOperationException("Repository does not support name-based operations");
    }

    /**
     * Count entities with optional filtering and searching.
     *
     * @param search  optional search term to filter entities
     * @param filters optional map of field-specific filters
     * @return the count of entities matching the criteria
     */
    public long count(String search, Map<String, Object> filters) {
        // Add user filter if user-aware
        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new SecurityException("User context not available");
            }

            if (filters != null && !filters.containsKey(USER_ID_FIELD)) {
                filters.put(USER_ID_FIELD, currentUserId);
            }
        }

        if (filters == null || filters.isEmpty()) {
            // For search-only counting, we need to use specifications
            Specification<T> spec = createSpecificationFromFilters(search, null);
            return repository.count(spec);
        }

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
        if (id instanceof Long longId) {
            ValidationUtils.validateId(longId);
        } else if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
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
        ensureUserFilter(filters);

        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<Predicate>();

            addSearchPredicates(root, criteriaBuilder, predicates, search);
            addFilterPredicates(root, criteriaBuilder, predicates, filters);

            return predicates.isEmpty() ? null
                    : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void ensureUserFilter(Map<String, Object> filters) {
        if (isUserAware()) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new SecurityException("User context not available");
            }

            if (filters != null && !filters.containsKey(USER_ID_FIELD)) {
                filters.put(USER_ID_FIELD, currentUserId);
            }
        }
    }

    private void addSearchPredicates(Root<T> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            String search) {
        if (search != null && !search.trim().isEmpty()) {
            predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + search.toLowerCase() + "%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")),
                            "%" + search.toLowerCase() + "%")));
        }
    }

    private void addFilterPredicates(Root<T> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            Map<String, Object> filters) {
        if (filters != null) {
            filters.forEach((key, value) -> {
                if (value != null) {
                    addFilterPredicate(root, criteriaBuilder, predicates, key, value);
                }
            });
        }
    }

    private void addFilterPredicate(Root<T> root,
            CriteriaBuilder criteriaBuilder,
            List<Predicate> predicates,
            String key, Object value) {
        switch (key) {
            case USER_ID_FIELD -> predicates.add(criteriaBuilder.equal(root.get("user").get("id"), value));
            case "name" -> predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                    "%" + value.toString().toLowerCase() + "%"));
            case IS_ACTIVE_FIELD -> predicates.add(Boolean.TRUE.equals(value) ?
                    criteriaBuilder.isTrue(root.get(IS_ACTIVE_FIELD))
                    : criteriaBuilder.isFalse(root.get(IS_ACTIVE_FIELD)));
            default -> log.debug("Ignoring unknown filter key: {}", key);
        }
    }

    /**
     * Creates a pageable with sorting.
     *
     * @param pageable      the base pageable
     * @param sortBy        the field to sort by
     * @param sortDirection the sort direction
     * @return the pageable with sorting
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

        if (pageable.isPaged()) {
            return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        } else {
            return PageRequest.of(0, Integer.MAX_VALUE, sort);
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
        return getFieldValue(dto, "getName", String.class);
    }

    /**
     * Generic method to get a field value using reflection.
     *
     * @param obj        the object to get the field from
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
            throw new ReflectionException("Failed to get field value using " + methodName, e);
        }
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

        /**
         * Find entity by name and user ID (case-insensitive).
         *
         * @param name   the name to search for
         * @param userId the user ID
         * @return an Optional containing the entity if found, empty otherwise
         */
        Optional<T> findByNameIgnoreCaseAndUserId(String name, Long userId);

        /**
         * Check if entity exists by name and user ID (case-insensitive).
         *
         * @param name   the name to check
         * @param userId the user ID
         * @return true if entity exists, false otherwise
         */
        boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

        /**
         * Find all entities by user ID ordered by name (ascending).
         *
         * @param userId the user ID
         * @return a list of entities ordered by name
         */
        List<T> findAllByUserIdOrderByNameAsc(Long userId);
    }
}
