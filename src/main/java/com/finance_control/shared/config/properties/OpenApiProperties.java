package com.finance_control.shared.config.properties;

/**
 * OpenAPI configuration properties.
 */
public record OpenApiProperties(
    String title,
    String description,
    String version,
    String contactName,
    String contactEmail,
    String contactUrl,
    String licenseName,
    String licenseUrl,
    String serverUrl,
    String serverDescription
) {
    public OpenApiProperties() {
        this("Finance Control API",
             "API for managing personal finances",
             "1.0.0",
             "Finance Control Team",
             "support@finance-control.com",
             "https://github.com/LucasSantana/finance-control",
             "MIT License",
             "https://opensource.org/licenses/MIT",
             "http://localhost:8080",
             "Development server");
    }
}

