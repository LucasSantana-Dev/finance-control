package com.finance_control.unit;

import com.finance_control.shared.context.UserContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Utility for configuring UserContext in unit tests.
 * Provides methods to simulate an authenticated user during tests.
 */
public class TestUtils {
    
    /**
     * Configures UserContext with a specific user ID.
     * 
     * @param userId the user ID to be configured
     */
    public static void setupUserContext(Long userId) {
        UserContext.setCurrentUserId(userId);
    }
    
    /**
     * Limpa o UserContext.
     */
    public static void clearUserContext() {
        UserContext.clear();
    }
    
    /**
     * Extension para configurar automaticamente o UserContext nos testes.
     */
    @ExtendWith(UserContextExtension.class)
    public static class UserContextExtension implements BeforeEachCallback, AfterEachCallback {
        
        @Override
        public void beforeEach(ExtensionContext context) {
            // Configure a default user for all tests
            UserContext.setCurrentUserId(1L);
        }
        
        @Override
        public void afterEach(ExtensionContext context) {
            // Clear context after each test
            UserContext.clear();
        }
    }
} 