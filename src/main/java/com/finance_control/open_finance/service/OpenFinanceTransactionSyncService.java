package com.finance_control.open_finance.service;

import com.finance_control.open_finance.client.AccountInformationClient;
import com.finance_control.open_finance.dto.SyncStatusDTO;
import com.finance_control.open_finance.model.AccountSyncLog;
import com.finance_control.open_finance.model.ConnectedAccount;
import com.finance_control.open_finance.repository.AccountSyncLogRepository;
import com.finance_control.open_finance.repository.ConnectedAccountRepository;
import com.finance_control.shared.enums.TransactionSource;
import com.finance_control.shared.enums.TransactionSubtype;
import com.finance_control.shared.enums.TransactionType;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.service.SupabaseRealtimeService;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for synchronizing transactions from Open Finance accounts.
 * Handles transaction import, duplicate detection, and mapping to internal Transaction entity.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceTransactionSyncService {

    private final ConnectedAccountRepository accountRepository;
    private final AccountSyncLogRepository syncLogRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final TransactionCategoryRepository categoryRepository;
    private final TransactionSourceRepository sourceRepository;
    private final AccountInformationClient accountClient;
    private final OpenFinanceConsentService consentService;
    private final MetricsService metricsService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private SupabaseRealtimeService realtimeService;

    /**
     * Synchronizes transactions for a specific account.
     *
     * @param accountId the account ID
     * @param fromDate optional start date (defaults to last sync date or 30 days ago)
     * @param toDate optional end date (defaults to now)
     * @return sync status DTO
     */
    @Transactional
    public SyncStatusDTO syncTransactions(Long accountId, LocalDateTime fromDate, LocalDateTime toDate) {
        ConnectedAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        if (!account.isSyncable()) {
            throw new IllegalStateException("Account is not syncable");
        }

        // Determine date range
        LocalDateTime syncFromDate = fromDate != null ? fromDate :
                (account.getLastSyncedAt() != null ? account.getLastSyncedAt() :
                        LocalDateTime.now().minusDays(30));
        LocalDateTime syncToDate = toDate != null ? toDate : LocalDateTime.now();

        // Create sync log
        AccountSyncLog syncLog = new AccountSyncLog();
        syncLog.setAccount(account);
        syncLog.setSyncType("TRANSACTIONS");
        syncLog.setStatus("SYNCING");
        syncLog.setSyncedAt(LocalDateTime.now());
        syncLog = syncLogRepository.save(syncLog);

        int recordsImported = 0;
        String errorMessage = null;

        try {
            String accessToken = consentService.getAccessToken(account.getConsent().getId());

            // Fetch transactions from Open Finance API
            var transactionResponse = accountClient.getAccountTransactions(
                    accessToken,
                    account.getExternalAccountId(),
                    syncFromDate,
                    syncToDate,
                    1,
                    100
            ).block();

            if (transactionResponse != null && transactionResponse.getTransactions() != null) {
                // Get or create default category and source entity
                TransactionCategory defaultCategory = getOrCreateDefaultCategory();
                TransactionSourceEntity sourceEntity = getOrCreateSourceEntity(account);

                // Process each transaction
                for (var ofTransaction : transactionResponse.getTransactions()) {
                    try {
                        if (isDuplicate(account.getUser().getId(), ofTransaction.getTransactionId())) {
                            log.debug("Skipping duplicate transaction: {}", ofTransaction.getTransactionId());
                            continue;
                        }

                        TransactionDTO transactionDTO = mapToTransactionDTO(
                                ofTransaction, account, defaultCategory, sourceEntity);
                        transactionService.create(transactionDTO);
                        recordsImported++;
                        metricsService.incrementOpenFinanceTransactionImported();

                        // Notify realtime
                        if (realtimeService != null) {
                            try {
                                realtimeService.notifyTransactionUpdate(account.getUser().getId(), transactionDTO);
                            } catch (Exception e) {
                                log.warn("Failed to send realtime notification: {}", e.getMessage());
                            }
                        }
                    } catch (Exception e) {
                        log.error("Failed to import transaction {}: {}",
                                 ofTransaction.getTransactionId(), e.getMessage());
                    }
                }

                // Handle pagination if needed
                if (transactionResponse.getTotalPages() > 1) {
                    for (int page = 2; page <= transactionResponse.getTotalPages(); page++) {
                        var pageResponse = accountClient.getAccountTransactions(
                                accessToken,
                                account.getExternalAccountId(),
                                syncFromDate,
                                syncToDate,
                                page,
                                100
                        ).block();

                        if (pageResponse != null && pageResponse.getTransactions() != null) {
                            for (var ofTransaction : pageResponse.getTransactions()) {
                                try {
                                    if (isDuplicate(account.getUser().getId(), ofTransaction.getTransactionId())) {
                                        continue;
                                    }

                                    TransactionDTO transactionDTO = mapToTransactionDTO(
                                            ofTransaction, account, defaultCategory, sourceEntity);
                                    transactionService.create(transactionDTO);
                                    recordsImported++;
                                } catch (Exception e) {
                                    log.error("Failed to import transaction {}: {}",
                                             ofTransaction.getTransactionId(), e.getMessage());
                                }
                            }
                        }
                    }
                }
            }

            // Update account sync status
            account.setLastSyncedAt(LocalDateTime.now());
            account.setSyncStatus("SUCCESS");
            accountRepository.save(account);

            syncLog.setStatus("SUCCESS");
            syncLog.setRecordsImported(recordsImported);

            metricsService.incrementOpenFinanceAccountSyncSuccess();
            log.info("Successfully synced {} transactions for account {}", recordsImported, accountId);

        } catch (Exception e) {
            errorMessage = e.getMessage();
            syncLog.setStatus("FAILED");
            syncLog.setErrorMessage(errorMessage);
            account.setSyncStatus("FAILED");
            accountRepository.save(account);
            metricsService.incrementOpenFinanceAccountSyncFailure();
            log.error("Failed to sync transactions for account {}: {}", accountId, errorMessage);
        } finally {
            syncLogRepository.save(syncLog);
        }

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

    /**
     * Synchronizes transactions for all syncable accounts.
     * Called by scheduler.
     */
    @Transactional
    public void syncAllTransactions() {
        List<ConnectedAccount> accounts = accountRepository.findAccountsNeedingSync();

        for (ConnectedAccount account : accounts) {
            try {
                syncTransactions(account.getId(), null, null);
            } catch (Exception e) {
                log.error("Failed to sync transactions for account {}: {}", account.getId(), e.getMessage());
            }
        }
    }

    private boolean isDuplicate(Long userId, String externalTransactionId) {
        if (externalTransactionId == null) {
            return false;
        }
        return transactionRepository.existsByUserIdAndExternalReference(userId, externalTransactionId);
    }

    private TransactionDTO mapToTransactionDTO(
            AccountInformationClient.Transaction ofTransaction,
            ConnectedAccount account,
            TransactionCategory category,
            TransactionSourceEntity sourceEntity) {

        TransactionDTO dto = new TransactionDTO();
        dto.setUserId(account.getUser().getId());
        dto.setDescription(ofTransaction.getDescription() != null ?
                          ofTransaction.getDescription() : "Open Finance Transaction");
        dto.setAmount(ofTransaction.getAmount().abs());
        dto.setDate(ofTransaction.getBookingDate() != null ?
                   ofTransaction.getBookingDate() : LocalDateTime.now());
        dto.setExternalReference(ofTransaction.getTransactionId());
        dto.setBankReference(ofTransaction.getTransactionId());

        // Determine transaction type based on credit/debit indicator
        if ("CREDIT".equalsIgnoreCase(ofTransaction.getCreditDebitIndicator())) {
            dto.setType(TransactionType.INCOME);
            dto.setSubtype(TransactionSubtype.VARIABLE);
        } else {
            dto.setType(TransactionType.EXPENSE);
            dto.setSubtype(TransactionSubtype.VARIABLE);
        }

        // Set source based on account type
        dto.setSource(mapAccountTypeToSource(account.getAccountType()));

        // Set category and source entity
        dto.setCategoryId(category.getId());
        dto.setSourceEntityId(sourceEntity.getId());

        return dto;
    }

    private TransactionSource mapAccountTypeToSource(String accountType) {
        if (accountType == null) {
            return TransactionSource.OTHER;
        }
        return switch (accountType.toUpperCase()) {
            case "CHECKING", "SAVINGS" -> TransactionSource.BANK_TRANSACTION;
            case "CREDIT_CARD" -> TransactionSource.CREDIT_CARD;
            case "DEBIT_CARD" -> TransactionSource.DEBIT_CARD;
            default -> TransactionSource.OTHER;
        };
    }

    private TransactionCategory getOrCreateDefaultCategory() {
        return categoryRepository.findByNameIgnoreCase("Open Finance")
                .orElseGet(() -> {
                    TransactionCategory category = new TransactionCategory();
                    category.setName("Open Finance");
                    return categoryRepository.save(category);
                });
    }

    private TransactionSourceEntity getOrCreateSourceEntity(ConnectedAccount account) {
        String sourceName = account.getInstitution().getName() + " - " +
                           (account.getAccountNumber() != null ? account.getAccountNumber() : account.getExternalAccountId());

        return sourceRepository.findByNameIgnoreCaseAndUserId(sourceName, account.getUser().getId())
                .orElseGet(() -> {
                    TransactionSourceEntity source = new TransactionSourceEntity();
                    source.setUser(account.getUser());
                    source.setName(sourceName);
                    source.setSourceType(TransactionSource.BANK_TRANSACTION);
                    source.setBankName(account.getInstitution().getName());
                    source.setAccountNumber(account.getAccountNumber());
                    source.setIsActive(true);
                    return sourceRepository.save(source);
                });
    }
}
