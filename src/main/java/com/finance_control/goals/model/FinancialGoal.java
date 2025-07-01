package com.finance_control.goals.model;

import com.finance_control.shared.enums.GoalType;
import com.finance_control.shared.model.BaseEntity;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.users.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "financial_goals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FinancialGoal extends BaseEntity<Long> {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private GoalType goalType;

    @NotNull
    @Positive
    @Column(name = "target_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", precision = 19, scale = 2)
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "auto_calculate")
    private Boolean autoCalculate = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private TransactionSourceEntity account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public BigDecimal getProgressPercentage() {
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getRemainingAmount() {
        return targetAmount.subtract(currentAmount != null ? currentAmount : BigDecimal.ZERO);
    }

    public boolean isCompleted() {
        return currentAmount != null && currentAmount.compareTo(targetAmount) >= 0;
    }
}