package com.finance_control.brazilian_market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for market data services.
 * Provides beans for external API integration.
 */
@Configuration
public class MarketDataConfig {

    /**
     * RestTemplate bean for making HTTP requests to external APIs
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
