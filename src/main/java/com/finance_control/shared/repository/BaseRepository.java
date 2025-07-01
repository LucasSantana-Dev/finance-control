package com.finance_control.shared.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Base repository interface providing common data access operations.
 * This interface extends JpaRepository and adds custom query methods
 * for common operations like date-based filtering and search functionality.
 * 
 * <p>
 * <strong>Search Override Pattern:</strong>
 * Each repository should override the {@link #findAll(String, Pageable)} method
 * to provide entity-specific search functionality. The default implementation
 * searches across the string representation of the entity, but subclasses
 * should override this to search across specific fields relevant to their
 * domain.
 * </p>
 * 
 * <p>
 * <strong>Example Override:</strong>
 * 
 * <pre>{@code
 * @Override
 * @Query("SELECT e FROM Entity e WHERE " +
 *         "(:search IS NULL OR :search = '' OR " +
 *         "LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
 *         "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))")
 * Page<Entity> findAll(@Param("search") String search, Pageable pageable);
 * }</pre>
 * </p>
 * 
 * @param <T> The entity type managed by this repository
 * @param <I> The ID type of the entity (typically Long)
 */
@NoRepositoryBean
public interface BaseRepository<T, I> extends JpaRepository<T, I>, JpaSpecificationExecutor<T> {

    /**
     * Finds entities created between the specified dates.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return a list of entities created within the specified date range
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    List<T> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds entities updated between the specified dates.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return a list of entities updated within the specified date range
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt BETWEEN :startDate AND :endDate")
    List<T> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Finds entities created after the specified date.
     * 
     * @param startDate the start date (inclusive)
     * @return a list of entities created after the specified date
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.createdAt >= :startDate")
    List<T> findByCreatedAtAfter(LocalDateTime startDate);

    /**
     * Finds entities updated after the specified date.
     * 
     * @param startDate the start date (inclusive)
     * @return a list of entities updated after the specified date
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.updatedAt >= :startDate")
    List<T> findByUpdatedAtAfter(LocalDateTime startDate);

    /**
     * Counts entities created between the specified dates.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return the count of entities created within the specified date range
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.createdAt BETWEEN :startDate AND :endDate")
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Counts entities updated between the specified dates.
     * 
     * @param startDate the start date (inclusive)
     * @param endDate   the end date (inclusive)
     * @return the count of entities updated within the specified date range
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.updatedAt BETWEEN :startDate AND :endDate")
    long countByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Counts entities matching the search criteria.
     * 
     * <p>
     * <strong>Important:</strong> This method should be overridden by subclasses
     * to provide entity-specific search counting functionality. The default
     * implementation
     * counts entities based on the string representation, which may not be optimal.
     * </p>
     * 
     * @param search optional search term to filter entities
     * @return the count of entities matching the search criteria
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(CAST(e AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    long count(@Param("search") String search);

    /**
     * Finds all entities with optional search functionality and pagination.
     * 
     * <p>
     * <strong>Important:</strong> This method should be overridden by subclasses
     * to provide entity-specific search functionality. The default implementation
     * searches across the string representation of the entity, which may not be
     * optimal for all use cases.
     * </p>
     * 
     * <p>
     * <strong>Override Guidelines:</strong>
     * <ul>
     * <li>Search should be case-insensitive</li>
     * <li>Search should use LIKE with wildcards for partial matching</li>
     * <li>Search should cover the most relevant fields for the entity</li>
     * <li>Handle null/empty search parameters gracefully</li>
     * </ul>
     * </p>
     * 
     * @param search   optional search term to filter entities
     * @param pageable pagination parameters
     * @return a page of entities matching the search criteria
     */
    @Query("SELECT e FROM #{#entityName} e WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(CAST(e AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<T> findAll(@Param("search") String search, Pageable pageable);
}