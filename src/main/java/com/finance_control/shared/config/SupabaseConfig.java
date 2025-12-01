package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for Supabase clients.
 * Provides beans for Supabase API operations.
 * Note: Realtime operations are handled by SupabaseRealtimeService via WebSocket.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.enabled", havingValue = "true", matchIfMissing = true)
public class SupabaseConfig {

    private final AppProperties appProperties;


    /**
     * Creates a RestClient bean configured for Supabase API calls.
     * Used by SupabaseAuthService for authentication operations.
     *
     * @return configured RestClient
     */
    @Bean
    @Qualifier("supabaseRestClient")
    public RestClient supabaseRestClient() {
        String url = appProperties.supabase().url();
        String anonKey = appProperties.supabase().anonKey();

        if (!StringUtils.hasText(url) || !StringUtils.hasText(anonKey)) {
            log.warn("Supabase RestClient not configured. URL or anon key is missing.");
            return RestClient.builder().build();
        }

        return RestClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                .defaultHeader("apikey", anonKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

}
