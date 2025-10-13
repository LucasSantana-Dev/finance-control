package com.finance_control.shared.security;

import com.finance_control.shared.context.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter that processes JWT tokens from requests
 * and sets up the security context and user context.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

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
            logger.debug("Extracted JWT from request: " + (jwt != null ? "YES" : "NO"));

            if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
                Long userId = jwtUtils.getUserIdFromToken(jwt);
                logger.debug("Extracted user ID from JWT: " + userId);

                if (userId != null) {
                    // Set user context for the current request
                    UserContext.setCurrentUserId(userId);

                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());
                    logger.debug("Loaded user details for ID " + userId + ": " + (userDetails != null ? "SUCCESS" : "FAILED"));
                    
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("Set authentication in security context for user ID: " + userId);
                }
            } else {
                logger.debug("JWT validation failed or no JWT found");
            }
        } catch (Exception e) {
            logger.error("Could not set user authentication in security context", e);
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Always clear the user context at the end of the request
            UserContext.clear();
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
