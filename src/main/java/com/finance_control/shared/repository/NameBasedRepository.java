package com.finance_control.shared.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

/**
 * Repository interface for entities that support name-based operations.
 * This interface provides common methods for finding and checking existence
 * of entities by name, with support for both user-aware and non-user-aware entities.
 *
 * @param <T> The entity type
 * @param <I> The ID type of the entity (typically Long)
 */
@NoRepositoryBean
public interface NameBasedRepository<T, I> {

    /**
     * Find an entity by name (case-insensitive).
     *
     * @param name the name to search for
     * @return an Optional containing the entity if found, empty otherwise
     */
    Optional<T> findByNameIgnoreCase(String name);

    /**
     * Check if an entity exists by name (case-insensitive).
     *
     * @param name the name to check
     * @return true if entity exists, false otherwise
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find an entity by name and user ID (case-insensitive).
     * Used for user-aware entities.
     *
     * @param name the name to search for
     * @param userId the user ID to filter by
     * @return an Optional containing the entity if found, empty otherwise
     */
    Optional<T> findByNameIgnoreCaseAndUserId(String name, Long userId);

    /**
     * Check if an entity exists by name and user ID (case-insensitive).
     * Used for user-aware entities.
     *
     * @param name the name to check
     * @param userId the user ID to filter by
     * @return true if entity exists, false otherwise
     */
    boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
}
