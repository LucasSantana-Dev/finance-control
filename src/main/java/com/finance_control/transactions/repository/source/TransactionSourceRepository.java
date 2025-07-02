package com.finance_control.transactions.repository.source;

import com.finance_control.shared.service.BaseService;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionSourceRepository extends BaseService.NameBasedRepository<TransactionSourceEntity, Long> {

        @Query("SELECT tse FROM TransactionSourceEntity tse WHERE tse.user.id = :userId AND tse.sourceType = :sourceType")
        List<TransactionSourceEntity> findByUserIdAndSourceType(@Param("userId") Long userId,
                        @Param("sourceType") com.finance_control.shared.enums.TransactionSource sourceType);

        Optional<TransactionSourceEntity> findByIdAndUserId(Long id, Long userId);

        Optional<TransactionSourceEntity> findByNameIgnoreCaseAndUserId(String name, Long userId);

        boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);

        List<TransactionSourceEntity> findAllByUserIdOrderByNameAsc(Long userId);

        @Override
        @Query("SELECT tse FROM TransactionSourceEntity tse WHERE " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(tse.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(tse.sourceType) LIKE LOWER(CONCAT('%', :search, '%')))")
        Page<TransactionSourceEntity> findAll(@Param("search") String search, Pageable pageable);
}