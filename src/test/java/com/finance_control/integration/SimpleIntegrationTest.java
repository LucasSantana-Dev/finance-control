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
        "org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration"
})
class SimpleIntegrationTest {

    @Autowired
    private Environment environment;

    @Test
    void contextLoads() {
        // Se este teste passar, sabemos que o contexto carrega corretamente
        assertThat(true).isTrue();
    }

    @Test
    void testBasicConfiguration() {
        // Basic test to verify if configurations are loading
        assertThat(environment.getActiveProfiles()).contains("test");
    }
}
