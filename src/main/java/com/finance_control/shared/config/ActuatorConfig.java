package com.finance_control.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Actuator configuration that uses environment variables through AppProperties.
 * Configures Spring Boot Actuator endpoints and security from environment variables.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AppProperties.class)
public class ActuatorConfig {
    
    private final AppProperties appProperties;
    
    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        AppProperties.Actuator actuator = appProperties.getActuator();
        
        log.info("Configuring actuator security - Endpoints: {}, BasePath: {}, ShowDetails: {}", 
                String.join(",", actuator.getEndpoints()),
                actuator.getBasePath(),
                actuator.isShowDetails());
        
        http
            .securityMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(EndpointRequest.to("health")).permitAll()
                .requestMatchers(EndpointRequest.toAnyEndpoint()).authenticated()
            )
            .httpBasic();
        
        return http.build();
    }
} 