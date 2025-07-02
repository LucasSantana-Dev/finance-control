package com.finance_control.shared.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration class to enable @ConfigurationProperties and validation.
 * This allows Spring Boot to bind environment variables to the AppProperties class.
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
@Validated
public class ConfigurationPropertiesConfig {
} 