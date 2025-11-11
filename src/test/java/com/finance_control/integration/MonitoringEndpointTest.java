package com.finance_control.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.security.public-endpoints=/monitoring/**,/api/monitoring/**,/actuator/**",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisReactiveHealthContributorAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration"
})
@DisplayName("Monitoring Endpoint Test")
class MonitoringEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Should return 200 for health endpoint")
    void healthEndpoint_ShouldReturn200() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/monitoring/health", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return 200 for status endpoint")
    void statusEndpoint_ShouldReturn200() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/monitoring/status", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should return 200 for metrics summary endpoint")
    void metricsSummaryEndpoint_ShouldReturn200() {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/monitoring/metrics/summary", Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
