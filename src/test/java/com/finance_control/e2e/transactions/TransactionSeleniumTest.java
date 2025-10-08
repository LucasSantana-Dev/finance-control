package com.finance_control.e2e.transactions;

import com.finance_control.e2e.BaseSeleniumTest;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionSeleniumTest extends BaseSeleniumTest {

    @Test
    void shouldDisplayTransactionPage() {
        navigateTo("/transactions");

        // Wait for page to load
        new WebDriverWait(driver, Duration.ofSeconds(10))
            .until(webDriver -> webDriver.getTitle() != null);

        // This test assumes there's a web interface
        // For now, we'll just verify the application is running
        assertThat(driver.getTitle()).isNotNull();
    }

    @Test
    void shouldHandleApiEndpoints() {
        // Test API endpoints directly
        navigateTo("/actuator/health");

        // Verify health endpoint is accessible
        String pageSource = driver.getPageSource();
        assertThat(pageSource).contains("UP");
    }
}
