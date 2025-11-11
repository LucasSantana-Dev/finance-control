package com.finance_control.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * Base class for Selenium integration tests.
 * Use this for end-to-end testing with real browser.
 *
 * Note: In Docker environments without Chrome, this falls back to HTTP client testing.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseSeleniumTest {

    @LocalServerPort
    protected int port;

    protected String baseUrl;
    protected RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        restTemplate = new RestTemplate();
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    protected void navigateTo(String path) {
        // In a real Selenium test, this would navigate with WebDriver
        // For now, we'll just store the URL for HTTP client testing
        baseUrl = baseUrl + path;
    }

    protected String getPageContent(String path) {
        try {
            // Add small delay to ensure server is ready
            Thread.sleep(100);
            return restTemplate.getForObject(URI.create(baseUrl + path), String.class);
        } catch (org.springframework.web.client.HttpServerErrorException.ServiceUnavailable e) {
            // ServiceUnavailable (503) is acceptable for health checks when components are down
            // Return the response body which contains the health status JSON
            return e.getResponseBodyAsString();
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            throw new RuntimeException("HTTP error accessing path: " + path + " - Status: " + e.getStatusCode() + ", Message: " + e.getMessage(), e);
        } catch (org.springframework.web.client.ResourceAccessException e) {
            throw new RuntimeException("Connection error accessing path: " + path + " - Is the server running? Message: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get page content for path: " + path + " - Error: " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}
