package com.finance_control.transactions.service;

import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import com.finance_control.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Helper class for transaction query operations.
 * Reduces coupling and class fan-out in TransactionService.
 */
@Component
@RequiredArgsConstructor
public class TransactionQueryHelper {

    private final TransactionRepository transactionRepository;
    private final TransactionEntityLookupHelper entityLookupHelper;

    public List<TransactionCategory> getCategoriesByUserId(Long userId) {
        return transactionRepository.findDistinctCategoriesByUserId(userId);
    }

    public List<TransactionSubcategory> getSubcategoriesByCategoryId(Long categoryId) {
        return entityLookupHelper.getSubcategoriesByCategoryId(categoryId);
    }

    public List<String> getTransactionTypes() {
        return Collections.unmodifiableList(transactionRepository.findDistinctTypes());
    }

    public List<TransactionSourceEntity> getSourceEntities() {
        return Collections.unmodifiableList(entityLookupHelper.getAllSourceEntities());
    }

    public BigDecimal getTotalAmountByUserId(Long userId) {
        return transactionRepository.getTotalAmountByUserId(userId);
    }

    public Map<String, BigDecimal> getAmountByType(Long userId) {
        return transactionRepository.getAmountByType(userId);
    }

    public Map<String, BigDecimal> getAmountByCategory(Long userId) {
        return transactionRepository.getAmountByCategory(userId);
    }

    public Map<String, Object> getMonthlySummary(Long userId, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.getMonthlySummary(userId, startDate, endDate);
    }
}



