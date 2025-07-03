package com.finance_control.transactions.repository.category;

import com.finance_control.shared.repository.BaseRepository;
import com.finance_control.transactions.model.category.TransactionCategory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionCategoryRepository extends BaseRepository<TransactionCategory, Long> {
    
    List<TransactionCategory> findAllByOrderByNameAsc();
    
    Optional<TransactionCategory> findByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCase(String name);
} 