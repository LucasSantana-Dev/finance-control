package com.finance_control.shared.security;

import com.finance_control.shared.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Utility class for JWT token operations including generation, validation, and parsing.
 * Uses environment variables through AppProperties for configuration.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final AppProperties appProperties;

    /**
     * Generates a JWT token for the given user ID.
     *
     * @param userId the user ID to include in the token
     * @return the generated JWT token
     */
    public String generateToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.security().jwt().expirationMs());

        SecretKey key = Keys.hmacShaKeyFor(appProperties.security().jwt().secret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(appProperties.security().jwt().issuer())
                .audience().add(appProperties.security().jwt().audience()).and()
                .signWith(key)
                .compact();
    }

    /**
     * Generates a refresh token for the given user ID.
     *
     * @param userId the user ID to include in the token
     * @return the generated refresh token
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + appProperties.security().jwt().refreshExpirationMs());

        SecretKey key = Keys.hmacShaKeyFor(appProperties.security().jwt().secret().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(appProperties.security().jwt().issuer())
                .audience().add(appProperties.security().jwt().audience() + "-refresh").and()
                .signWith(key)
                .compact();
    }

    /**
     * Extracts the user ID from a JWT token.
     *
     * @param token the JWT token
     * @return the user ID, or null if the token is invalid
     */
    public Long getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(appProperties.security().jwt().secret().getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Long.valueOf(claims.getSubject());
        } catch (JwtException | NumberFormatException e) {
            log.warn("Failed to extract user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Validates a JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(appProperties.security().jwt().secret().getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Gets the expiration time from a JWT token.
     *
     * @param token the JWT token
     * @return the expiration date, or null if the token is invalid
     */
    public Date getExpirationFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(appProperties.security().jwt().secret().getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();
        } catch (JwtException e) {
            log.warn("Failed to extract expiration from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    /**
     * Validates a Supabase JWT token.
     * Supabase JWTs have specific claims: iss, aud, role, sub (UUID)
     *
     * @param token the Supabase JWT token
     * @return true if the token is valid, false otherwise
     */
    public boolean validateSupabaseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        try {
            Claims claims = parseSupabaseToken(token);
            if (claims == null) {
                return false;
            }

            return validateSupabaseClaims(claims);

        } catch (JwtException e) {
            log.warn("Invalid Supabase JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error validating Supabase JWT token", e);
            return false;
        }
    }

    private Claims parseSupabaseToken(String token) {
        String jwtSigner = appProperties.supabase().jwtSigner();
        if (!StringUtils.hasText(jwtSigner)) {
            log.warn("Supabase JWT signer not configured");
            return null;
        }

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSigner.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("Failed to parse Supabase JWT token: {}", e.getMessage());
            return null;
        }
    }

    private boolean validateSupabaseClaims(Claims claims) {
        String issuer = claims.getIssuer();
        String audience = extractAudience(claims);
        String role = claims.get("role", String.class);
        String subject = claims.getSubject();

        if (!validateBasicClaims(issuer, audience, role, subject)) {
            return false;
        }

        if (!validateRole(role)) {
            return false;
        }

        if (!validateSubject(subject)) {
            return false;
        }

        if (!validateExpiration(claims)) {
            return false;
        }

        log.debug("Successfully validated Supabase JWT for user: {} with role: {}", subject, role);
        return true;
    }

    private String extractAudience(Claims claims) {
        if (claims.getAudience() != null && !claims.getAudience().isEmpty()) {
            return claims.getAudience().iterator().next();
        }
        return null;
    }

    private boolean validateBasicClaims(String issuer, String audience, String role, String subject) {
        if (!StringUtils.hasText(issuer) || !StringUtils.hasText(audience)) {
            log.warn("Invalid Supabase JWT claims: missing issuer or audience");
            return false;
        }
        if (!StringUtils.hasText(role)) {
            log.warn("Invalid Supabase JWT: missing role claim");
            return false;
        }
        if (!StringUtils.hasText(subject)) {
            log.warn("Invalid Supabase JWT: missing subject claim");
            return false;
        }
        return true;
    }

    private boolean validateRole(String role) {
        if (!"authenticated".equals(role)) {
            log.warn("Invalid Supabase JWT: role must be 'authenticated', got: {}", role);
            return false;
        }
        return true;
    }

    private boolean validateSubject(String subject) {
        try {
            UUID.fromString(subject);
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Invalid Supabase JWT: subject is not a valid UUID: {}", subject);
            return false;
        }
    }

    private boolean validateExpiration(Claims claims) {
        Date expiration = claims.getExpiration();
        if (expiration != null && expiration.before(new Date())) {
            log.warn("Supabase JWT token is expired");
            return false;
        }
        return true;
    }

    /**
     * Extracts the Supabase user UUID from a Supabase JWT token.
     * Supabase uses UUID as the subject claim.
     *
     * @param token the Supabase JWT token
     * @return the Supabase user UUID, or null if invalid
     */
    public String getSupabaseUserIdFromToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        try {
            String jwtSigner = appProperties.supabase().jwtSigner();
            if (!StringUtils.hasText(jwtSigner)) {
                log.warn("Supabase JWT signer not configured");
                return null;
            }

            SecretKey key = Keys.hmacShaKeyFor(jwtSigner.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String subject = claims.getSubject();
            if (!StringUtils.hasText(subject)) {
                log.warn("Supabase JWT missing subject claim");
                return null;
            }

            // Validate UUID format
            try {
                UUID.fromString(subject);
                return subject;
            } catch (IllegalArgumentException e) {
                log.warn("Supabase JWT subject is not a valid UUID: {}", subject);
                return null;
            }

        } catch (JwtException e) {
            log.warn("Failed to extract Supabase user ID from token: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error extracting Supabase user ID from token", e);
            return null;
        }
    }

    /**
     * Gets the role from a Supabase JWT token.
     *
     * @param token the Supabase JWT token
     * @return the role, or null if invalid
     */
    public String getSupabaseRoleFromToken(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }

        try {
            String jwtSigner = appProperties.supabase().jwtSigner();
            if (!StringUtils.hasText(jwtSigner)) {
                return null;
            }

            SecretKey key = Keys.hmacShaKeyFor(jwtSigner.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("role", String.class);

        } catch (JwtException e) {
            log.warn("Failed to extract role from Supabase JWT: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("Error extracting role from Supabase JWT", e);
            return null;
        }
    }

    /**
     * Determines if a JWT token is a Supabase JWT by attempting to validate it.
     *
     * @param token the JWT token
     * @return true if it's a valid Supabase JWT, false otherwise
     */
    public boolean isSupabaseToken(String token) {
        return validateSupabaseToken(token);
    }

    /**
     * Universal token validation that supports both application JWTs and Supabase JWTs.
     *
     * @param token the JWT token
     * @return true if the token is valid (either format), false otherwise
     */
    public boolean validateTokenUniversal(String token) {
        // First try Supabase JWT validation
        if (isSupabaseToken(token)) {
            return true;
        }

        // Fall back to application JWT validation
        return validateToken(token);
    }

    /**
     * Universal user ID extraction that supports both application JWTs and Supabase JWTs.
     *
     * @param token the JWT token
     * @return the user ID (Long for app JWTs, String UUID for Supabase JWTs), or null if invalid
     */
    public Object getUserIdFromTokenUniversal(String token) {
        // First try Supabase JWT extraction
        if (isSupabaseToken(token)) {
            return getSupabaseUserIdFromToken(token);
        }

        // Fall back to application JWT extraction
        return getUserIdFromToken(token);
    }
}
