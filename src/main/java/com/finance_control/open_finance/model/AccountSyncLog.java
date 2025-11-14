package com.finance_control.open_finance.model;

import com.finance_control.shared.model.BaseModel;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a log entry for account synchronization operations.
 * Tracks sync attempts, results, and errors.
 */
@Entity
@Table(name = "account_sync_logs",
       indexes = {
           @Index(name = "idx_account_sync_logs_account_id", columnList = "account_id"),
           @Index(name = "idx_account_sync_logs_sync_type", columnList = "sync_type"),
           @Index(name = "idx_account_sync_logs_status", columnList = "status"),
           @Index(name = "idx_account_sync_logs_synced_at", columnList = "synced_at")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AccountSyncLog extends BaseModel<Long> {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private ConnectedAccount account;

    @NotBlank
    @Column(name = "sync_type", nullable = false, length = 50)
    private String syncType; // BALANCE, TRANSACTIONS, FULL

    @NotBlank
    @Column(nullable = false, length = 50)
    private String status; // SUCCESS, FAILED, PARTIAL

    @Column(name = "records_imported")
    private Integer recordsImported = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @NotNull
    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt = LocalDateTime.now();
}
