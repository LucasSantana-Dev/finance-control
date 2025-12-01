package com.finance_control.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.security.public-endpoints=/monitoring/**,/api/monitoring/**,/actuator/**",
    "app.feature-flags.monitoring.enabled=true",
    "app.actuator.enabled=true",
    "management.endpoints.web.exposure.include=health",
    "management.endpoints.web.base-path=/actuator",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisReactiveHealthContributorAutoConfiguration,org.springframework.boot.actuate.autoconfigure.data.redis.RedisHealthContributorAutoConfiguration,org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration"
})
@DisplayName("Monitoring Endpoint Test")
class MonitoringEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 200 for metrics summary endpoint")
    void metricsSummaryEndpoint_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/monitoring/metrics/summary"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 200 for actuator health endpoint")
    void actuatorHealthEndpoint_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
