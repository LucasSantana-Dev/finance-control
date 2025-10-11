package com.finance_control.shared.monitoring;

import io.sentry.SentryLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simplified service for managing application metrics and monitoring.
 * Uses Sentry for error tracking and performance monitoring.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final SentryService sentryService;

    private final AtomicLong transactionCreatedCounter = new AtomicLong(0);
    private final AtomicLong transactionUpdatedCounter = new AtomicLong(0);
    private final AtomicLong transactionDeletedCounter = new AtomicLong(0);
    private final AtomicLong userLoginCounter = new AtomicLong(0);
    private final AtomicLong userRegistrationCounter = new AtomicLong(0);
    private final AtomicLong goalCreatedCounter = new AtomicLong(0);
    private final AtomicLong goalCompletedCounter = new AtomicLong(0);
    private final AtomicLong cacheHitCounter = new AtomicLong(0);
    private final AtomicLong cacheMissCounter = new AtomicLong(0);
    private final AtomicLong rateLimitExceededCounter = new AtomicLong(0);
    private final AtomicLong apiErrorCounter = new AtomicLong(0);

    private final AtomicLong activeUsersGauge = new AtomicLong(0);
    private final AtomicLong totalTransactionsGauge = new AtomicLong(0);
    private final AtomicLong activeGoalsGauge = new AtomicLong(0);
    private final AtomicLong pendingReconciliationsGauge = new AtomicLong(0);

    public void incrementTransactionCreated() {
        transactionCreatedCounter.incrementAndGet();
        log.debug("Transaction created counter incremented");
    }

    public void incrementTransactionUpdated() {
        transactionUpdatedCounter.incrementAndGet();
        log.debug("Transaction updated counter incremented");
    }

    public void incrementTransactionDeleted() {
        transactionDeletedCounter.incrementAndGet();
        log.debug("Transaction deleted counter incremented");
    }

    public Instant startTransactionProcessingTimer() {
        return Instant.now();
    }

    public void recordTransactionProcessingTime(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        if (duration.toMillis() > 1000) {
            sentryService.addBreadcrumb("Slow transaction processing: " + duration.toMillis() + "ms",
                    "performance", SentryLevel.WARNING);
        }
        log.debug("Transaction processing time: {}ms", duration.toMillis());
    }

    public void incrementUserLogin() {
        userLoginCounter.incrementAndGet();
        log.debug("User login counter incremented");
    }

    public void incrementUserRegistration() {
        userRegistrationCounter.incrementAndGet();
        log.debug("User registration counter incremented");
    }

    public void setActiveUsers(long count) {
        activeUsersGauge.set(count);
        log.debug("Active users gauge updated to: {}", count);
    }

    public Instant startAuthenticationTimer() {
        return Instant.now();
    }

    public void recordAuthenticationTime(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        if (duration.toMillis() > 500) {
            sentryService.addBreadcrumb("Slow authentication: " + duration.toMillis() + "ms", "performance");
        }
        log.debug("Authentication time: {}ms", duration.toMillis());
    }

    public void incrementGoalCreated() {
        goalCreatedCounter.incrementAndGet();
        log.debug("Goal created counter incremented");
    }

    public void incrementGoalCompleted() {
        goalCompletedCounter.incrementAndGet();
        log.debug("Goal completed counter incremented");
    }

    public void setActiveGoals(long count) {
        activeGoalsGauge.set(count);
        log.debug("Active goals gauge updated to: {}", count);
    }

    public void incrementCacheHit() {
        cacheHitCounter.incrementAndGet();
        log.debug("Cache hit counter incremented");
    }

    public void incrementCacheMiss() {
        cacheMissCounter.incrementAndGet();
        log.debug("Cache miss counter incremented");
    }

    public void incrementRateLimitExceeded() {
        rateLimitExceededCounter.incrementAndGet();
        log.warn("Rate limit exceeded counter incremented");
    }

    public void incrementApiError(String errorType) {
        apiErrorCounter.incrementAndGet();
        sentryService.addBreadcrumb("API error: " + errorType, "error", SentryLevel.ERROR);
        log.warn("API error counter incremented for type: {}", errorType);
    }

    public Instant startDashboardGenerationTimer() {
        return Instant.now();
    }

    public void recordDashboardGenerationTime(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        if (duration.toMillis() > 2000) {
            sentryService.addBreadcrumb("Slow dashboard generation: " + duration.toMillis() + "ms",
                    "performance", SentryLevel.WARNING);
        }
        log.debug("Dashboard generation time: {}ms", duration.toMillis());
    }

    public Instant startMarketDataFetchTimer() {
        return Instant.now();
    }

    public void recordMarketDataFetchTime(Instant startTime) {
        Duration duration = Duration.between(startTime, Instant.now());
        if (duration.toMillis() > 3000) {
            sentryService.addBreadcrumb("Slow market data fetch: " + duration.toMillis() + "ms",
                    "performance", SentryLevel.WARNING);
        }
        log.debug("Market data fetch time: {}ms", duration.toMillis());
    }

    public void setTotalTransactions(long count) {
        totalTransactionsGauge.set(count);
        log.debug("Total transactions gauge updated to: {}", count);
    }

    public void setPendingReconciliations(long count) {
        pendingReconciliationsGauge.set(count);
        log.debug("Pending reconciliations gauge updated to: {}", count);
    }

    public void recordTransactionAmount(double amount, String type) {
        if (amount > 10000) {
            sentryService.addBreadcrumb("Large transaction: " + amount + " for type: " + type, "business");
        }
        log.debug("Transaction amount recorded: {} for type: {}", amount, type);
    }

    public void recordGoalProgress(double progressPercentage, String goalType) {
        if (progressPercentage > 90) {
            sentryService.addBreadcrumb("Goal near completion: " + progressPercentage + "% for type: " + goalType, "business");
        }
        log.debug("Goal progress recorded: {}% for type: {}", progressPercentage, goalType);
    }

    public void recordCacheSize(String cacheName, long size) {
        if (size > 1000) {
            sentryService.addBreadcrumb("Large cache size: " + size + " for cache: " + cacheName, "performance");
        }
        log.debug("Cache size recorded: {} for cache: {}", size, cacheName);
    }

    public void recordDatabaseConnectionPool(String poolName, int active, int idle, int total) {
        double utilization = (double) active / total * 100;
        if (utilization > 80) {
            sentryService.addBreadcrumb("High DB utilization: " + utilization + "% for pool: " + poolName, "performance");
        }
        log.debug("Database pool metrics recorded for pool: {} - active: {}, idle: {}, total: {}",
                poolName, active, idle, total);
    }
    public void captureException(Exception exception, String context) {
        sentryService.captureException(exception);
        sentryService.setTags(Map.of("context", context));
        log.error("Exception captured in Sentry: {}", exception.getMessage());
    }

    public void captureMessage(String message, SentryLevel level) {
        sentryService.captureMessage(message);
        log.info("Message captured in Sentry: {}", message);
    }

    public void addUserContext(String userId, String email) {
        sentryService.setUser(userId, email);
    }

    public void addBreadcrumb(String message, String category) {
        sentryService.addBreadcrumb(message, category);
    }
}
