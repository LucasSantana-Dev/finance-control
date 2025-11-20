package com.finance_control.shared.config.properties;

import java.util.List;

/**
 * Security configuration properties.
 */
public record SecurityProperties(
    JwtProperties jwt,
    CorsProperties cors,
    List<String> publicEndpoints,
    EncryptionProperties encryption
) {
    public SecurityProperties() {
        this(new JwtProperties(), new CorsProperties(), List.of(
            "/api/auth/**",
            "/api/users",
            "/api/monitoring/**",
            "/monitoring/**",
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/api-docs/**"), new EncryptionProperties());
    }

    public record JwtProperties(
        String secret,
        long expirationMs,
        long refreshExpirationMs,
        String issuer,
        String audience
    ) {
        public JwtProperties() {
            this(null, 86400000, 604800000, "finance-control", "finance-control-users");
        }
    }

    public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        boolean allowCredentials,
        long maxAge
    ) {
        public CorsProperties() {
            this(List.of("http://localhost:3000", "http://localhost:8080"),
                 List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"),
                 List.of("*"),
                 true,
                 3600);
        }
    }

    public record EncryptionProperties(
        boolean enabled,
        String key,
        String algorithm
    ) {
        public EncryptionProperties() {
            this(true, "", "AES/GCM/NoPadding");
        }
    }
}

