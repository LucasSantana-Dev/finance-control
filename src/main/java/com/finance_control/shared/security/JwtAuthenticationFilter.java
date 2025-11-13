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
import java.util.Optional;
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

        // Skip JWT processing for Swagger UI and API docs
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/swagger-ui") ||
                requestURI.startsWith("/v3/api-docs") ||
                requestURI.startsWith("/webjars") ||
                requestURI.startsWith("/auth") ||
                requestURI.startsWith("/public") ||
                requestURI.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String jwt = extractJwtFromRequest(request);
            log.debug("Extracted JWT from request: {}", jwt != null ? "YES" : "NO");

            if (StringUtils.hasText(jwt) && jwtUtils.validateTokenUniversal(jwt)) {
                Object userIdObj = jwtUtils.getUserIdFromTokenUniversal(jwt);
                log.debug("Extracted user ID from JWT: {}", userIdObj);

                if (userIdObj != null) {
                    Long userId = null;
                    boolean isSupabaseToken = jwtUtils.isSupabaseToken(jwt);

                    if (isSupabaseToken) {
                        // Handle Supabase JWT - userIdObj is a String UUID
                        String supabaseUserId = (String) userIdObj;
                        log.debug("Processing Supabase JWT for user: {}", supabaseUserId);

                        // TODO: Implement mapping from Supabase UUID to application user ID
                        // For now, we'll need to create/find the user in the application database
                        // This will be implemented in the SupabaseAuthService
                        userId = mapSupabaseUserToApplicationUser(supabaseUserId);
                    } else {
                        // Handle application JWT - userIdObj is a Long
                        userId = (Long) userIdObj;
                    }

                    if (userId != null) {
                        // Set user context for the current request
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
                    } else {
                        log.warn("Could not map user ID for authentication");
                    }
                }
            } else {
                log.debug("JWT validation failed or no JWT found");
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the user context at the end of the request
            UserContext.clear();
        }
    }

    /**
     * Maps a Supabase user UUID to an application user ID.
     * Uses UserMappingService to find or create the corresponding local user.
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
            // For JWT-based mapping, we need to get user info from Supabase
            // Since we don't have the access token here, we'll try to find by Supabase user ID
            // If not found, we can't create the user without more information

            Optional<com.finance_control.users.model.User> existingUser =
                userMappingService.findBySupabaseUserId(supabaseUserId);

            if (existingUser.isPresent()) {
                log.debug("Found existing mapped user for Supabase user: {}", supabaseUserId);
                return existingUser.get().getId();
            } else {
                // If we can't find the user and don't have full user info, we can't create them
                // In a real implementation, you might want to store minimal user info or defer creation
                log.warn("No existing user found for Supabase user ID: {}. User must authenticate via Supabase auth endpoints first.", supabaseUserId);
                return null;
            }

        } catch (Exception e) {
            log.error("Error mapping Supabase user to application user", e);
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
