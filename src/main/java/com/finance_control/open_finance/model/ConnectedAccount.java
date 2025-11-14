package com.finance_control.open_finance.model;

import com.finance_control.shared.model.BaseModel;
import com.finance_control.users.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a bank account connected via Open Finance.
 * Stores account information, balance, and sync status.
 */
@Entity
@Table(name = "connected_accounts",
       uniqueConstraints = @UniqueConstraint(columnNames = {"institution_id", "external_account_id"}),
       indexes = {
           @Index(name = "idx_connected_accounts_user_id", columnList = "user_id"),
           @Index(name = "idx_connected_accounts_consent_id", columnList = "consent_id"),
           @Index(name = "idx_connected_accounts_institution_id", columnList = "institution_id"),
           @Index(name = "idx_connected_accounts_sync_status", columnList = "sync_status"),
           @Index(name = "idx_connected_accounts_last_synced_at", columnList = "last_synced_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ConnectedAccount extends BaseModel<Long> {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consent_id", nullable = false)
    private OpenFinanceConsent consent;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private OpenFinanceInstitution institution;

    @NotBlank
    @Column(name = "external_account_id", nullable = false, length = 255)
    private String externalAccountId; // Account ID from the institution

    @NotBlank
    @Column(name = "account_type", nullable = false, length = 50)
    private String accountType; // CHECKING, SAVINGS, CREDIT_CARD, etc.

    @Column(name = "account_number", length = 100)
    private String accountNumber;

    @Column(length = 50)
    private String branch;

    @Column(name = "account_holder_name", length = 255)
    private String accountHolderName;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(length = 3)
    private String currency = "BRL";

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @NotBlank
    @Column(name = "sync_status", nullable = false, length = 50)
    private String syncStatus = "PENDING"; // PENDING, SYNCING, SUCCESS, FAILED, DISABLED

    public boolean isSyncable() {
        return !"DISABLED".equals(syncStatus) &&
               consent != null &&
               consent.isActive();
    }
}
