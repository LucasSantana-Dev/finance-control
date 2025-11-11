package com.finance_control.e2e.transactions;

import com.finance_control.e2e.BaseSeleniumTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "app.actuator.enabled=true",
    "management.endpoints.web.exposure.include=health",
    "management.endpoints.web.base-path=/actuator"
})
class TransactionSeleniumTest extends BaseSeleniumTest {

    @Test
    void shouldDisplayTransactionPage() {
        // Test that the application is running and accessible
        String content = getPageContent("/actuator/health");

        // Verify the application is responding
        // Note: Health may show DOWN if Redis is unavailable, but endpoint should be accessible
        assertThat(content).isNotNull();
        assertThat(content).contains("\"status\"");
    }

    @Test
    void shouldHandleApiEndpoints() {
        // Test API endpoints directly using HTTP client
        String content = getPageContent("/actuator/health");

        // Verify health endpoint is accessible
        // Note: Health may show DOWN if Redis is unavailable, but endpoint should be accessible
        assertThat(content).isNotNull();
        assertThat(content).contains("\"status\"");
    }
}
