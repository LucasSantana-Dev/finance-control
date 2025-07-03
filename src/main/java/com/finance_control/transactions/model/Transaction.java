package com.finance_control.transactions.model;

import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.model.BaseModel;
import com.finance_control.users.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles;
import com.finance_control.transactions.model.responsibles.TransactionResponsibles.TransactionResponsibility;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.model.subcategory.TransactionSubcategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@ToString(exclude = {"user", "category", "subcategory", "sourceEntity", "responsibilities"})
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseModel<Long> {
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private TransactionType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionSubtype subtype;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionSource source;
    
    @NotBlank
    @Column(nullable = false)
    private String description;
    
    @NotNull
    @Positive
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "installments")
    private Integer installments;
    
    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TransactionCategory category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private TransactionSubcategory subcategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_entity_id")
    private TransactionSourceEntity sourceEntity;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<TransactionResponsibility> responsibilities = new ArrayList<>();
    
    // Reconciliation fields
    @Column(name = "reconciled_amount", precision = 19, scale = 2)
    private BigDecimal reconciledAmount;
    
    @Column(name = "reconciliation_date")
    private LocalDateTime reconciliationDate;
    
    @Column(name = "is_reconciled")
    private Boolean reconciled = false;
    
    @Column(name = "reconciliation_notes", length = 1000)
    private String reconciliationNotes;
    
    @Column(name = "bank_reference", length = 100)
    private String bankReference;
    
    @Column(name = "external_reference", length = 100)
    private String externalReference;
    
    public void addResponsible(TransactionResponsibles responsible, BigDecimal percentage) {
        TransactionResponsibility responsibility = new TransactionResponsibility(this, responsible, percentage);
        this.responsibilities.add(responsibility);
    }
    
    public void addResponsible(TransactionResponsibles responsible, BigDecimal percentage, String notes) {
        TransactionResponsibility responsibility = new TransactionResponsibility(this, responsible, percentage);
        responsibility.setNotes(notes);
        this.responsibilities.add(responsibility);
    }
    
    public BigDecimal getTotalPercentage() {
        return responsibilities.stream()
                .map(TransactionResponsibility::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public boolean isPercentageValid() {
        BigDecimal total = getTotalPercentage();
        return total.compareTo(BigDecimal.valueOf(100)) == 0;
    }
    
    public BigDecimal getAmountForResponsible(TransactionResponsibles responsible) {
        return responsibilities.stream()
                .filter(r -> r.getResponsible().equals(responsible))
                .map(TransactionResponsibility::getCalculatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 