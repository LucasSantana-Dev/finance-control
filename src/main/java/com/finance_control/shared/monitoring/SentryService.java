package com.finance_control.shared.monitoring;

import io.sentry.Sentry;
import io.sentry.SentryLevel;
import io.sentry.protocol.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

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
     * Capture an exception with optional context.
     * 
     * @param throwable The exception to capture
     * @param context Additional context information
     */
    public void captureException(Throwable throwable, Map<String, Object> context) {
        try {
            if (context != null && !context.isEmpty()) {
                context.forEach(Sentry::setExtra);
            }
            Sentry.captureException(throwable);
            log.debug("Exception captured by Sentry: {}", throwable.getMessage());
        } catch (Exception e) {
            log.error("Failed to capture exception in Sentry", e);
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
        } catch (Exception e) {
            log.error("Failed to capture exception in Sentry", e);
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
            Sentry.captureMessage(message, level);
            log.debug("Message captured by Sentry: {}", message);
        } catch (Exception e) {
            log.error("Failed to capture message in Sentry", e);
        }
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
     * @param level The breadcrumb level
     */
    public void addBreadcrumb(String message, String category, SentryLevel level) {
        try {
            Sentry.addBreadcrumb(message, category, level);
            log.debug("Breadcrumb added: {} - {}", category, message);
        } catch (Exception e) {
            log.error("Failed to add breadcrumb to Sentry", e);
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
        } catch (Exception e) {
            log.error("Failed to set user context in Sentry", e);
        }
    }

    /**
     * Set custom tags for error tracking.
     * 
     * @param tags Map of tags to set
     */
    public void setTags(Map<String, String> tags) {
        try {
            if (tags != null && !tags.isEmpty()) {
                tags.forEach(Sentry::setTag);
                log.debug("Tags set for Sentry: {}", tags);
            }
        } catch (Exception e) {
            log.error("Failed to set tags in Sentry", e);
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
            Sentry.setExtra(key, value);
            log.debug("Context set for Sentry: {} = {}", key, value);
        } catch (Exception e) {
            log.error("Failed to set context in Sentry", e);
        }
    }

    /**
     * Clear user context.
     */
    public void clearUserContext() {
        try {
            Sentry.clearUser();
            log.debug("User context cleared in Sentry");
        } catch (Exception e) {
            log.error("Failed to clear user context in Sentry", e);
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
            log.error("Failed to check Sentry status", e);
            return false;
        }
    }
}
