package com.finance_control.shared.repository;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for name-based operations.
 * Extracted from BaseService to reduce file length.
 *
 * @param <T> The entity type
 * @param <I> The ID type
 */
@NoRepositoryBean
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
