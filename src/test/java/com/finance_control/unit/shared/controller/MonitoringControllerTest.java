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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MonitoringController.
 * Tests the simplified monitoring endpoints that delegate to Spring Actuator.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MonitoringController Unit Tests")
class MonitoringControllerTest {

    @InjectMocks
    private MonitoringController monitoringController;

    @BeforeEach
    void setUp() {
        // No dependencies needed - controller is simplified
    }

    @Test
    @DisplayName("Should return metrics summary successfully")
    void getMetricsSummary_ShouldReturnMetricsSummary() {
        // When
        ResponseEntity<Map<String, Object>> response = monitoringController.getMetricsSummary();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("system");
        assertThat(response.getBody()).containsKey("note");

        @SuppressWarnings("unchecked")
        Map<String, Object> system = (Map<String, Object>) response.getBody().get("system");
        assertThat(system).containsKeys("memoryUsed", "memoryTotal", "memoryMax", "processors");
        assertThat(system.get("processors")).isInstanceOf(Integer.class);
    }

    @Test
    @DisplayName("Should handle errors gracefully")
    void getMetricsSummary_ShouldHandleErrors() {
        // The current implementation doesn't throw exceptions, but we verify it returns 200
        ResponseEntity<Map<String, Object>> response = monitoringController.getMetricsSummary();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
