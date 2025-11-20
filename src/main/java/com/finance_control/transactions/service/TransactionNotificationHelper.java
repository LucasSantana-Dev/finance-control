package com.finance_control.transactions.service;

import com.finance_control.dashboard.service.DashboardService;
import com.finance_control.shared.monitoring.SentryService;
import com.finance_control.shared.service.SupabaseRealtimeService;
import com.finance_control.transactions.dto.TransactionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Helper class for handling transaction-related notifications.
 * Reduces coupling and code duplication in TransactionService.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionNotificationHelper {

    private final SentryService sentryService;

    @Autowired(required = false)
    private SupabaseRealtimeService realtimeService;

    @Autowired(required = false)
    private DashboardService dashboardService;

    /**
     * Sends all notifications for a transaction operation.
     *
     * @param transaction the transaction DTO
     * @param operation   the operation type (e.g., "creation", "update", "deletion")
     */
    public void notifyTransactionChange(TransactionDTO transaction, String operation) {
        notifyRealtime(transaction, operation);
        notifyDashboard(transaction, operation);
    }

    private void notifyRealtime(TransactionDTO transaction, String operation) {
        if (realtimeService == null) {
            return;
        }

        try {
            realtimeService.notifyTransactionUpdate(transaction.getUserId(), transaction);
            log.debug("Sent realtime notification for transaction {}: {}", operation, transaction.getId());
        } catch (Exception e) {
            log.warn("Failed to send realtime notification for transaction {}: {}", operation, e.getMessage());
            sentryService.captureException(e, Map.of(
                    "operation", "realtime_notification",
                    "transaction_id", transaction.getId().toString(),
                    "user_id", transaction.getUserId().toString()
            ));
        }
    }

    private void notifyDashboard(TransactionDTO transaction, String operation) {
        if (dashboardService == null) {
            return;
        }

        try {
            dashboardService.notifyDashboardUpdate(transaction.getUserId());
            log.debug("Sent dashboard update notification for transaction {}: {}", operation, transaction.getId());
        } catch (Exception e) {
            log.warn("Failed to send dashboard update notification for transaction {}: {}", operation, e.getMessage());
            sentryService.captureException(e, Map.of(
                    "operation", "dashboard_notification",
                    "transaction_id", transaction.getId().toString(),
                    "user_id", transaction.getUserId().toString()
            ));
        }
    }
}



