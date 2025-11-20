package com.finance_control.shared.config.properties;

/**
 * Server configuration properties.
 */
public record ServerProperties(
    int port,
    String contextPath,
    String servletPath,
    int maxHttpHeaderSize,
    int maxHttpPostSize,
    int connectionTimeout,
    int readTimeout,
    int writeTimeout
) {
    public ServerProperties() {
        this(8080, "", "/", 8192, 2097152, 20000, 30000, 30000);
    }
}

