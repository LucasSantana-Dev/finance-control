package com.finance_control.shared.config.properties;

import java.util.List;

/**
 * Flyway configuration properties.
 */
public record FlywayProperties(
    boolean enabled,
    List<String> locations,
    String baselineOnMigrate,
    String baselineVersion,
    String validateOnMigrate,
    String outOfOrder,
    String cleanDisabled,
    String cleanOnValidationError
) {
    public FlywayProperties() {
        this(true,
             List.of("classpath:db/migration"),
             "false",
             "0",
             "true",
             "false",
             "true",
             "false");
    }
}

