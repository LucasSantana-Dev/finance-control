package com.finance_control.users.model;

import com.finance_control.goals.model.FinancialGoal;
import com.finance_control.profile.model.Profile;
import com.finance_control.shared.converter.EncryptedEmailConverter;
import com.finance_control.shared.model.BaseModel;
import com.finance_control.transactions.model.Transaction;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = {"profile", "transactions", "financialGoals"})
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel<Long> {

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    @Convert(converter = EncryptedEmailConverter.class)
    private String email;

    @Column(name = "email_hash", length = 64, unique = true)
    private String emailHash;

    @Size(min = 8)
    @Column(nullable = true)
    private String password;

    @Column(name = "supabase_user_id", length = 36, unique = true)
    private String supabaseUserId;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FinancialGoal> financialGoals;
}
