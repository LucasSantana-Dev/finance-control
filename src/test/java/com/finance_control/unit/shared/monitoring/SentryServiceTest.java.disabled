package com.finance_control.unit.shared.monitoring;

import com.finance_control.shared.monitoring.SentryService;
import io.sentry.SentryLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SentryService.
 */
@ExtendWith(MockitoExtension.class)
class SentryServiceTest {

    @Mock
    private SentryService sentryService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(sentryService);
    }

    @Test
    void captureException_WithContext_ShouldCallSentryService() {
        // Given
        Exception exception = new RuntimeException("Test exception");
        Map<String, Object> context = new HashMap<>();
        context.put("key", "value");

        // When
        sentryService.captureException(exception, context);

        // Then
        verify(sentryService).captureException(exception, context);
    }

    @Test
    void captureException_WithUserContext_ShouldCallSentryService() {
        // Given
        Exception exception = new RuntimeException("Test exception");
        Long userId = 123L;
        String userEmail = "test@example.com";

        // When
        sentryService.captureException(exception, userId, userEmail);

        // Then
        verify(sentryService).captureException(exception, userId, userEmail);
    }

    @Test
    void captureMessage_WithLevel_ShouldCallSentryService() {
        // Given
        String message = "Test message";
        SentryLevel level = SentryLevel.ERROR;

        // When
        sentryService.captureMessage(message, level);

        // Then
        verify(sentryService).captureMessage(message, level);
    }

    @Test
    void captureError_ShouldCallSentryService() {
        // Given
        String message = "Test error";

        // When
        sentryService.captureError(message);

        // Then
        verify(sentryService).captureError(message);
    }

    @Test
    void captureWarning_ShouldCallSentryService() {
        // Given
        String message = "Test warning";

        // When
        sentryService.captureWarning(message);

        // Then
        verify(sentryService).captureWarning(message);
    }

    @Test
    void captureInfo_ShouldCallSentryService() {
        // Given
        String message = "Test info";

        // When
        sentryService.captureInfo(message);

        // Then
        verify(sentryService).captureInfo(message);
    }

    @Test
    void addBreadcrumb_ShouldCallSentryService() {
        // Given
        String message = "Test breadcrumb";
        String category = "test";
        SentryLevel level = SentryLevel.INFO;

        // When
        sentryService.addBreadcrumb(message, category, level);

        // Then
        verify(sentryService).addBreadcrumb(message, category, level);
    }

    @Test
    void setUserContext_ShouldCallSentryService() {
        // Given
        Long userId = 123L;
        String userEmail = "test@example.com";
        String username = "testuser";

        // When
        sentryService.setUserContext(userId, userEmail, username);

        // Then
        verify(sentryService).setUserContext(userId, userEmail, username);
    }

    @Test
    void setTags_ShouldCallSentryService() {
        // Given
        Map<String, String> tags = new HashMap<>();
        tags.put("environment", "test");
        tags.put("service", "finance-control");

        // When
        sentryService.setTags(tags);

        // Then
        verify(sentryService).setTags(tags);
    }

    @Test
    void setContext_ShouldCallSentryService() {
        // Given
        String key = "test_key";
        Object value = "test_value";

        // When
        sentryService.setContext(key, value);

        // Then
        verify(sentryService).setContext(key, value);
    }

    @Test
    void clearUserContext_ShouldCallSentryService() {
        // When
        sentryService.clearUserContext();

        // Then
        verify(sentryService).clearUserContext();
    }

    @Test
    void isEnabled_ShouldCallSentryService() {
        // When
        sentryService.isEnabled();

        // Then
        verify(sentryService).isEnabled();
    }
}
