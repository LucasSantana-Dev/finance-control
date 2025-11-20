package com.finance_control.shared.feature;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for checking feature flags.
 * Provides type-safe methods to determine if features are enabled.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureFlagService {

    private final AppProperties appProperties;

    /**
     * Check if a feature is enabled.
     *
     * @param feature the feature to check
     * @return true if the feature is enabled, false otherwise
     */
    public boolean isEnabled(Feature feature) {
        boolean enabled = checkFeatureEnabled(feature);
        log.debug("Feature '{}' is {}", feature.name(), enabled ? "enabled" : "disabled");
        return enabled;
    }

    /**
     * Check if a feature is enabled by name.
     *
     * @param featureName the name of the feature to check
     * @return true if the feature is enabled, false otherwise
     */
    public boolean isEnabled(String featureName) {
        try {
            Feature feature = Feature.valueOf(featureName.toUpperCase());
            return isEnabled(feature);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown feature name: {}", featureName);
            return false;
        }
    }

    /**
     * Require that a feature is enabled.
     * Throws FeatureDisabledException if the feature is disabled.
     *
     * @param feature the feature to check
     * @throws FeatureDisabledException if the feature is disabled
     */
    public void requireEnabled(Feature feature) {
        if (!isEnabled(feature)) {
            throw new FeatureDisabledException(feature);
        }
    }

    /**
     * Check if a feature category is enabled.
     *
     * @param category the category name (e.g., "financial-predictions")
     * @return true if the category is enabled, false otherwise
     */
    public boolean isFeatureCategoryEnabled(String category) {
        try {
            Feature feature = Feature.valueOf(category.toUpperCase().replace("-", "_"));
            return isEnabled(feature);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown feature category: {}", category);
            return false;
        }
    }

    private boolean checkFeatureEnabled(Feature feature) {
        var featureFlags = appProperties.featureFlags();

        return switch (feature) {
            case FINANCIAL_PREDICTIONS -> featureFlags.financialPredictions().enabled();
            case BRAZILIAN_MARKET -> featureFlags.brazilianMarket().enabled();
            case OPEN_FINANCE -> featureFlags.openFinance().enabled();
            case REPORTS -> featureFlags.reports().enabled();
            case DATA_EXPORT -> featureFlags.dataExport().enabled();
            case REALTIME_NOTIFICATIONS -> featureFlags.realtimeNotifications().enabled();
            case MONITORING -> featureFlags.monitoring().enabled();
            case SUPABASE_AUTH -> featureFlags.supabaseFeatures().auth();
            case SUPABASE_STORAGE -> featureFlags.supabaseFeatures().storage();
            case SUPABASE_REALTIME -> featureFlags.supabaseFeatures().realtime();
        };
    }
}
