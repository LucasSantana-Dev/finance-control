package com.finance_control.unit.shared.controller;

import com.finance_control.shared.controller.MonitoringController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MonitoringController (simplified version).
 * Tests basic monitoring endpoints that delegate to Spring Actuator.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringController Unit Tests")
class MonitoringControllerUnitTest {

    @InjectMocks
    private MonitoringController monitoringController;

    @BeforeEach
    void setUp() {
        // No dependencies needed - controller is simplified
    }

    @Test
    @DisplayName("Should return health status successfully")
    void getMetricsSummary_ShouldReturnMetricsSummary() {
        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getMetricsSummary();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("system"));
        assertTrue(body.containsKey("note"));

        @SuppressWarnings("unchecked")
        Map<String, Object> system = (Map<String, Object>) body.get("system");
        assertThat(system).containsKeys("memoryUsed", "memoryTotal", "memoryMax", "processors");
    }
}
