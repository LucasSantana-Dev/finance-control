package com.finance_control.dashboard.service;

/**
 * Abstraction for LLM-backed financial prediction engines.
 */
public interface PredictionModelClient {

    /**
     * Generates a financial forecast using the underlying AI provider.
     *
     * @param prompt textual instructions containing both system and user context
     * @return raw model output
     */
    String generateForecast(String prompt);
}
