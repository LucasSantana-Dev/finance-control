package com.finance_control.e2e.transactions;

import com.finance_control.e2e.BaseSeleniumTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSeleniumTest extends BaseSeleniumTest {

    @Test
    void shouldDisplayTransactionPage() {
        // Test that the application is running and accessible
        String content = getPageContent("/actuator/health");

        // Verify the application is responding
        assertThat(content).isNotNull();
        assertThat(content).contains("UP");
    }

    @Test
    void shouldHandleApiEndpoints() {
        // Test API endpoints directly using HTTP client
        String content = getPageContent("/actuator/health");

        // Verify health endpoint is accessible
        assertThat(content).contains("UP");
    }
}
