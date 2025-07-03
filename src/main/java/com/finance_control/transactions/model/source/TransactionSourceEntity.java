package com.finance_control.transactions.model.source;

import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.model.BaseModel;
import com.finance_control.users.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.finance_control.transactions.model.Transaction;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "transaction_source_entity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransactionSourceEntity extends BaseModel<Long> {
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private TransactionSource sourceType;
    
    @Column(name = "bank_name")
    private String bankName;
    
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "card_type")
    private String cardType;
    
    @Column(name = "card_last_four")
    private String cardLastFour;
    
    @Column(name = "account_balance", precision = 19, scale = 2)
    private BigDecimal accountBalance;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @OneToMany(mappedBy = "sourceEntity", fetch = FetchType.LAZY)
    private List<Transaction> transactions;
} 