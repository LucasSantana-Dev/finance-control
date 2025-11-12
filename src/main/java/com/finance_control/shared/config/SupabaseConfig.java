package com.finance_control.shared.config;

// Temporarily disabled until Supabase dependency is available
// import com.harium.supabase.SupabaseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Supabase realtime client.
 * Provides bean for Supabase realtime operations.
 * TEMPORARILY DISABLED - Missing Supabase dependency
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.enabled", havingValue = "true", matchIfMissing = false) // Disabled by default
public class SupabaseConfig {

    private final AppProperties appProperties;

    /**
     * Creates a Supabase client bean for realtime operations.
     * TEMPORARILY DISABLED - Missing Supabase dependency
     *
     * @return configured SupabaseClient
     */
    // @Bean
    // @ConditionalOnProperty(value = "app.supabase.realtime.enabled", havingValue = "true", matchIfMissing = true)
    public Object supabaseClient() { // Changed return type to Object
        String url = appProperties.supabase().url();
        String anonKey = appProperties.supabase().anonKey();

        if (url.isEmpty() || anonKey.isEmpty()) {
            log.warn("Supabase client not configured. URL or anon key is missing.");
            return null;
        }

        log.info("Initializing Supabase client for URL: {} - TEMPORARILY DISABLED", url);
        // return new SupabaseClient(url, anonKey);
        return null; // Temporarily return null
    }
}
