package com.finance_control.transactions.repository.category;

import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.shared.repository.NameBasedRepository;
import com.finance_control.transactions.model.category.TransactionCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCategoryRepository extends BaseRepository<TransactionCategory, Long>, NameBasedRepository<TransactionCategory, Long> {

    List<TransactionCategory> findAllByOrderByNameAsc();

    // Override BaseRepository search method to search by name field
    @Override
    @Query("SELECT c FROM TransactionCategory c WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TransactionCategory> findAll(@Param("search") String search, Pageable pageable);

    // NameBasedRepository interface methods
    Optional<TransactionCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);

    // User-aware methods (not applicable for categories, but required by interface)
    @Override
    default Optional<TransactionCategory> findByNameIgnoreCaseAndUserId(String name, Long userId) {
        throw new UnsupportedOperationException("Transaction categories are not user-aware. Use findByNameIgnoreCase instead.");
    }

    @Override
    default boolean existsByNameIgnoreCaseAndUserId(String name, Long userId) {
        throw new UnsupportedOperationException("Transaction categories are not user-aware. Use existsByNameIgnoreCase instead.");
    }
}
