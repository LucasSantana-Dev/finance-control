package com.finance_control.transactions.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for transaction reconciliation requests.
 * Used for complete reconciliation of transaction data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReconciliationRequest {
    
    @NotNull(message = "Reconciled amount is required")
    private BigDecimal reconciledAmount;
    
    @NotNull(message = "Reconciliation date is required")
    private LocalDateTime reconciliationDate;
    
    @NotNull(message = "Reconciled status is required")
    private Boolean reconciled;
    
    private String reconciliationNotes;
    
    private String bankReference;
    
    private String externalReference;
} 