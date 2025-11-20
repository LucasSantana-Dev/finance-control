package com.finance_control.shared.feature;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown when a required feature is disabled.
 * Returns HTTP 503 Service Unavailable status.
 */
public class FeatureDisabledException extends ResponseStatusException {

    public FeatureDisabledException(Feature feature) {
        super(
            HttpStatus.SERVICE_UNAVAILABLE,
            String.format("Feature '%s' is currently disabled", feature.name())
        );
    }

    public FeatureDisabledException(String featureName) {
        super(
            HttpStatus.SERVICE_UNAVAILABLE,
            String.format("Feature '%s' is currently disabled", featureName)
        );
    }
}
