package com.finance_control.transactions.repository;

import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.transactions.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface TransactionRepository extends BaseRepository<Transaction, Long> {

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = :type " +
            "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumByUserAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT t FROM Transaction t " +
            "LEFT JOIN FETCH t.responsibilities r " +
            "LEFT JOIN FETCH r.responsible " +
            "WHERE t.user.id = :userId")
    List<Transaction> findByUserIdWithResponsibilities(@Param("userId") Long userId);

    @Override
    @Query("SELECT t FROM Transaction t WHERE " +
            "(:search IS NULL OR :search = '' OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.type) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.subtype) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.source) LIKE LOWER(CONCAT('%', :search, '%')) )")
    Page<Transaction> findAll(
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT DISTINCT t.category FROM Transaction t WHERE t.user.id = :userId")
    List<com.finance_control.transactions.model.category.TransactionCategory> findDistinctCategoriesByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT t.type FROM Transaction t")
    List<String> findDistinctTypes();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId")
    BigDecimal getTotalAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT t.type, COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user.id = :userId GROUP BY t.type")
    Map<String, BigDecimal> getAmountByType(@Param("userId") Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.amount = :amount AND LOWER(t.description) = LOWER(:description) " +
            "AND t.date BETWEEN :startDate AND :endDate")
    List<Transaction> findPotentialDuplicates(@Param("userId") Long userId,
            @Param("amount") BigDecimal amount,
            @Param("description") String description,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT c.name, COALESCE(SUM(t.amount), 0) FROM Transaction t JOIN t.category c WHERE t.user.id = :userId GROUP BY c.name")
    Map<String, BigDecimal> getAmountByCategory(@Param("userId") Long userId);

    @Query("SELECT FUNCTION('DATE_FORMAT', t.date, '%Y-%m') as month, " +
           "COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as income, " +
           "COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as expense " +
           "FROM Transaction t WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE_FORMAT', t.date, '%Y-%m') ORDER BY month")
    Map<String, Object> getMonthlySummary(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.user.id = :userId AND t.externalReference = :externalReference")
    long countByUserIdAndExternalReference(@Param("userId") Long userId, @Param("externalReference") String externalReference);

    default boolean existsByUserIdAndExternalReference(Long userId, String externalReference) {
        return countByUserIdAndExternalReference(userId, externalReference) > 0;
    }
}
