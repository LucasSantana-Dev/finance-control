package com.finance_control.unit;

import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for controller unit tests using @WebMvcTest.
 * These are slice tests that only load the web layer (controllers, filters, etc.)
 * and mock all service dependencies.
 */
@ActiveProfiles("test")
public abstract class BaseWebMvcTest {
    // Common setup for all WebMvcTest-based controller unit tests
} 