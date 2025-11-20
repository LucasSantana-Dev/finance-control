package com.finance_control.open_finance.service;

import com.finance_control.open_finance.client.AccountInformationClient;
import com.finance_control.open_finance.dto.SyncStatusDTO;
import com.finance_control.open_finance.model.AccountSyncLog;
import com.finance_control.open_finance.model.ConnectedAccount;
import com.finance_control.open_finance.repository.AccountSyncLogRepository;
import com.finance_control.open_finance.repository.ConnectedAccountRepository;
import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.service.SupabaseRealtimeService;
import com.finance_control.transactions.dto.TransactionDTO;
import com.finance_control.transactions.model.category.TransactionCategory;
import com.finance_control.transactions.model.source.TransactionSourceEntity;
import com.finance_control.transactions.repository.TransactionRepository;
import com.finance_control.transactions.repository.category.TransactionCategoryRepository;
import com.finance_control.transactions.repository.source.TransactionSourceRepository;
import com.finance_control.transactions.service.TransactionService;
import com.finance_control.open_finance.service.mapper.OpenFinanceTransactionMapper;
import com.finance_control.open_finance.service.helper.OpenFinanceSyncLogHelper;
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
    private final OpenFinanceTransactionMapper transactionMapper;
    private final OpenFinanceSyncLogHelper syncLogHelper;

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
        ConnectedAccount account = validateAndGetAccount(accountId);
        DateRange dateRange = calculateDateRange(account, fromDate, toDate);
        AccountSyncLog syncLog = syncLogHelper.createSyncLog(account);

        int recordsImported = 0;
        String errorMessage = null;

        try {
            String accessToken = consentService.getAccessToken(account.getConsent().getId());
            recordsImported = processTransactions(account, dateRange, accessToken);
            updateAccountSyncStatus(account, "SUCCESS");
            syncLogHelper.updateSyncLogSuccess(syncLog, recordsImported);
            metricsService.incrementOpenFinanceAccountSyncSuccess();
            log.info("Successfully synced {} transactions for account {}", recordsImported, accountId);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            handleSyncFailure(account, syncLog, errorMessage, accountId);
        } finally {
            syncLogRepository.save(syncLog);
        }

        return syncLogHelper.buildSyncStatusDTO(accountId, syncLog, recordsImported, errorMessage);
    }

    private ConnectedAccount validateAndGetAccount(Long accountId) {
        ConnectedAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
        if (!account.isSyncable()) {
            throw new IllegalStateException("Account is not syncable");
        }
        return account;
    }

    private DateRange calculateDateRange(ConnectedAccount account, LocalDateTime fromDate, LocalDateTime toDate) {
        LocalDateTime syncFromDate = fromDate != null ? fromDate :
                (account.getLastSyncedAt() != null ? account.getLastSyncedAt() :
                        LocalDateTime.now().minusDays(30));
        LocalDateTime syncToDate = toDate != null ? toDate : LocalDateTime.now();
        return new DateRange(syncFromDate, syncToDate);
    }


    private int processTransactions(ConnectedAccount account, DateRange dateRange, String accessToken) {
        AccountInformationClient.TransactionListResponse transactionResponse = fetchFirstPage(account, dateRange, accessToken);
        if (transactionResponse == null || transactionResponse.getTransactions() == null) {
            return 0;
        }

        TransactionCategory defaultCategory = getOrCreateDefaultCategory();
        TransactionSourceEntity sourceEntity = getOrCreateSourceEntity(account);

        int recordsImported = processTransactionList(
                transactionResponse.getTransactions(), account, defaultCategory, sourceEntity);

        if (transactionResponse.getTotalPages() > 1) {
            recordsImported += processAdditionalPages(
                    account, dateRange, accessToken, transactionResponse.getTotalPages(),
                    defaultCategory, sourceEntity);
        }

        return recordsImported;
    }

    private AccountInformationClient.TransactionListResponse fetchFirstPage(ConnectedAccount account, DateRange dateRange, String accessToken) {
        return accountClient.getAccountTransactions(
                accessToken,
                account.getExternalAccountId(),
                dateRange.from(),
                dateRange.to(),
                1,
                100
        ).block();
    }

    private int processTransactionList(List<AccountInformationClient.Transaction> transactions, ConnectedAccount account,
                                      TransactionCategory defaultCategory,
                                      TransactionSourceEntity sourceEntity) {
        int recordsImported = 0;
        for (AccountInformationClient.Transaction ofTransaction : transactions) {
            if (importTransaction(ofTransaction, account, defaultCategory, sourceEntity)) {
                recordsImported++;
            }
        }
        return recordsImported;
    }

    private int processAdditionalPages(ConnectedAccount account, DateRange dateRange,
                                      String accessToken, int totalPages,
                                      TransactionCategory defaultCategory,
                                      TransactionSourceEntity sourceEntity) {
        int recordsImported = 0;
        for (int page = 2; page <= totalPages; page++) {
            AccountInformationClient.TransactionListResponse pageResponse = accountClient.getAccountTransactions(
                    accessToken,
                    account.getExternalAccountId(),
                    dateRange.from(),
                    dateRange.to(),
                    page,
                    100
            ).block();

            if (pageResponse != null && pageResponse.getTransactions() != null) {
                recordsImported += processTransactionList(
                        pageResponse.getTransactions(), account, defaultCategory, sourceEntity);
            }
        }
        return recordsImported;
    }

    private boolean importTransaction(AccountInformationClient.Transaction ofTransaction, ConnectedAccount account,
                                     TransactionCategory defaultCategory,
                                     TransactionSourceEntity sourceEntity) {
        try {
            if (isDuplicate(account.getUser().getId(), ofTransaction.getTransactionId())) {
                log.debug("Skipping duplicate transaction: {}", ofTransaction.getTransactionId());
                return false;
            }

            TransactionDTO transactionDTO = transactionMapper.mapToTransactionDTO(
                    ofTransaction, account, defaultCategory, sourceEntity);
            transactionService.create(transactionDTO);
            metricsService.incrementOpenFinanceTransactionImported();
            notifyRealtimeTransaction(account.getUser().getId(), transactionDTO);
            return true;
        } catch (Exception e) {
            log.error("Failed to import transaction {}: {}",
                     ofTransaction.getTransactionId(), e.getMessage());
            return false;
        }
    }

    private void notifyRealtimeTransaction(Long userId, TransactionDTO transactionDTO) {
        if (realtimeService != null) {
            try {
                realtimeService.notifyTransactionUpdate(userId, transactionDTO);
            } catch (Exception e) {
                log.warn("Failed to send realtime notification: {}", e.getMessage());
            }
        }
    }

    private void updateAccountSyncStatus(ConnectedAccount account, String status) {
        account.setLastSyncedAt(LocalDateTime.now());
        account.setSyncStatus(status);
        accountRepository.save(account);
    }

    private void handleSyncFailure(ConnectedAccount account, AccountSyncLog syncLog,
                                  String errorMessage, Long accountId) {
        syncLogHelper.updateSyncLogFailure(syncLog, errorMessage);
        updateAccountSyncStatus(account, "FAILED");
        metricsService.incrementOpenFinanceAccountSyncFailure();
        log.error("Failed to sync transactions for account {}: {}", accountId, errorMessage);
    }

    private record DateRange(LocalDateTime from, LocalDateTime to) {}

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
                    source.setSourceType(com.finance_control.shared.enums.TransactionSource.BANK_TRANSACTION);
                    source.setBankName(account.getInstitution().getName());
                    source.setAccountNumber(account.getAccountNumber());
                    source.setIsActive(true);
                    return sourceRepository.save(source);
                });
    }
}
