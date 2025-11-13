package com.finance_control.integration;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.service.SupabaseRealtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SupabaseRealtimeService.
 * These tests require a real Supabase project and are disabled by default.
 * To run these tests:
 * 1. Set up a Supabase project with Realtime enabled
 * 2. Create the required tables (transactions, goals, profiles)
 * 3. Enable Row Level Security and create policies
 * 4. Set environment variables: SUPABASE_URL, SUPABASE_ANON_KEY, SUPABASE_REALTIME_ENABLED=true
 * 5. Remove @Disabled annotation
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires real Supabase project setup with Realtime enabled")
class SupabaseRealtimeIntegrationTest {

    @Autowired
    private SupabaseRealtimeService realtimeService;

    @Autowired
    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        // Wait for service to initialize
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void connect_ShouldEstablishWebSocketConnection() {
        // When
        realtimeService.connect();

        // Then
        assertThat(realtimeService.isConnected()).isTrue();
    }

    @Test
    void subscribeToChannel_WithValidChannel_ShouldSubscribeSuccessfully() {
        // Given
        String channelName = "transactions";
        Long userId = 999L; // Test user ID

        // When
        realtimeService.subscribeToChannel(channelName, userId);

        // Then
        Map<String, Integer> subscriptionCounts = realtimeService.getSubscriptionCounts();
        assertThat(subscriptionCounts.get(channelName)).isEqualTo(1);
    }

    @Test
    void broadcastToChannel_WithConnectedService_ShouldBroadcastMessage() {
        // Given
        String channelName = "transactions";
        Long userId = 999L;
        realtimeService.subscribeToChannel(channelName, userId);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "test_message");
        message.put("data", "integration test message");
        message.put("timestamp", System.currentTimeMillis());

        // When
        realtimeService.broadcastToChannel(channelName, message);

        // Then
        // Message should be broadcast to subscribers
        // In a real integration test, you would verify the message was received
        // by a test client connected to the WebSocket
    }

    @Test
    void subscribeToDatabaseChanges_WithValidTable_ShouldSubscribeToChanges() {
        // Given
        String tableName = "transactions";
        Long userId = 999L;

        // When
        realtimeService.subscribeToDatabaseChanges(tableName, userId);

        // Then
        // Database change subscription should be active
        // In a real test, you would insert/update records and verify notifications
    }

    @Test
    void notifyTransactionUpdate_WithConnectedService_ShouldSendNotification() {
        // Given
        Long userId = 999L;
        Map<String, Object> transactionData = new HashMap<>();
        transactionData.put("id", 12345L);
        transactionData.put("userId", userId);
        transactionData.put("amount", 100.0);
        transactionData.put("description", "Test transaction");

        // When
        realtimeService.notifyTransactionUpdate(userId, transactionData);

        // Then
        // Notification should be sent to transaction channel subscribers
    }

    @Test
    void notifyDashboardUpdate_WithConnectedService_ShouldSendNotification() {
        // Given
        Long userId = 999L;
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("userId", userId);
        dashboardData.put("timestamp", System.currentTimeMillis());
        dashboardData.put("type", "dashboard_update");

        // When
        realtimeService.notifyDashboardUpdate(userId, dashboardData);

        // Then
        // Notification should be sent to dashboard channel subscribers
    }

    @Test
    void notifyGoalUpdate_WithConnectedService_ShouldSendNotification() {
        // Given
        Long userId = 999L;
        Map<String, Object> goalData = new HashMap<>();
        goalData.put("id", 12345L);
        goalData.put("userId", userId);
        goalData.put("targetAmount", 1000.0);
        goalData.put("currentAmount", 500.0);

        // When
        realtimeService.notifyGoalUpdate(userId, goalData);

        // Then
        // Notification should be sent to goals channel subscribers
    }

    @Test
    void disconnect_ShouldCloseWebSocketConnection() {
        // Given - service is connected
        realtimeService.connect();
        assertThat(realtimeService.isConnected()).isTrue();

        // When
        realtimeService.disconnect();

        // Then
        assertThat(realtimeService.isConnected()).isFalse();
    }

    @Test
    void handleRealtimeMessage_WithDatabaseEvent_ShouldProcessEvent() {
        // Given
        Map<String, Object> databaseEvent = new HashMap<>();
        databaseEvent.put("eventType", "INSERT");
        databaseEvent.put("new", Map.of(
            "user_id", 999L,
            "id", 12345L,
            "amount", 100.0,
            "description", "Database test transaction"
        ));

        // When
        realtimeService.handleRealtimeMessage("transactions", databaseEvent);

        // Then
        // Database event should be processed and notifications sent
        // In a real test, you would verify that subscribers received the notification
    }

    @Test
    void getSubscriptionCounts_WithActiveSubscriptions_ShouldReturnCorrectCounts() {
        // Given
        realtimeService.subscribeToChannel("transactions", 999L);
        realtimeService.subscribeToChannel("transactions", 1000L);
        realtimeService.subscribeToChannel("dashboard", 999L);

        // When
        Map<String, Integer> counts = realtimeService.getSubscriptionCounts();

        // Then
        assertThat(counts.get("transactions")).isEqualTo(2);
        assertThat(counts.get("dashboard")).isEqualTo(1);
        assertThat(counts.get("goals")).isZero();
    }
}
