package com.finance_control.shared.config;

import com.harium.supabase.SupabaseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Supabase realtime client.
 * Provides bean for Supabase realtime operations.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.enabled", havingValue = "true", matchIfMissing = true)
public class SupabaseConfig {

    private final AppProperties appProperties;

    /**
     * Creates a Supabase client bean for realtime operations.
     *
     * @return configured SupabaseClient
     */
    @Bean
    @ConditionalOnProperty(value = "app.supabase.realtime.enabled", havingValue = "true", matchIfMissing = true)
    public SupabaseClient supabaseClient() {
        String url = appProperties.getSupabase().getUrl();
        String anonKey = appProperties.getSupabase().getAnonKey();

        if (url.isEmpty() || anonKey.isEmpty()) {
            log.warn("Supabase client not configured. URL or anon key is missing.");
            return null;
        }

        log.info("Initializing Supabase client for URL: {}", url);
        return new SupabaseClient(url, anonKey);
    }
}
