package com.finance_control.transactions.model.helper;

import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles.TransactionResponsibility;

import java.math.BigDecimal;
import java.util.List;

/**
 * Helper class for transaction responsibility-related business logic.
 * Extracted from Transaction.java to reduce class fan-out complexity.
 */
public class TransactionResponsibilityHelper {

    /**
     * Calculates the total percentage from all responsibilities.
     *
     * @param responsibilities the list of responsibilities
     * @return the total percentage
     */
    public static BigDecimal getTotalPercentage(List<TransactionResponsibility> responsibilities) {
        return responsibilities.stream()
                .map(TransactionResponsibility::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Validates that the total percentage equals 100.
     *
     * @param responsibilities the list of responsibilities
     * @return true if total percentage equals 100, false otherwise
     */
    public static boolean isPercentageValid(List<TransactionResponsibility> responsibilities) {
        BigDecimal total = getTotalPercentage(responsibilities);
        return total.compareTo(BigDecimal.valueOf(100)) == 0;
    }

    /**
     * Calculates the amount for a specific responsible.
     *
     * @param responsibilities the list of responsibilities
     * @param responsible the responsible to calculate amount for
     * @param totalAmount the total transaction amount
     * @return the calculated amount for the responsible
     */
    public static BigDecimal getAmountForResponsible(List<TransactionResponsibility> responsibilities,
                                                     TransactionResponsibles responsible,
                                                     BigDecimal totalAmount) {
        return responsibilities.stream()
                .filter(r -> r.getResponsible().equals(responsible))
                .map(r -> r.getCalculatedAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

