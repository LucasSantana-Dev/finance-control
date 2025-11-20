package com.finance_control.transactions.repository.subcategory;

import com.finance_control.shared.repository.NameBasedRepository;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionSubcategoryRepository extends NameBasedRepository<TransactionSubcategory, Long> {

    List<TransactionSubcategory> findByCategoryIdAndIsActiveTrueOrderByNameAsc(Long categoryId);

    List<TransactionSubcategory> findByIsActiveTrueOrderByNameAsc();

    Optional<TransactionSubcategory> findByCategoryIdAndNameIgnoreCase(Long categoryId, String name);

    boolean existsByCategoryIdAndNameIgnoreCase(Long categoryId, String name);

    @Query("SELECT s FROM TransactionSubcategory s " +
            "LEFT JOIN s.transactions t " +
            "WHERE s.category.id = :categoryId AND s.isActive = true " +
            "GROUP BY s.id " +
            "ORDER BY COUNT(t) DESC, s.name ASC")
    List<TransactionSubcategory> findByCategoryIdOrderByUsageAndName(@Param("categoryId") Long categoryId);

    long countByCategoryIdAndIsActiveTrue(Long categoryId);

    // Override BaseRepository search method to search by name field
    @Override
    @Query("SELECT s FROM TransactionSubcategory s WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TransactionSubcategory> findAll(@Param("search") String search, Pageable pageable);

    // NameBasedRepository interface methods
    // For subcategories, these methods are not applicable as names are scoped to
    // categories
    // We'll provide default implementations that throw
    // UnsupportedOperationException

    @Override
    default Optional<TransactionSubcategory> findByNameIgnoreCase(String name) {
        throw new UnsupportedOperationException(
                "Subcategory names are scoped to categories. Use findByCategoryIdAndNameIgnoreCase instead.");
    }

    @Override
    default boolean existsByNameIgnoreCase(String name) {
        throw new UnsupportedOperationException(
                "Subcategory names are scoped to categories. Use existsByCategoryIdAndNameIgnoreCase instead.");
    }

    @Override
    default Optional<TransactionSubcategory> findByNameIgnoreCaseAndUserId(String name, Long userId) {
        throw new UnsupportedOperationException(
                "Subcategory names are scoped to categories. Use findByCategoryIdAndNameIgnoreCase instead.");
    }

    @Override
    default boolean existsByNameIgnoreCaseAndUserId(String name, Long userId) {
        throw new UnsupportedOperationException(
                "Subcategory names are scoped to categories. Use existsByCategoryIdAndNameIgnoreCase instead.");
    }
}
