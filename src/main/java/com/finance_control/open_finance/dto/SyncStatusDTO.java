package com.finance_control.open_finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for account synchronization status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncStatusDTO {

    @NotNull
    private Long accountId;

    private String syncStatus;
    private String syncType;
    private Integer recordsImported;
    private String errorMessage;
    private LocalDateTime lastSyncedAt;
    private boolean success;
}
