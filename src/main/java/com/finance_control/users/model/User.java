package com.finance_control.users.model;

import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.shared.model.BaseEntity;
import com.finance_control.transactions.model.Transaction;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity<Long> {
    
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank
    @Size(min = 8)
    @Column(nullable = false)
    private String password;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FinancialGoal> financialGoals;
} 