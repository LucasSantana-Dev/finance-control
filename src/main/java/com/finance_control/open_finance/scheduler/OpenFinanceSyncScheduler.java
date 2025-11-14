package com.finance_control.open_finance.scheduler;

import com.finance_control.open_finance.service.OpenFinanceAccountService;
import com.finance_control.open_finance.service.OpenFinanceConsentService;
import com.finance_control.open_finance.service.OpenFinanceTransactionSyncService;
import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for Open Finance synchronization tasks.
 * Handles periodic balance sync, transaction sync, and token refresh.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.open-finance.enabled", havingValue = "true", matchIfMissing = false)
public class OpenFinanceSyncScheduler {

    private final OpenFinanceAccountService accountService;
    private final OpenFinanceTransactionSyncService transactionSyncService;
    private final OpenFinanceConsentService consentService;
    private final AppProperties appProperties;

    /**
     * Synchronizes account balances periodically.
     * Runs every 15 minutes by default (configurable).
     */
    @Scheduled(fixedDelayString = "${app.open-finance.sync.balance-sync-interval-ms:900000}",
               initialDelay = 60000)
    public void syncBalances() {
        if (!appProperties.openFinance().sync().enabled()) {
            log.debug("Open Finance sync is disabled, skipping balance sync");
            return;
        }

        try {
            log.info("Starting scheduled balance synchronization");
            accountService.syncAllBalances();
            log.info("Completed scheduled balance synchronization");
        } catch (Exception e) {
            log.error("Error during scheduled balance synchronization", e);
        }
    }

    /**
     * Synchronizes transactions periodically.
     * Runs daily by default (configurable).
     */
    @Scheduled(cron = "${app.open-finance.sync.transaction-sync-cron:0 0 2 * * ?}")
    public void syncTransactions() {
        if (!appProperties.openFinance().sync().enabled()) {
            log.debug("Open Finance sync is disabled, skipping transaction sync");
            return;
        }

        try {
            log.info("Starting scheduled transaction synchronization");
            transactionSyncService.syncAllTransactions();
            log.info("Completed scheduled transaction synchronization");
        } catch (Exception e) {
            log.error("Error during scheduled transaction synchronization", e);
        }
    }

    /**
     * Refreshes expiring tokens periodically.
     * Runs every hour to check for tokens expiring soon.
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void refreshExpiringTokens() {
        if (!appProperties.openFinance().sync().enabled()) {
            log.debug("Open Finance sync is disabled, skipping token refresh");
            return;
        }

        try {
            log.info("Starting scheduled token refresh");
            consentService.refreshExpiringTokens();
            log.info("Completed scheduled token refresh");
        } catch (Exception e) {
            log.error("Error during scheduled token refresh", e);
        }
    }
}
