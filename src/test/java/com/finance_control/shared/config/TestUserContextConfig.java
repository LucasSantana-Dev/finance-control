package com.finance_control.shared.config;

import com.finance_control.shared.context.UserContext;
import org.springframework.boot.test.context.TestConfiguration;

import jakarta.annotation.PostConstruct;

/**
 * Test configuration that initializes UserContext
 * with test data for integration tests.
 */
@TestConfiguration
public class TestUserContextConfig {

    @PostConstruct
    public void initializeUserContext() {
        // Configure user context for tests
        UserContext.setCurrentUserId(1L);
    }
} 