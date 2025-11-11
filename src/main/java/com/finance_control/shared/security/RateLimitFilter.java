package com.finance_control.shared.security;

import com.finance_control.shared.config.AppProperties;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

/**
 * Rate limiting filter that applies rate limiting to API requests.
 * Uses Bucket4j for token bucket algorithm implementation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final Bucket rateLimitBucket;
    private final AppProperties appProperties;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip rate limiting if disabled
        if (!appProperties.getRateLimit().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip rate limiting for public endpoints
        String requestPath = request.getRequestURI();
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check rate limit
        ConsumptionProbe probe = rateLimitBucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.setHeader("X-Rate-Limit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            log.warn("Rate limit exceeded for request: {} from IP: {}", requestPath, getClientIpAddress(request));

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-Rate-Limit-Remaining", "0");
            response.setHeader("X-Rate-Limit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));
            response.setHeader("Retry-After", String.valueOf(Duration.ofNanos(probe.getNanosToWaitForRefill()).getSeconds()));
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
        }
    }

    /**
     * Checks if the request path is a public endpoint that should skip rate limiting.
     */
    private boolean isPublicEndpoint(String requestPath) {
        String[] publicEndpoints = appProperties.getSecurity().getPublicEndpoints();
        if (publicEndpoints == null) {
            return false;
        }

        for (String endpoint : publicEndpoints) {
            if (requestPath.matches(endpoint.replace("**", ".*"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the client IP address from the request.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
