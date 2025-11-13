package com.finance_control.shared.config;

import com.finance_control.shared.security.CustomUserDetailsService;
import com.finance_control.shared.security.JwtAuthenticationFilter;
import com.finance_control.shared.security.RateLimitFilter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration that uses environment variables through AppProperties.
 * Configures JWT authentication, CORS, and security filters.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@SuppressFBWarnings("EI_EXPOSE_REP2") // False positive: Spring dependency injection is safe
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final AppProperties appProperties;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security with public endpoints: {}",
                appProperties.security().publicEndpoints());

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                // Basic hardening; keep CSP conservative since we expose only API endpoints
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
                .frameOptions(frame -> frame.sameOrigin())
                .referrerPolicy(ref -> ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
            )
            .authorizeHttpRequests(auth -> {
                List<String> publicEndpoints = appProperties.security().publicEndpoints();
                if (publicEndpoints != null && !publicEndpoints.isEmpty()) {
                    auth.requestMatchers(publicEndpoints.toArray(new String[0])).permitAll();
                } else {
                    // Fallback to hardcoded endpoints
                    auth.requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users").permitAll()
                        .requestMatchers("/api/monitoring/**").permitAll()
                        .requestMatchers("/monitoring/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        AppProperties.Cors cors = appProperties.security().cors();

        // Parse allowed origins from comma-separated string
        List<String> allowedOrigins = cors.allowedOrigins();
        configuration.setAllowedOriginPatterns(allowedOrigins);

        configuration.setAllowedMethods(cors.allowedMethods());

        // Avoid permissive wildcard headers; use a conservative default set when '*' is present
        List<String> requestedHeaders = cors.allowedHeaders();
        if (requestedHeaders.size() == 1 && "*".equals(requestedHeaders.get(0))) {
            requestedHeaders = Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
            );
        }
        configuration.setAllowedHeaders(requestedHeaders);
        configuration.setAllowCredentials(cors.allowCredentials());
        configuration.setMaxAge(cors.maxAge());

        log.info("CORS configured - Origins: {}, Methods: {}, Credentials: {}",
                allowedOrigins, cors.allowedMethods(), cors.allowCredentials());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @SuppressWarnings("deprecation") // Using deprecated constructor/method as they are the only available ones
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
