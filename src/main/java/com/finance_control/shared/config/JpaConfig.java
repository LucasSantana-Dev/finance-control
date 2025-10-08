package com.finance_control.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration class that enables JPA auditing and repositories.
 * This configuration is essential for automatic timestamp management
 * in BaseModel entities (createdAt, updatedAt fields).
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.finance_control")
public class JpaConfig {
    // Configuration is handled by annotations
}
