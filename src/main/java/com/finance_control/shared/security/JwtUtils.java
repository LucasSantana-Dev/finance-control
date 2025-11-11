package com.finance_control.shared.security;

import com.finance_control.shared.config.AppProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

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
        Date expiryDate = new Date(now.getTime() + appProperties.getSecurity().getJwt().getExpirationMs());
        
        SecretKey key = Keys.hmacShaKeyFor(appProperties.getSecurity().getJwt().getSecret().getBytes());
        
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(appProperties.getSecurity().getJwt().getIssuer())
                .audience().add(appProperties.getSecurity().getJwt().getAudience()).and()
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
        Date expiryDate = new Date(now.getTime() + appProperties.getSecurity().getJwt().getRefreshExpirationMs());
        
        SecretKey key = Keys.hmacShaKeyFor(appProperties.getSecurity().getJwt().getSecret().getBytes());
        
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .issuer(appProperties.getSecurity().getJwt().getIssuer())
                .audience().add(appProperties.getSecurity().getJwt().getAudience() + "-refresh").and()
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
            SecretKey key = Keys.hmacShaKeyFor(appProperties.getSecurity().getJwt().getSecret().getBytes());
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | NumberFormatException e) {
            log.warn("Failed to extract user ID from token");
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
            SecretKey key = Keys.hmacShaKeyFor(appProperties.getSecurity().getJwt().getSecret().getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            log.warn("Invalid JWT token");
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
            SecretKey key = Keys.hmacShaKeyFor(appProperties.getSecurity().getJwt().getSecret().getBytes());
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return claims.getExpiration();
        } catch (JwtException e) {
            log.warn("Failed to extract expiration from token");
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
} 