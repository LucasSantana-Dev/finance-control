package com.finance_control.transactions.repository.subcategory;

import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionSubcategoryRepository extends BaseRepository<TransactionSubcategory, Long> {

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
}