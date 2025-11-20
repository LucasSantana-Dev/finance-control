package com.finance_control.shared.security;

import com.finance_control.shared.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import com.finance_control.shared.service.UserMappingService;
import com.finance_control.shared.service.SupabaseAuthService;

/**
 * JWT authentication filter that processes JWT tokens from requests
 * and sets up the security context and user context.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    // Optional services for Supabase integration
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private UserMappingService userMappingService;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    @SuppressWarnings("unused")
    private SupabaseAuthService supabaseAuthService; // Reserved for future use

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (shouldSkipJwtProcessing(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            processJwtAuthentication(request);
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            UserContext.clear();
        }
    }

    private boolean shouldSkipJwtProcessing(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.startsWith("/webjars") ||
               requestURI.startsWith("/auth") ||
               requestURI.startsWith("/public") ||
               requestURI.startsWith("/actuator");
    }

    private void processJwtAuthentication(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        log.debug("Extracted JWT from request: {}", jwt != null ? "YES" : "NO");

        if (!StringUtils.hasText(jwt) || !jwtUtils.validateTokenUniversal(jwt)) {
            log.debug("JWT validation failed or no JWT found");
            return;
        }

        Object userIdObj = jwtUtils.getUserIdFromTokenUniversal(jwt);
        log.debug("Extracted user ID from JWT: {}", userIdObj);

        if (userIdObj == null) {
            return;
        }

        Long userId = extractUserIdFromToken(jwt, userIdObj);
        if (userId != null) {
            setAuthenticationForUser(request, userId, jwtUtils.isSupabaseToken(jwt));
        }
    }

    private Long extractUserIdFromToken(String jwt, Object userIdObj) {
        boolean isSupabaseToken = jwtUtils.isSupabaseToken(jwt);
        if (isSupabaseToken) {
            String supabaseUserId = (String) userIdObj;
            log.debug("Processing Supabase JWT for user: {}", supabaseUserId);
            return mapSupabaseUserToApplicationUser(supabaseUserId);
        } else {
            return (Long) userIdObj;
        }
    }

    private void setAuthenticationForUser(HttpServletRequest request, Long userId, boolean isSupabaseToken) {
        UserContext.setCurrentUserId(userId);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());
        log.debug("Loaded user details for ID {}: {}", userId, userDetails != null ? "SUCCESS" : "FAILED");

        if (userDetails != null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set authentication in security context for user ID: {} (Supabase: {})", userId, isSupabaseToken);
        } else {
            log.warn("User details could not be loaded for user ID: {}", userId);
        }
    }

    /**
     * Maps a Supabase user UUID to an application user ID.
     * Uses UserMappingService to find the corresponding local user.
     * Note: Users must authenticate via Supabase auth endpoints first to create the mapping.
     *
     * @param supabaseUserId the Supabase user UUID
     * @return the application user ID, or null if mapping fails
     */
    private Long mapSupabaseUserToApplicationUser(String supabaseUserId) {
        if (userMappingService == null) {
            log.warn("UserMappingService not available for Supabase user mapping");
            return null;
        }

        try {
            Long userId = userMappingService.findUserIdBySupabaseId(supabaseUserId);
            if (userId == null) {
                log.warn("No local user found for Supabase user ID: {}. User must authenticate via Supabase auth endpoints first.", supabaseUserId);
            } else {
                log.debug("Mapped Supabase user {} to local user ID: {}", supabaseUserId, userId);
            }
            return userId;
        } catch (Exception e) {
            log.error("Error mapping Supabase user to application user: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts the JWT token from the Authorization header.
     *
     * @param request the HTTP request
     * @return the JWT token, or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
