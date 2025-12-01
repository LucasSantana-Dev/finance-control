package com.finance_control.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple integration test to validate if the context loads correctly
 * without complex security dependencies.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration," +
        "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration," +
        "org.springframework.boot.actuate.autoconfigure.data.redis.RedisReactiveHealthContributorAutoConfiguration," +
        "org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration," +
        "org.springframework.boot.autoconfigure.http.client.HttpClientAutoConfiguration," +
        "org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration"
})
class SimpleIntegrationTest {

    @Autowired
    private Environment environment;

    @Test
    void shouldLoadApplicationContextSuccessfully() {
        // If this test runs, the context loaded successfully
        // No assertion needed - test failure would indicate context loading issues
        assertThat(environment).isNotNull();
    }

    @Test
    void shouldHaveTestProfileActive() {
        // Verify test profile is active
        assertThat(environment.getActiveProfiles()).contains("test");
    }
}
