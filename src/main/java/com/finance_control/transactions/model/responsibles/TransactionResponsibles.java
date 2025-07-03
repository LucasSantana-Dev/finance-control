package com.finance_control.transactions.model.responsibles;

import com.finance_control.shared.model.BaseModel;
import com.finance_control.transactions.model.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Entity
@Table(name = "transaction_responsibles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionResponsibles extends BaseModel<Long> {
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    // Relationship with transactions through responsibility assignments
    @OneToMany(mappedBy = "responsible", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionResponsibility> responsibilities;
    
    // Inner class for responsibility assignments
    @Entity
    @Table(name = "transaction_responsibilities")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class TransactionResponsibility extends BaseModel<Long> {
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "transaction_id", nullable = false)
        private Transaction transaction;
        
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "responsible_id", nullable = false)
        private TransactionResponsibles responsible;
        
        @NotNull
        @DecimalMin(value = "0.01", message = "Percentage must be at least 0.01%")
        @DecimalMax(value = "100.00", message = "Percentage cannot exceed 100%")
        @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
        private BigDecimal percentage;
        
        @Column(name = "calculated_amount", precision = 19, scale = 2)
        private BigDecimal calculatedAmount;
        
        @Column(name = "notes", length = 500)
        private String notes;
        
        public void calculateAmount() {
            if (transaction != null && transaction.getAmount() != null && percentage != null) {
                this.calculatedAmount = transaction.getAmount()
                        .multiply(percentage)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }

        public TransactionResponsibility(Transaction transaction, TransactionResponsibles responsible, 
                                       BigDecimal percentage) {
            this.transaction = transaction;
            this.responsible = responsible;
            this.percentage = percentage;
            calculateAmount();
        }
    }
} 