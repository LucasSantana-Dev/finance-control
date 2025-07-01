package com.finance_control.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing
@Profile("test")
public class TestConfig {
    
    // This configuration is only loaded for tests with @ActiveProfiles("test")
    // It ensures JPA auditing is available for repository tests
} 