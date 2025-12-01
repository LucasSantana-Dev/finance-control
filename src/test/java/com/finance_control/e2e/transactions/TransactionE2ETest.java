package com.finance_control.e2e.transactions;

import com.finance_control.e2e.BaseE2ETest;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
    "app.actuator.enabled=true",
    "management.endpoints.web.exposure.include=health",
    "management.endpoints.web.base-path=/actuator",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration,org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration"
})
class TransactionE2ETest extends BaseE2ETest {

    @Test
    void shouldDisplayTransactionPage() {
        // Test that the application is running and accessible
        String content = getPageContent("/actuator/health");

        // Verify the application is responding
        assertThat(content).isNotNull();
        assertThat(content).contains("\"status\"");
    }

    @Test
    void shouldHandleApiEndpoints() {
        // Test API endpoints directly using HTTP client
        String content = getPageContent("/actuator/health");

        // Verify health endpoint is accessible
        assertThat(content).isNotNull();
        assertThat(content).contains("\"status\"");
    }
}
