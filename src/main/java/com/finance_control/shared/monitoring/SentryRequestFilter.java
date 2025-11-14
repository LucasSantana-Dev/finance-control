package com.finance_control.shared.monitoring;

import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.security.CustomUserDetails;
import io.sentry.SentryLevel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Filter that automatically tracks HTTP requests in Sentry with user context,
 * breadcrumbs, and performance monitoring.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SentryRequestFilter extends OncePerRequestFilter {

    private final SentryService sentryService;
    private static final long SLOW_REQUEST_THRESHOLD_MS = 1000; // 1 second

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                   @NonNull HttpServletResponse response,
                                   @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip Sentry tracking for monitoring endpoints
        String requestURI = request.getRequestURI();
        if (shouldSkipTracking(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        Instant startTime = Instant.now();
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Set user context from SecurityContext
            setUserContextFromSecurityContext();

            // Add breadcrumb for request start
            sentryService.addBreadcrumb(
                String.format("%s %s", request.getMethod(), requestURI),
                "http.request",
                SentryLevel.INFO
            );

            // Set request context
            setRequestContext(request);

            filterChain.doFilter(wrappedRequest, wrappedResponse);

            // Track request duration
            Duration duration = Duration.between(startTime, Instant.now());
            long durationMs = duration.toMillis();

            // Add breadcrumb for request completion
            sentryService.addBreadcrumb(
                String.format("%s %s - %dms - Status: %d",
                    request.getMethod(), requestURI, durationMs, wrappedResponse.getStatus()),
                "http.response",
                durationMs > SLOW_REQUEST_THRESHOLD_MS ? SentryLevel.WARNING : SentryLevel.INFO
            );

            // Capture slow requests
            if (durationMs > SLOW_REQUEST_THRESHOLD_MS) {
                sentryService.captureWarning(
                    String.format("Slow request detected: %s %s took %dms",
                        request.getMethod(), requestURI, durationMs)
                );
                sentryService.setTags(Map.of(
                    "slow_request", "true",
                    "duration_ms", String.valueOf(durationMs),
                    "http_method", request.getMethod(),
                    "http_status", String.valueOf(wrappedResponse.getStatus())
                ));
            }

            // Capture 5xx errors
            if (wrappedResponse.getStatus() >= 500) {
                sentryService.captureError(
                    String.format("Server error: %s %s returned status %d",
                        request.getMethod(), requestURI, wrappedResponse.getStatus())
                );
                sentryService.setTags(Map.of(
                    "http_status", String.valueOf(wrappedResponse.getStatus()),
                    "http_method", request.getMethod()
                ));
            }

        } catch (Exception e) {
            // Capture exceptions with request context
            sentryService.captureException(e, buildRequestContext(request));
            sentryService.setTags(Map.of(
                "request_path", requestURI,
                "http_method", request.getMethod()
            ));
            throw e;
        } finally {
            // Copy response content
            wrappedResponse.copyBodyToResponse();

            // Clear user context to prevent leaks
            sentryService.clearUserContext();
        }
    }

    /**
     * Checks if the request should skip Sentry tracking.
     */
    private boolean shouldSkipTracking(String requestURI) {
        return requestURI.startsWith("/actuator") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.startsWith("/webjars") ||
               requestURI.equals("/favicon.ico");
    }

    /**
     * Sets user context in Sentry from SecurityContext.
     */
    private void setUserContextFromSecurityContext() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof CustomUserDetails) {
                    CustomUserDetails userDetails = (CustomUserDetails) principal;
                    com.finance_control.users.model.User user = userDetails.getUser();
                    sentryService.setUserContext(
                        user.getId(),
                        user.getEmail(),
                        user.getEmail() // Using email as username
                    );
                } else if (principal instanceof UserDetails) {
                    UserDetails userDetails = (UserDetails) principal;
                    Long userId = UserContext.getCurrentUserId();
                    if (userId != null) {
                        sentryService.setUserContext(userId, null, userDetails.getUsername());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to set user context from SecurityContext: {}", e.getMessage());
        }
    }

    /**
     * Sets request context in Sentry.
     */
    private void setRequestContext(HttpServletRequest request) {
        try {
            Map<String, String> tags = Map.of(
                "http_method", request.getMethod(),
                "request_path", request.getRequestURI()
            );
            sentryService.setTags(tags);

            Map<String, Object> context = buildRequestContext(request);
            context.forEach((key, value) -> sentryService.setContext(key, value));
        } catch (Exception e) {
            log.debug("Failed to set request context: {}", e.getMessage());
        }
    }

    /**
     * Builds request context map for Sentry.
     */
    private Map<String, Object> buildRequestContext(HttpServletRequest request) {
        return Map.of(
            "request_uri", request.getRequestURI(),
            "query_string", request.getQueryString() != null ? request.getQueryString() : "",
            "remote_addr", request.getRemoteAddr(),
            "user_agent", request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : ""
        );
    }
}
