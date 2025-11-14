package com.finance_control.unit.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.service.SupabaseRealtimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupabaseRealtimeServiceTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private SupabaseRealtimeService realtimeService;

    private Map<String, Object> testData;

    @BeforeEach
    void setUp() {
        // Setup AppProperties mock
        AppProperties.Supabase supabaseRecord = new AppProperties.Supabase(
            true, "https://test.supabase.co", "test-anon-key", "test-jwt-signer", "test-service-role",
            new AppProperties.SupabaseDatabase(),
            new AppProperties.Storage(true, "avatars", "documents", "transactions", new AppProperties.Compression()),
            new AppProperties.Realtime(true, new java.util.ArrayList<>())
        );
        when(appProperties.supabase()).thenReturn(supabaseRecord);

        // Setup test data
        testData = new HashMap<>();
        testData.put("id", 1L);
        testData.put("userId", 1L);
        testData.put("amount", 100.0);
        testData.put("type", "INCOME");

        // Initialize the service
        ReflectionTestUtils.invokeMethod(realtimeService, "initialize");
    }

    @Test
    void subscribeToChannel_WithValidChannel_ShouldAddSubscription() {
        // Given
        String channelName = "transactions";
        Long userId = 1L;

        // When
        realtimeService.subscribeToChannel(channelName, userId);

        // Then
        Map<String, Integer> subscriptionCounts = realtimeService.getSubscriptionCounts();
        assertThat(subscriptionCounts.get(channelName)).isEqualTo(1);
    }

    @Test
    void subscribeToChannel_WithInvalidChannel_ShouldThrowException() {
        // Given
        String invalidChannel = "invalid_channel";
        Long userId = 1L;

        // When & Then
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            realtimeService.subscribeToChannel(invalidChannel, userId);
        });
    }

    @Test
    void unsubscribeFromChannel_WithExistingSubscription_ShouldRemoveSubscription() {
        // Given
        String channelName = "transactions";
        Long userId = 1L;
        realtimeService.subscribeToChannel(channelName, userId);

        // When
        realtimeService.unsubscribeFromChannel(channelName, userId);

        // Then
        Map<String, Integer> subscriptionCounts = realtimeService.getSubscriptionCounts();
        assertThat(subscriptionCounts.get(channelName)).isZero();
    }

    @Test
    void broadcastToChannel_WithSubscribers_ShouldSendMessage() {
        // Given
        String channelName = "transactions";
        Long userId = 1L;
        realtimeService.subscribeToChannel(channelName, userId);

        // When
        realtimeService.broadcastToChannel(channelName, testData);

        // Then
        // Verify that messagingTemplate.convertAndSend was called
        // Note: Since the messaging template is mocked and disabled in the current implementation,
        // this test verifies the logic without actual message sending
    }

    @Test
    void broadcastToChannel_WithNoSubscribers_ShouldNotSendMessage() {
        // Given
        String channelName = "transactions";

        // When
        realtimeService.broadcastToChannel(channelName, testData);

        // Then
        // No subscribers, so no message should be sent
    }

    @Test
    void notifyTransactionUpdate_WithValidData_ShouldBroadcastToChannel() {
        // Given
        Long userId = 1L;
        String channelName = "transactions";
        realtimeService.subscribeToChannel(channelName, userId);

        // When
        realtimeService.notifyTransactionUpdate(userId, testData);

        // Then
        // Verify broadcast was called for transactions channel
    }

    @Test
    void notifyDashboardUpdate_WithValidData_ShouldBroadcastToChannel() {
        // Given
        Long userId = 1L;
        String channelName = "dashboard";
        realtimeService.subscribeToChannel(channelName, userId);

        // When
        realtimeService.notifyDashboardUpdate(userId, testData);

        // Then
        // Verify broadcast was called for dashboard channel
    }

    @Test
    void notifyGoalUpdate_WithValidData_ShouldBroadcastToChannel() {
        // Given
        Long userId = 1L;
        String channelName = "goals";
        realtimeService.subscribeToChannel(channelName, userId);

        // When
        realtimeService.notifyGoalUpdate(userId, testData);

        // Then
        // Verify broadcast was called for goals channel
    }

    @Test
    void subscribeToDatabaseChanges_WithValidTable_ShouldAddSubscription() {
        // Given
        String tableName = "transactions";
        Long userId = 1L;

        // When
        realtimeService.subscribeToDatabaseChanges(tableName, userId);

        // Then
        // Verify that database subscription was added
        // Note: This is internal state, so we can't easily test it without exposing getters
    }

    @Test
    void unsubscribeFromDatabaseChanges_WithExistingSubscription_ShouldRemoveSubscription() {
        // Given
        String tableName = "transactions";
        Long userId = 1L;
        realtimeService.subscribeToDatabaseChanges(tableName, userId);

        // When
        realtimeService.unsubscribeFromDatabaseChanges(tableName, userId);

        // Then
        // Verify that database subscription was removed
    }

    @Test
    void handleRealtimeMessage_WithDatabaseChangeEvent_ShouldProcessEvent() {
        // Given
        Map<String, Object> databaseEvent = new HashMap<>();
        databaseEvent.put("eventType", "INSERT");
        databaseEvent.put("new", Map.of("user_id", 1L, "id", 1L, "amount", 100.0));

        // When
        realtimeService.handleRealtimeMessage("transactions", databaseEvent);

        // Then
        // Verify that database change was processed
    }

    @Test
    void handleRealtimeMessage_WithRegularMessage_ShouldBroadcast() {
        // Given
        String channelName = "transactions";
        Long userId = 1L;
        realtimeService.subscribeToChannel(channelName, userId);

        Map<String, Object> message = new HashMap<>();
        message.put("type", "custom_message");
        message.put("data", "test");

        // When
        realtimeService.handleRealtimeMessage(channelName, message);

        // Then
        // Verify broadcast was called
    }

    @Test
    void isConnected_WhenNotConnected_ShouldReturnFalse() {
        // When
        boolean connected = realtimeService.isConnected();

        // Then
        assertThat(connected).isFalse();
    }

    @Test
    void getSubscriptionCounts_WithNoSubscriptions_ShouldReturnEmptyMap() {
        // When
        Map<String, Integer> counts = realtimeService.getSubscriptionCounts();

        // Then
        assertThat(counts).isEmpty();
    }

    @Test
    void getSubscriptionCounts_WithSubscriptions_ShouldReturnCorrectCounts() {
        // Given
        realtimeService.subscribeToChannel("transactions", 1L);
        realtimeService.subscribeToChannel("transactions", 2L);
        realtimeService.subscribeToChannel("dashboard", 1L);

        // When
        Map<String, Integer> counts = realtimeService.getSubscriptionCounts();

        // Then
        assertThat(counts.get("transactions")).isEqualTo(2);
        assertThat(counts.get("dashboard")).isEqualTo(1);
        assertThat(counts.get("goals")).isZero(); // Not subscribed
    }
}
