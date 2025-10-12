package com.finance_control.unit.shared.monitoring;

import com.finance_control.shared.monitoring.MetricsService;
import com.finance_control.shared.monitoring.SentryService;
import io.sentry.Sentry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsService Unit Tests")
class MetricsServiceTest {

    @Mock
    private SentryService sentryService;

    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsService(sentryService);
    }

    @Test
    @DisplayName("Should increment transaction created counter")
    void incrementTransactionCreated_ShouldIncrementCounter() {
        metricsService.incrementTransactionCreated();
    }

    @Test
    @DisplayName("Should increment transaction updated counter")
    void incrementTransactionUpdated_ShouldIncrementCounter() {
        metricsService.incrementTransactionUpdated();
    }

    @Test
    @DisplayName("Should increment transaction deleted counter")
    void incrementTransactionDeleted_ShouldIncrementCounter() {
        metricsService.incrementTransactionDeleted();
    }

    @Test
    @DisplayName("Should record transaction processing time")
    void recordTransactionProcessingTime_ShouldRecordTime() {
        Instant startTime = Instant.now().minus(Duration.ofMillis(1500));
        metricsService.recordTransactionProcessingTime(startTime);
    }

    @Test
    @DisplayName("Should record slow transaction processing time with Sentry breadcrumb")
    void recordTransactionProcessingTime_WithSlowTransaction_ShouldAddSentryBreadcrumb() {
        Instant startTime = Instant.now().minus(Duration.ofMillis(1500));

        metricsService.recordTransactionProcessingTime(startTime);
        verify(sentryService).addBreadcrumb(anyString(), eq("performance"), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should increment user login counter")
    void incrementUserLogin_ShouldIncrementCounter() {
        // When
        metricsService.incrementUserLogin();

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should increment user registration counter")
    void incrementUserRegistration_ShouldIncrementCounter() {
        // When
        metricsService.incrementUserRegistration();

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should record authentication time")
    void recordAuthenticationTime_ShouldRecordTime() {
        // Given
        Instant startTime = Instant.now().minus(Duration.ofMillis(600));

        // When
        metricsService.recordAuthenticationTime(startTime);

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should record slow authentication time with Sentry breadcrumb")
    void recordAuthenticationTime_WithSlowAuthentication_ShouldAddSentryBreadcrumb() {
        // Given
        Instant startTime = Instant.now().minus(Duration.ofMillis(600));

        // When
        metricsService.recordAuthenticationTime(startTime);

        // Then
        verify(sentryService).addBreadcrumb(anyString(), eq("performance"));
    }

    @Test
    @DisplayName("Should increment goal created counter")
    void incrementGoalCreated_ShouldIncrementCounter() {
        // When
        metricsService.incrementGoalCreated();

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should increment goal completed counter")
    void incrementGoalCompleted_ShouldIncrementCounter() {
        // When
        metricsService.incrementGoalCompleted();

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should increment cache hit counter")
    void incrementCacheHit_ShouldIncrementCounter() {
        // When
        metricsService.incrementCacheHit();

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should increment cache miss counter")
    void incrementCacheMiss_ShouldIncrementCounter() {
        // When
        metricsService.incrementCacheMiss();

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should increment rate limit exceeded counter")
    void incrementRateLimitExceeded_ShouldIncrementCounter() {
        // When
        metricsService.incrementRateLimitExceeded();

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should increment API error counter")
    void incrementApiError_ShouldIncrementCounter() {
        // Given
        String errorType = "VALIDATION_ERROR";

        // When
        metricsService.incrementApiError(errorType);

        // Then
        verify(sentryService).addBreadcrumb(anyString(), eq("error"), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should record dashboard generation time")
    void recordDashboardGenerationTime_ShouldRecordTime() {
        // Given
        Instant startTime = Instant.now().minus(Duration.ofMillis(2500));

        // When
        metricsService.recordDashboardGenerationTime(startTime);

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should record slow dashboard generation time with Sentry breadcrumb")
    void recordDashboardGenerationTime_WithSlowGeneration_ShouldAddSentryBreadcrumb() {
        // Given
        Instant startTime = Instant.now().minus(Duration.ofMillis(2500));

        // When
        metricsService.recordDashboardGenerationTime(startTime);

        // Then
        verify(sentryService).addBreadcrumb(anyString(), eq("performance"), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should record market data fetch time")
    void recordMarketDataFetchTime_ShouldRecordTime() {
        // Given
        Instant startTime = Instant.now().minus(Duration.ofMillis(3500));

        // When
        metricsService.recordMarketDataFetchTime(startTime);

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should record slow market data fetch time with Sentry breadcrumb")
    void recordMarketDataFetchTime_WithSlowFetch_ShouldAddSentryBreadcrumb() {
        // Given
        Instant startTime = Instant.now().minus(Duration.ofMillis(3500));

        // When
        metricsService.recordMarketDataFetchTime(startTime);

        // Then
        verify(sentryService).addBreadcrumb(anyString(), eq("performance"), any(io.sentry.SentryLevel.class));
    }

    @Test
    @DisplayName("Should set active users gauge")
    void setActiveUsers_ShouldSetGauge() {
        // Given
        long userCount = 150L;

        // When
        metricsService.setActiveUsers(userCount);

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should set active goals gauge")
    void setActiveGoals_ShouldSetGauge() {
        // Given
        long goalCount = 25L;

        // When
        metricsService.setActiveGoals(goalCount);

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should set total transactions gauge")
    void setTotalTransactions_ShouldSetGauge() {
        // Given
        long transactionCount = 1000L;

        // When
        metricsService.setTotalTransactions(transactionCount);

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should set pending reconciliations gauge")
    void setPendingReconciliations_ShouldSetGauge() {
        // Given
        long reconciliationCount = 50L;

        // When
        metricsService.setPendingReconciliations(reconciliationCount);

        // Then - No exceptions should be thrown
    }

    @Test
    @DisplayName("Should record large transaction amount with Sentry breadcrumb")
    void recordTransactionAmount_WithLargeAmount_ShouldAddSentryBreadcrumb() {
        // Given
        double largeAmount = 15000.0;
        String type = "INCOME";

        // When
        metricsService.recordTransactionAmount(largeAmount, type);

        // Then
        verify(sentryService).addBreadcrumb(anyString(), eq("business"));
    }

    @Test
    @DisplayName("Should record near-completion goal progress with Sentry breadcrumb")
    void recordGoalProgress_WithNearCompletion_ShouldAddSentryBreadcrumb() {
        // Given
        double progressPercentage = 95.0;
        String goalType = "SAVINGS";

        // When
        metricsService.recordGoalProgress(progressPercentage, goalType);

        // Then
        verify(sentryService).addBreadcrumb(anyString(), eq("business"));
    }

    @Test
    @DisplayName("Should record large cache size with Sentry breadcrumb")
    void recordCacheSize_WithLargeSize_ShouldAddSentryBreadcrumb() {
        // Given
        String cacheName = "dashboard";
        long largeSize = 1500L;

        // When
        metricsService.recordCacheSize(cacheName, largeSize);

        // Then
        verify(sentryService).addBreadcrumb(anyString(), eq("performance"));
    }

    @Test
    @DisplayName("Should record high database utilization with Sentry breadcrumb")
    void recordDatabaseConnectionPool_WithHighUtilization_ShouldAddSentryBreadcrumb() {
        // Given
        String poolName = "main";
        int active = 9;
        int idle = 1;
        int total = 10;

        // When
        metricsService.recordDatabaseConnectionPool(poolName, active, idle, total);

        // Then
        verify(sentryService).addBreadcrumb(anyString(), eq("performance"));
    }

    @Test
    @DisplayName("Should capture exception with Sentry")
    void captureException_ShouldCaptureInSentry() {
        // Given
        Exception exception = new RuntimeException("Test exception");
        String context = "test_context";

        // When
        metricsService.captureException(exception, context);

        // Then
        verify(sentryService).captureException(exception);
        verify(sentryService).setTags(any(Map.class));
    }

    @Test
    @DisplayName("Should capture message with Sentry")
    void captureMessage_ShouldCaptureInSentry() {
        // Given
        String message = "Test message";
        io.sentry.SentryLevel level = io.sentry.SentryLevel.INFO;

        // When
        metricsService.captureMessage(message, level);

        // Then
        verify(sentryService).captureMessage(message);
    }

    @Test
    @DisplayName("Should add user context to Sentry")
    void addUserContext_ShouldAddToSentry() {
        // Given
        String userId = "123";
        String email = "test@example.com";

        // When
        metricsService.addUserContext(userId, email);

        // Then
        verify(sentryService).setUser(userId, email);
    }

    @Test
    @DisplayName("Should add breadcrumb to Sentry")
    void addBreadcrumb_ShouldAddToSentry() {
        // Given
        String message = "Test breadcrumb";
        String category = "test";

        // When
        metricsService.addBreadcrumb(message, category);

        // Then
        verify(sentryService).addBreadcrumb(message, category);
    }
}
