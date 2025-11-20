package com.finance_control.open_finance.service.helper;

import com.finance_control.open_finance.dto.SyncStatusDTO;
import com.finance_control.open_finance.model.AccountSyncLog;
import com.finance_control.open_finance.model.ConnectedAccount;
import com.finance_control.open_finance.repository.AccountSyncLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Helper class for managing sync logs in OpenFinanceTransactionSyncService.
 * Extracted to reduce class fan-out complexity.
 */
@Component
@RequiredArgsConstructor
public class OpenFinanceSyncLogHelper {

    private final AccountSyncLogRepository syncLogRepository;

    /**
     * Creates a new sync log for the given account.
     *
     * @param account the connected account
     * @return the created sync log
     */
    public AccountSyncLog createSyncLog(ConnectedAccount account) {
        AccountSyncLog syncLog = new AccountSyncLog();
        syncLog.setAccount(account);
        syncLog.setSyncType("TRANSACTIONS");
        syncLog.setStatus("SYNCING");
        syncLog.setSyncedAt(LocalDateTime.now());
        return syncLogRepository.save(syncLog);
    }

    /**
     * Updates the sync log with success status and records imported.
     *
     * @param syncLog the sync log to update
     * @param recordsImported the number of records imported
     */
    public void updateSyncLogSuccess(AccountSyncLog syncLog, int recordsImported) {
        syncLog.setStatus("SUCCESS");
        syncLog.setRecordsImported(recordsImported);
    }

    /**
     * Updates the sync log with failure status and error message.
     *
     * @param syncLog the sync log to update
     * @param errorMessage the error message
     */
    public void updateSyncLogFailure(AccountSyncLog syncLog, String errorMessage) {
        syncLog.setStatus("FAILED");
        syncLog.setErrorMessage(errorMessage);
    }

    /**
     * Builds a SyncStatusDTO from the sync log and related data.
     *
     * @param accountId the account ID
     * @param syncLog the sync log
     * @param recordsImported the number of records imported
     * @param errorMessage the error message (if any)
     * @return the SyncStatusDTO
     */
    public SyncStatusDTO buildSyncStatusDTO(Long accountId, AccountSyncLog syncLog,
                                           int recordsImported, String errorMessage) {
        SyncStatusDTO dto = new SyncStatusDTO();
        dto.setAccountId(accountId);
        dto.setSyncStatus(syncLog.getStatus());
        dto.setSyncType("TRANSACTIONS");
        dto.setRecordsImported(recordsImported);
        dto.setErrorMessage(errorMessage);
        dto.setLastSyncedAt(syncLog.getSyncedAt());
        dto.setSuccess("SUCCESS".equals(syncLog.getStatus()));
        return dto;
    }
}

