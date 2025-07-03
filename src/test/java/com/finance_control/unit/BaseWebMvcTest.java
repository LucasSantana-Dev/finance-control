package com.finance_control.unit;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for controller unit tests using @WebMvcTest.
 * These are slice tests that only load the web layer (controllers, filters, etc.)
 * and mock all service dependencies.
 */
@ExtendWith(TestUtils.UserContextExtension.class)
@ActiveProfiles("test")
public abstract class BaseWebMvcTest {
    // Common setup for all WebMvcTest-based controller unit tests
    // UserContext is automatically configured with userId = 1L
} 