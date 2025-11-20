package com.finance_control.shared.service.helper;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.repository.NameBasedRepository;
import com.finance_control.shared.util.ValidationUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Helper class for name-based operations in BaseService.
 * Extracted to reduce BaseService file length.
 *
 * @param <T> The entity type
 * @param <I> The ID type
 * @param <D> The DTO type
 */
public class BaseServiceNameBasedHelper<T, I, D> {

    private final BaseRepository<T, I> repository;
    private final boolean userAware;
    private final Function<T, D> mapToResponseDTO;

    public BaseServiceNameBasedHelper(BaseRepository<T, I> repository, boolean userAware,
                                     Function<T, D> mapToResponseDTO) {
        this.repository = repository;
        this.userAware = userAware;
        this.mapToResponseDTO = mapToResponseDTO;
    }

    /**
     * Find entity by name (if name-based).
     *
     * @param name the name to search for
     * @return an Optional containing the response DTO if found, empty otherwise
     */
    public Optional<D> findByName(String name) {
        ValidationUtils.validateString(name, "Name");

        if (userAware) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new SecurityException("User context not available");
            }

            if (repository instanceof NameBasedRepository) {
                return ((NameBasedRepository<T, I>) repository).findByNameIgnoreCaseAndUserId(name, currentUserId)
                        .map(mapToResponseDTO);
            }
        } else {
            if (repository instanceof NameBasedRepository) {
                return ((NameBasedRepository<T, I>) repository).findByNameIgnoreCase(name)
                        .map(mapToResponseDTO);
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
        ValidationUtils.validateString(name, "Name");

        if (userAware) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new SecurityException("User context not available");
            }

            try {
                Method method = repository.getClass().getMethod("existsByNameIgnoreCaseAndUserId", String.class, Long.class);
                return (Boolean) method.invoke(repository, name, currentUserId);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new UnsupportedOperationException("Repository does not support name-based operations");
            }
        } else {
            try {
                Method method = repository.getClass().getMethod("existsByNameIgnoreCase", String.class);
                return (Boolean) method.invoke(repository, name);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
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
        if (userAware) {
            Long currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new SecurityException("User context not available");
            }

            if (repository instanceof NameBasedRepository) {
                return ((NameBasedRepository<T, I>) repository).findAllByUserIdOrderByNameAsc(currentUserId)
                        .stream()
                        .map(mapToResponseDTO)
                        .toList();
            }
        } else {
            if (repository instanceof NameBasedRepository) {
                return ((NameBasedRepository<T, I>) repository).findAllByOrderByNameAsc()
                        .stream()
                        .map(mapToResponseDTO)
                        .toList();
            }
        }

        throw new UnsupportedOperationException("Repository does not support name-based operations");
    }
}

