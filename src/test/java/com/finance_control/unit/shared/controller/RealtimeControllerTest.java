package com.finance_control.unit.shared.controller;

import com.finance_control.shared.controller.RealtimeController;
import com.finance_control.shared.service.SupabaseRealtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealtimeControllerTest {

    @Mock
    private SupabaseRealtimeService realtimeService;

    @InjectMocks
    private RealtimeController controller;

    @BeforeEach
    void setUp() {
        lenient().when(realtimeService.getSubscriptionCounts()).thenReturn(new HashMap<>());
    }

    @Test
    void getStatus_WhenConnected_ShouldReturnOk() {
        when(realtimeService.isConnected()).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("connected")).isEqualTo(true);
        assertThat(response.getBody().containsKey("subscriptionCounts")).isTrue();
        assertThat(response.getBody().containsKey("timestamp")).isTrue();
    }

    @Test
    void getStatus_WhenNotConnected_ShouldReturnServiceUnavailable() {
        when(realtimeService.isConnected()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.getStatus();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("connected")).isEqualTo(false);
    }

    @Test
    void subscribeToChannel_WhenConnected_ShouldReturnSuccess() {
        when(realtimeService.isConnected()).thenReturn(true);
        doNothing().when(realtimeService).subscribeToChannel(anyString(), anyLong());

        ResponseEntity<Map<String, Object>> response = controller.subscribeToChannel("transactions", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("channel")).isEqualTo("transactions");
        assertThat(response.getBody().get("userId")).isEqualTo(1L);
        verify(realtimeService).subscribeToChannel("transactions", 1L);
    }

    @Test
    void subscribeToChannel_WhenNotConnected_ShouldReturnServiceUnavailable() {
        when(realtimeService.isConnected()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.subscribeToChannel("transactions", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Realtime service is not available");
        verify(realtimeService, never()).subscribeToChannel(anyString(), anyLong());
    }

    @Test
    void subscribeToChannel_WhenInvalidChannel_ShouldReturnBadRequest() {
        when(realtimeService.isConnected()).thenReturn(true);
        doThrow(new IllegalArgumentException("Invalid channel")).when(realtimeService)
                .subscribeToChannel(anyString(), anyLong());

        ResponseEntity<Map<String, Object>> response = controller.subscribeToChannel("invalid", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Invalid channel");
    }

    @Test
    void subscribeToChannel_WhenException_ShouldReturnInternalServerError() {
        when(realtimeService.isConnected()).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(realtimeService)
                .subscribeToChannel(anyString(), anyLong());

        ResponseEntity<Map<String, Object>> response = controller.subscribeToChannel("transactions", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Failed to subscribe to channel");
    }

    @Test
    void unsubscribeFromChannel_WhenConnected_ShouldReturnSuccess() {
        when(realtimeService.isConnected()).thenReturn(true);
        doNothing().when(realtimeService).unsubscribeFromChannel(anyString(), anyLong());

        ResponseEntity<Map<String, Object>> response = controller.unsubscribeFromChannel("transactions", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("channel")).isEqualTo("transactions");
        assertThat(response.getBody().get("userId")).isEqualTo(1L);
        verify(realtimeService).unsubscribeFromChannel("transactions", 1L);
    }

    @Test
    void unsubscribeFromChannel_WhenNotConnected_ShouldReturnServiceUnavailable() {
        when(realtimeService.isConnected()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = controller.unsubscribeFromChannel("transactions", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Realtime service is not available");
        verify(realtimeService, never()).unsubscribeFromChannel(anyString(), anyLong());
    }

    @Test
    void unsubscribeFromChannel_WhenException_ShouldReturnInternalServerError() {
        when(realtimeService.isConnected()).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(realtimeService)
                .unsubscribeFromChannel(anyString(), anyLong());

        ResponseEntity<Map<String, Object>> response = controller.unsubscribeFromChannel("transactions", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Failed to unsubscribe from channel");
    }

    @Test
    void broadcastToChannel_WhenConnected_ShouldReturnSuccess() {
        when(realtimeService.isConnected()).thenReturn(true);
        doNothing().when(realtimeService).broadcastToChannel(anyString(), any(Object.class));

        Map<String, Object> message = Map.of("type", "update", "data", "test");

        ResponseEntity<Map<String, Object>> response = controller.broadcastToChannel("transactions", message);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("channel")).isEqualTo("transactions");
        verify(realtimeService).broadcastToChannel("transactions", message);
    }

    @Test
    void broadcastToChannel_WhenNotConnected_ShouldReturnServiceUnavailable() {
        when(realtimeService.isConnected()).thenReturn(false);

        Map<String, Object> message = Map.of("type", "update");

        ResponseEntity<Map<String, Object>> response = controller.broadcastToChannel("transactions", message);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Realtime service is not available");
        verify(realtimeService, never()).broadcastToChannel(anyString(), any(Object.class));
    }

    @Test
    void broadcastToChannel_WhenException_ShouldReturnInternalServerError() {
        when(realtimeService.isConnected()).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(realtimeService)
                .broadcastToChannel(anyString(), any(Object.class));

        Map<String, Object> message = Map.of("type", "update");

        ResponseEntity<Map<String, Object>> response = controller.broadcastToChannel("transactions", message);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Failed to broadcast message");
    }

    @Test
    void notifyTransactionUpdate_WhenConnected_ShouldReturnSuccess() {
        when(realtimeService.isConnected()).thenReturn(true);
        doNothing().when(realtimeService).notifyTransactionUpdate(anyLong(), any(Object.class));

        Map<String, Object> transactionData = Map.of("id", 1L, "amount", 100.0);

        ResponseEntity<Map<String, Object>> response = controller.notifyTransactionUpdate(1L, transactionData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("type")).isEqualTo("transaction_update");
        assertThat(response.getBody().get("userId")).isEqualTo(1L);
        verify(realtimeService).notifyTransactionUpdate(1L, transactionData);
    }

    @Test
    void notifyTransactionUpdate_WhenNotConnected_ShouldReturnServiceUnavailable() {
        when(realtimeService.isConnected()).thenReturn(false);

        Map<String, Object> transactionData = Map.of("id", 1L);

        ResponseEntity<Map<String, Object>> response = controller.notifyTransactionUpdate(1L, transactionData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Realtime service is not available");
        verify(realtimeService, never()).notifyTransactionUpdate(anyLong(), any(Object.class));
    }

    @Test
    void notifyTransactionUpdate_WhenException_ShouldReturnInternalServerError() {
        when(realtimeService.isConnected()).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(realtimeService)
                .notifyTransactionUpdate(anyLong(), any(Object.class));

        Map<String, Object> transactionData = Map.of("id", 1L);

        ResponseEntity<Map<String, Object>> response = controller.notifyTransactionUpdate(1L, transactionData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Failed to send notification");
    }

    @Test
    void notifyDashboardUpdate_WhenConnected_ShouldReturnSuccess() {
        when(realtimeService.isConnected()).thenReturn(true);
        doNothing().when(realtimeService).notifyDashboardUpdate(anyLong(), any(Object.class));

        Map<String, Object> dashboardData = Map.of("summary", "test");

        ResponseEntity<Map<String, Object>> response = controller.notifyDashboardUpdate(1L, dashboardData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("type")).isEqualTo("dashboard_update");
        assertThat(response.getBody().get("userId")).isEqualTo(1L);
        verify(realtimeService).notifyDashboardUpdate(1L, dashboardData);
    }

    @Test
    void notifyDashboardUpdate_WhenNotConnected_ShouldReturnServiceUnavailable() {
        when(realtimeService.isConnected()).thenReturn(false);

        Map<String, Object> dashboardData = Map.of("summary", "test");

        ResponseEntity<Map<String, Object>> response = controller.notifyDashboardUpdate(1L, dashboardData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Realtime service is not available");
        verify(realtimeService, never()).notifyDashboardUpdate(anyLong(), any(Object.class));
    }

    @Test
    void notifyDashboardUpdate_WhenException_ShouldReturnInternalServerError() {
        when(realtimeService.isConnected()).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(realtimeService)
                .notifyDashboardUpdate(anyLong(), any(Object.class));

        Map<String, Object> dashboardData = Map.of("summary", "test");

        ResponseEntity<Map<String, Object>> response = controller.notifyDashboardUpdate(1L, dashboardData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Failed to send notification");
    }

    @Test
    void notifyGoalUpdate_WhenConnected_ShouldReturnSuccess() {
        when(realtimeService.isConnected()).thenReturn(true);
        doNothing().when(realtimeService).notifyGoalUpdate(anyLong(), any(Object.class));

        Map<String, Object> goalData = Map.of("id", 1L, "progress", 50.0);

        ResponseEntity<Map<String, Object>> response = controller.notifyGoalUpdate(1L, goalData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("type")).isEqualTo("goal_update");
        assertThat(response.getBody().get("userId")).isEqualTo(1L);
        verify(realtimeService).notifyGoalUpdate(1L, goalData);
    }

    @Test
    void notifyGoalUpdate_WhenNotConnected_ShouldReturnServiceUnavailable() {
        when(realtimeService.isConnected()).thenReturn(false);

        Map<String, Object> goalData = Map.of("id", 1L);

        ResponseEntity<Map<String, Object>> response = controller.notifyGoalUpdate(1L, goalData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Realtime service is not available");
        verify(realtimeService, never()).notifyGoalUpdate(anyLong(), any(Object.class));
    }

    @Test
    void notifyGoalUpdate_WhenException_ShouldReturnInternalServerError() {
        when(realtimeService.isConnected()).thenReturn(true);
        doThrow(new RuntimeException("Unexpected error")).when(realtimeService)
                .notifyGoalUpdate(anyLong(), any(Object.class));

        Map<String, Object> goalData = Map.of("id", 1L);

        ResponseEntity<Map<String, Object>> response = controller.notifyGoalUpdate(1L, goalData);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("Failed to send notification");
    }
}
