package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for Supabase clients.
 * Provides beans for Supabase Storage and Realtime operations.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseConfig {

    private final AppProperties appProperties;


    /**
     * Creates a WebClient bean configured for Supabase API calls.
     * Used by SupabaseAuthService for authentication operations.
     *
     * @return configured WebClient
     */
    @Bean
    @Qualifier("supabaseWebClient")
    public WebClient supabaseWebClient() {
        String url = appProperties.supabase().url();
        String anonKey = appProperties.supabase().anonKey();

        if (!StringUtils.hasText(url) || !StringUtils.hasText(anonKey)) {
            log.warn("Supabase WebClient not configured. URL or anon key is missing.");
            return WebClient.builder().build();
        }

        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + anonKey)
                .defaultHeader("apikey", anonKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .build();
    }

    /**
     * Creates a Supabase client bean for realtime operations.
     * TODO: Implement when Supabase Realtime Java client is available
     *
     * @return configured SupabaseClient for realtime
     */
    // @Bean
    // @ConditionalOnProperty(value = "app.supabase.realtime.enabled", havingValue = "true", matchIfMissing = true)
    public Object supabaseRealtimeClient() {
        String url = appProperties.supabase().url();
        String anonKey = appProperties.supabase().anonKey();

        if (!StringUtils.hasText(url) || !StringUtils.hasText(anonKey)) {
            log.warn("Supabase Realtime client not configured. URL or anon key is missing.");
            return null;
        }

        log.info("Supabase Realtime client initialization placeholder for URL: {}", url);
        // TODO: Implement when Supabase Realtime Java client becomes available
        return null;
    }
}
