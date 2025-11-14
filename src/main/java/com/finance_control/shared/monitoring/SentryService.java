package com.finance_control.shared.monitoring;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

import io.sentry.ISpan;
import io.sentry.SpanStatus;

/**
 * Service for Sentry integration providing centralized error tracking and monitoring.
 *
 * This service provides a clean interface for sending errors, exceptions, and
 * custom events to Sentry with proper context and user information.
 */
@Slf4j
@Service
public class SentryService {

    /**
     * Check if Sentry is enabled and properly configured.
     *
     * @return true if Sentry is enabled, false otherwise
     */
    private boolean isSentryEnabled() {
        try {
            return Sentry.isEnabled();
        } catch (Exception e) {
            log.debug("Sentry not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Capture an exception.
     *
     * @param throwable The exception to capture
     */
    public void captureException(Throwable throwable) {
        try {
            if (isSentryEnabled()) {
                Sentry.captureException(throwable);
                log.debug("Exception captured by Sentry: {}", throwable.getMessage());
            } else {
                log.debug("Sentry not enabled, skipping exception capture: {}", throwable.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to capture exception in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Capture an exception with optional context.
     *
     * @param throwable The exception to capture
     * @param context Additional context information
     */
    public void captureException(Throwable throwable, Map<String, Object> context) {
        try {
            if (isSentryEnabled()) {
                if (context != null && !context.isEmpty()) {
                    context.forEach((key, value) -> Sentry.setExtra(key, value.toString()));
                }
                Sentry.captureException(throwable);
                log.debug("Exception captured by Sentry: {}", throwable.getMessage());
            } else {
                log.debug("Sentry not enabled, skipping exception capture: {}", throwable.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to capture exception in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Capture an exception with user context.
     *
     * @param throwable The exception to capture
     * @param userId The user ID associated with the error
     * @param userEmail The user email associated with the error
     */
    public void captureException(Throwable throwable, Long userId, String userEmail) {
        try {
            if (isSentryEnabled()) {
                if (userId != null || userEmail != null) {
                    User user = new User();
                    if (userId != null) {
                        user.setId(userId.toString());
                    }
                    if (userEmail != null) {
                        user.setEmail(userEmail);
                    }
                    Sentry.setUser(user);
                }
                Sentry.captureException(throwable);
                log.debug("Exception captured by Sentry for user {}: {}", userId, throwable.getMessage());
            } else {
                log.debug("Sentry not enabled, skipping exception capture for user {}: {}", userId, throwable.getMessage());
            }
        } catch (Exception e) {
            log.warn("Failed to capture exception in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Capture a message.
     *
     * @param message The message to capture
     */
    public void captureMessage(String message) {
        try {
            if (isSentryEnabled()) {
                Sentry.captureMessage(message);
                log.debug("Message captured by Sentry: {}", message);
            } else {
                log.debug("Sentry not enabled, skipping message capture: {}", message);
            }
        } catch (Exception e) {
            log.warn("Failed to capture message in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Capture a message with specified level.
     *
     * @param message The message to capture
     * @param level The severity level
     */
    public void captureMessage(String message, SentryLevel level) {
        try {
            if (isSentryEnabled()) {
                Sentry.captureMessage(message, level);
                log.debug("Message captured by Sentry: {}", message);
            } else {
                log.debug("Sentry not enabled, skipping message capture: {}", message);
            }
        } catch (Exception e) {
            log.warn("Failed to capture message in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Capture a message and return the resulting Sentry event id.
     *
     * @param message message to capture
     * @param level severity level
     * @return event id or null when Sentry disabled
     */
    public String captureMessageWithResponse(String message, SentryLevel level) {
        try {
            if (isSentryEnabled()) {
                var sentryId = Sentry.captureMessage(message, level);
                return sentryId != null ? sentryId.toString() : null;
            }
        } catch (Exception e) {
            log.warn("Failed to capture message in Sentry: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Capture an error message.
     *
     * @param message The error message
     */
    public void captureError(String message) {
        captureMessage(message, SentryLevel.ERROR);
    }

    /**
     * Capture a warning message.
     *
     * @param message The warning message
     */
    public void captureWarning(String message) {
        captureMessage(message, SentryLevel.WARNING);
    }

    /**
     * Capture an info message.
     *
     * @param message The info message
     */
    public void captureInfo(String message) {
        captureMessage(message, SentryLevel.INFO);
    }

    /**
     * Add breadcrumb for tracking user actions.
     *
     * @param message The breadcrumb message
     * @param category The breadcrumb category
     */
    public void addBreadcrumb(String message, String category) {
        try {
            if (isSentryEnabled()) {
                Sentry.addBreadcrumb(message, category);
                log.debug("Breadcrumb added: {} - {}", category, message);
            } else {
                log.debug("Sentry not enabled, skipping breadcrumb: {} - {}", category, message);
            }
        } catch (Exception e) {
            log.warn("Failed to add breadcrumb to Sentry: {}", e.getMessage());
        }
    }

    /**
     * Add breadcrumb for tracking user actions.
     *
     * @param message The breadcrumb message
     * @param category The breadcrumb category
     * @param level The breadcrumb level
     */
    public void addBreadcrumb(String message, String category, SentryLevel level) {
        try {
            if (isSentryEnabled()) {
                Sentry.addBreadcrumb(message, category);
                log.debug("Breadcrumb added: {} - {}", category, message);
            } else {
                log.debug("Sentry not enabled, skipping breadcrumb: {} - {}", category, message);
            }
        } catch (Exception e) {
            log.warn("Failed to add breadcrumb to Sentry: {}", e.getMessage());
        }
    }

    /**
     * Set user context for error tracking.
     *
     * @param userId The user ID
     * @param userEmail The user email
     */
    public void setUser(String userId, String userEmail) {
        try {
            if (isSentryEnabled()) {
                User user = new User();
                if (userId != null) {
                    user.setId(userId);
                }
                if (userEmail != null) {
                    user.setEmail(userEmail);
                }
                Sentry.setUser(user);
                log.debug("User context set for Sentry: {}", userId);
            } else {
                log.debug("Sentry not enabled, skipping user context setting: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Failed to set user context in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Set user context for error tracking.
     *
     * @param userId The user ID
     * @param userEmail The user email
     * @param username The username
     */
    public void setUserContext(Long userId, String userEmail, String username) {
        try {
            if (isSentryEnabled()) {
                User user = new User();
                if (userId != null) {
                    user.setId(userId.toString());
                }
                if (userEmail != null) {
                    user.setEmail(userEmail);
                }
                if (username != null) {
                    user.setUsername(username);
                }
                Sentry.setUser(user);
                log.debug("User context set for Sentry: {}", userId);
            } else {
                log.debug("Sentry not enabled, skipping user context setting: {}", userId);
            }
        } catch (Exception e) {
            log.warn("Failed to set user context in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Set custom tags for error tracking.
     *
     * @param tags Map of tags to set
     */
    public void setTags(Map<String, String> tags) {
        try {
            if (isSentryEnabled() && tags != null && !tags.isEmpty()) {
                tags.forEach(Sentry::setTag);
                log.debug("Tags set for Sentry: {}", tags);
            } else {
                log.debug("Sentry not enabled, skipping tags setting: {}", tags);
            }
        } catch (Exception e) {
            log.warn("Failed to set tags in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Set custom context data.
     *
     * @param key The context key
     * @param value The context value
     */
    public void setContext(String key, Object value) {
        try {
            if (isSentryEnabled()) {
                Sentry.setExtra(key, value.toString());
                log.debug("Context set for Sentry: {} = {}", key, value);
            } else {
                log.debug("Sentry not enabled, skipping context setting: {} = {}", key, value);
            }
        } catch (Exception e) {
            log.warn("Failed to set context in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Clear user context.
     */
    public void clearUserContext() {
        try {
            if (isSentryEnabled()) {
                Sentry.setUser(null);
                log.debug("User context cleared in Sentry");
            } else {
                log.debug("Sentry not enabled, skipping user context clearing");
            }
        } catch (Exception e) {
            log.warn("Failed to clear user context in Sentry: {}", e.getMessage());
        }
    }

    /**
     * Check if Sentry is enabled and configured.
     *
     * @return true if Sentry is enabled
     */
    public boolean isEnabled() {
        try {
            return Sentry.isEnabled();
        } catch (Exception e) {
            log.warn("Failed to check Sentry status: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Execute a supplier function with performance monitoring.
     * Automatically tracks execution time and captures slow operations.
     *
     * @param operationName The name of the operation being monitored
     * @param slowThresholdMs Threshold in milliseconds to consider operation slow
     * @param supplier The function to execute
     * @param <T> The return type
     * @return The result of the supplier function
     */
    public <T> T executeWithMonitoring(String operationName, long slowThresholdMs, Supplier<T> supplier) {
        Instant start = Instant.now();
        ISpan span = null;

        try {
            if (isSentryEnabled()) {
                span = Sentry.startTransaction(operationName, "operation");
                addBreadcrumb("Starting operation: " + operationName, "performance");
            }

            T result = supplier.get();

            Duration duration = Duration.between(start, Instant.now());
            long durationMs = duration.toMillis();

            if (isSentryEnabled()) {
                if (span != null) {
                    span.setStatus(SpanStatus.OK);
                    span.finish();
                }

                if (durationMs > slowThresholdMs) {
                    captureWarning(String.format("Slow operation detected: %s took %dms (threshold: %dms)",
                        operationName, durationMs, slowThresholdMs));
                    setTags(Map.of(
                        "operation", operationName,
                        "duration_ms", String.valueOf(durationMs),
                        "slow", "true"
                    ));
                } else {
                    addBreadcrumb(
                        String.format("Operation completed: %s took %dms", operationName, durationMs),
                        "performance",
                        SentryLevel.INFO
                    );
                }
            }

            return result;
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            if (isSentryEnabled()) {
                if (span != null) {
                    span.setStatus(SpanStatus.INTERNAL_ERROR);
                    span.setThrowable(e);
                    span.finish();
                }
                captureException(e, Map.of(
                    "operation", operationName,
                    "duration_ms", String.valueOf(duration.toMillis())
                ));
            }
            throw e;
        }
    }

    /**
     * Execute a runnable with performance monitoring.
     * Automatically tracks execution time and captures slow operations.
     *
     * @param operationName The name of the operation being monitored
     * @param slowThresholdMs Threshold in milliseconds to consider operation slow
     * @param runnable The function to execute
     */
    public void executeWithMonitoring(String operationName, long slowThresholdMs, Runnable runnable) {
        executeWithMonitoring(operationName, slowThresholdMs, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Start a Sentry transaction for performance monitoring.
     *
     * @param operationName The name of the operation
     * @param operationType The type of operation (e.g., "db.query", "http.request")
     * @return The transaction span, or null if Sentry is disabled
     */
    public ISpan startTransaction(String operationName, String operationType) {
        try {
            if (isSentryEnabled()) {
                return Sentry.startTransaction(operationName, operationType);
            }
        } catch (Exception e) {
            log.warn("Failed to start Sentry transaction: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Finish a Sentry transaction with status.
     *
     * @param span The transaction span to finish
     * @param status The status of the transaction
     */
    public void finishTransaction(ISpan span, SpanStatus status) {
        try {
            if (span != null && isSentryEnabled()) {
                span.setStatus(status);
                span.finish();
            }
        } catch (Exception e) {
            log.warn("Failed to finish Sentry transaction: {}", e.getMessage());
        }
    }

    /**
     * Finish a Sentry transaction with exception.
     *
     * @param span The transaction span to finish
     * @param throwable The exception that occurred
     */
    public void finishTransaction(ISpan span, Throwable throwable) {
        try {
            if (span != null && isSentryEnabled()) {
                span.setStatus(SpanStatus.INTERNAL_ERROR);
                span.setThrowable(throwable);
                span.finish();
            }
        } catch (Exception e) {
            log.warn("Failed to finish Sentry transaction: {}", e.getMessage());
        }
    }
}
