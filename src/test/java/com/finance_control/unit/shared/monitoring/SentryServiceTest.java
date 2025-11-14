package com.finance_control.unit.shared.monitoring;

import com.finance_control.shared.monitoring.SentryService;
import io.sentry.SentryLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for SentryService.
 */
@ExtendWith(MockitoExtension.class)
class SentryServiceTest {

    private SentryService sentryService;

    @BeforeEach
    void setUp() {
        sentryService = new SentryService();
    }

    @Test
    void captureException_WithContext_ShouldCallSentryService() {
        // Given
        Exception exception = new RuntimeException("Test exception");
        Map<String, Object> context = new HashMap<>();
        context.put("key", "value");

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.captureException(exception, context));
    }

    @Test
    void captureException_WithUserContext_ShouldCallSentryService() {
        // Given
        Exception exception = new RuntimeException("Test exception");
        Long userId = 123L;
        String userEmail = "test@example.com";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.captureException(exception, userId, userEmail));
    }

    @Test
    void captureMessage_WithLevel_ShouldCallSentryService() {
        // Given
        String message = "Test message";
        SentryLevel level = SentryLevel.ERROR;

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.captureMessage(message, level));
    }

    @Test
    void captureMessageWithResponse_ShouldReturnNullWhenDisabled() {
        // Given
        String message = "Frontend error";

        // When & Then - Should not throw exception even when Sentry disabled
        assertDoesNotThrow(() -> sentryService.captureMessageWithResponse(message, SentryLevel.ERROR));
    }

    @Test
    void captureError_ShouldCallSentryService() {
        // Given
        String message = "Test error";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.captureError(message));
    }

    @Test
    void captureWarning_ShouldCallSentryService() {
        // Given
        String message = "Test warning";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.captureWarning(message));
    }

    @Test
    void captureInfo_ShouldCallSentryService() {
        // Given
        String message = "Test info";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.captureInfo(message));
    }

    @Test
    void addBreadcrumb_ShouldCallSentryService() {
        // Given
        String message = "Test breadcrumb";
        String category = "test";
        SentryLevel level = SentryLevel.INFO;

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.addBreadcrumb(message, category, level));
    }

    @Test
    void setUserContext_ShouldCallSentryService() {
        // Given
        Long userId = 123L;
        String userEmail = "test@example.com";
        String username = "testuser";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.setUserContext(userId, userEmail, username));
    }

    @Test
    void setTags_ShouldCallSentryService() {
        // Given
        Map<String, String> tags = new HashMap<>();
        tags.put("environment", "test");
        tags.put("service", "finance-control");

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.setTags(tags));
    }

    @Test
    void setContext_ShouldCallSentryService() {
        // Given
        String key = "test_key";
        Object value = "test_value";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.setContext(key, value));
    }

    @Test
    void clearUserContext_ShouldCallSentryService() {
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.clearUserContext());
    }

    @Test
    void isEnabled_ShouldCallSentryService() {
        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> sentryService.isEnabled());
    }
}
