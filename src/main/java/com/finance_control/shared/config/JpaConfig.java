package com.finance_control.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA configuration for auditing support.
 * Enables automatic timestamp management for BaseModel entities.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
