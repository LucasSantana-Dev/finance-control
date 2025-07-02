package com.finance_control.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Profile("!test")
@EnableJpaAuditing
@Slf4j
public class JpaAuditingConfig {
} 