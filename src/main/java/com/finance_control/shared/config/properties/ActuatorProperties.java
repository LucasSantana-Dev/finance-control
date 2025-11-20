package com.finance_control.shared.config.properties;

import java.util.List;

/**
 * Actuator configuration properties.
 */
public record ActuatorProperties(
    boolean enabled,
    List<String> endpoints,
    String basePath,
    boolean exposeHealthDetails,
    boolean showDetails,
    boolean showComponents
) {
    public ActuatorProperties() {
        this(true,
             List.of("health", "info", "metrics", "env"),
             "/actuator",
             true,
             true,
             true);
    }
}

