package com.finance_control.transactions.repository;

import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.transactions.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends BaseRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    
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
    @Query(
        "SELECT t FROM Transaction t WHERE " +
        "(:search IS NULL OR :search = '' OR " +
        "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(t.type) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(t.subtype) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
        "LOWER(t.source) LIKE LOWER(CONCAT('%', :search, '%')) )"
    )
    Page<Transaction> findAll(
        @Param("search") String search,
        Pageable pageable
    );
} 