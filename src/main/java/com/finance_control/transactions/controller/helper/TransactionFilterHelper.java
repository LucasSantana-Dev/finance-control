package com.finance_control.transactions.controller.helper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Helper class for transaction filtering logic in TransactionController.
 * Extracted to reduce class fan-out complexity.
 */
@Component
public class TransactionFilterHelper {

    /**
     * Adds transaction-specific filters to the filters map.
     *
     * @param category the category filter
     * @param subcategory the subcategory filter
     * @param source the source filter
     * @param type the type filter
     * @param startDate the start date filter
     * @param endDate the end date filter
     * @param minAmount the minimum amount filter
     * @param maxAmount the maximum amount filter
     * @param isActive the active status filter
     * @param filters the filters map to modify
     */
    public void addTransactionFilters(String category, String subcategory, String source, String type,
                                     LocalDate startDate, LocalDate endDate, BigDecimal minAmount,
                                     BigDecimal maxAmount, Boolean isActive, Map<String, Object> filters) {
        addStringFilter("category", category, filters);
        addStringFilter("subcategory", subcategory, filters);
        addStringFilter("source", source, filters);
        addStringFilter("type", type, filters);
        addDateFilter("startDate", startDate, filters);
        addDateFilter("endDate", endDate, filters);
        addAmountFilter("minAmount", minAmount, filters);
        addAmountFilter("maxAmount", maxAmount, filters);
        addBooleanFilter("isActive", isActive, filters);
    }

    private void addStringFilter(String key, String value, Map<String, Object> filters) {
        if (value != null && !value.trim().isEmpty()) {
            filters.put(key, value);
        }
    }

    private void addDateFilter(String key, LocalDate value, Map<String, Object> filters) {
        if (value != null) {
            filters.put(key, value);
        }
    }

    private void addAmountFilter(String key, BigDecimal value, Map<String, Object> filters) {
        if (value != null) {
            filters.put(key, value);
        }
    }

    private void addBooleanFilter(String key, Boolean value, Map<String, Object> filters) {
        if (value != null) {
            filters.put(key, value);
        }
    }
}

