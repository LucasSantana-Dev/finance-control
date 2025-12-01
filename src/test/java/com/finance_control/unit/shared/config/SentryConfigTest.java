package com.finance_control.unit.shared.config;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.properties.MonitoringProperties;
import com.finance_control.shared.config.SentryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SentryConfig.
 */
@ExtendWith(MockitoExtension.class)
class SentryConfigTest {

    @Mock
    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        MonitoringProperties monitoring = new MonitoringProperties(
            true,
            new MonitoringProperties.SentryProperties(true, "https://test@sentry.io/test", "test", "1.0.0", 0.1, 0.1, false, true, true)
        );
        when(appProperties.monitoring()).thenReturn(monitoring);
    }

    @Test
    void customizeSentryOptions_WithValidDsn_ShouldCustomizeSentry() {
        // Given
        SentryConfig sentryConfig = new SentryConfig(appProperties);

        try (MockedStatic<io.sentry.Sentry> sentryMock = Mockito.mockStatic(io.sentry.Sentry.class)) {
            // Mock Sentry.isEnabled() to return true
            sentryMock.when(() -> io.sentry.Sentry.isEnabled()).thenReturn(true);
            // Mock Sentry.getCurrentHub() and getOptions()
            io.sentry.IHub mockHub = Mockito.mock(io.sentry.IHub.class);
            io.sentry.SentryOptions mockOptions = Mockito.mock(io.sentry.SentryOptions.class);
            sentryMock.when(() -> io.sentry.Sentry.getCurrentHub()).thenReturn(mockHub);
            when(mockHub.getOptions()).thenReturn(mockOptions);

            // When - Customize Sentry options (should not throw exception)
            sentryConfig.customizeSentryOptions();

            // Then - Verify that customization completed successfully
            // The method should execute without exception when Sentry is enabled and options are available
        }
    }

    @Test
    void customizeSentryOptions_WithEmptyDsn_ShouldNotCustomizeSentry() {
        // Given
        MonitoringProperties monitoring = new MonitoringProperties(
            true,
            new MonitoringProperties.SentryProperties(true, "", "test", "1.0.0", 0.1, 0.1, false, true, true)
        );
        when(appProperties.monitoring()).thenReturn(monitoring);
        SentryConfig sentryConfig = new SentryConfig(appProperties);

        try (MockedStatic<io.sentry.Sentry> sentryMock = Mockito.mockStatic(io.sentry.Sentry.class)) {
            // When
            sentryConfig.customizeSentryOptions();

            // Then - Should return early without accessing Sentry
            sentryMock.verify(() -> io.sentry.Sentry.getCurrentHub(), never());
        }
    }

    @Test
    void customizeSentryOptions_WithNullDsn_ShouldNotCustomizeSentry() {
        // Given
        MonitoringProperties monitoring = new MonitoringProperties(
            true,
            new MonitoringProperties.SentryProperties(true, null, "test", "1.0.0", 0.1, 0.1, false, true, true)
        );
        when(appProperties.monitoring()).thenReturn(monitoring);
        SentryConfig sentryConfig = new SentryConfig(appProperties);

        try (MockedStatic<io.sentry.Sentry> sentryMock = Mockito.mockStatic(io.sentry.Sentry.class)) {
            // When
            sentryConfig.customizeSentryOptions();

            // Then - Should return early without accessing Sentry
            sentryMock.verify(() -> io.sentry.Sentry.getCurrentHub(), never());
        }
    }
}
