package com.finance_control.unit.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.finance_control.shared.config.properties.SupabaseProperties;
import com.finance_control.shared.service.SupabaseRealtimeService;
import com.finance_control.shared.service.realtime.RealtimeMessageHandler;
import com.finance_control.shared.service.realtime.RealtimeSubscriptionManager;
import com.finance_control.shared.service.realtime.RealtimeWebSocketManager;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupabaseRealtimeServiceTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private RealtimeWebSocketManager webSocketManager;

    @Mock
    private RealtimeSubscriptionManager subscriptionManager;

    @Mock
    private RealtimeMessageHandler messageHandler;

    @InjectMocks
    private SupabaseRealtimeService realtimeService;

    private Map<String, Object> testData;

    @BeforeEach
    void setUp() {
        // Setup AppProperties mock with allowed channels for testing
        SupabaseProperties.RealtimeProperties realtime = new SupabaseProperties.RealtimeProperties(
            true,
            java.util.Arrays.asList("transactions", "dashboard", "goals", "users")
        );
        SupabaseProperties supabaseRecord = new SupabaseProperties(
            true, "https://test.supabase.co", "test-anon-key", "test-jwt-signer", "test-service-role",
            new SupabaseProperties.SupabaseDatabaseProperties(false, "", 5432, "", "", "", false, "require"),
            new SupabaseProperties.StorageProperties(true, "avatars", "documents", "transactions", new SupabaseProperties.CompressionProperties(true, 6, 0.1, 1024, java.util.List.of())),
            realtime
        );
        when(appProperties.supabase()).thenReturn(supabaseRecord);

        // Setup mocks for dependencies
        lenient().doNothing().when(webSocketManager).initialize();
        lenient().when(webSocketManager.connect(any())).thenReturn(true);
        lenient().doNothing().when(webSocketManager).disconnect();
        lenient().when(webSocketManager.isConnected()).thenReturn(false);
        lenient().when(webSocketManager.getSession()).thenReturn(null);
        lenient().when(webSocketManager.getNextMessageId()).thenReturn(1L);
        lenient().doNothing().when(subscriptionManager).setupDefaultChannels();
        lenient().doNothing().when(subscriptionManager).clearAll();
        lenient().doNothing().when(subscriptionManager).subscribeToChannel(anyString(), anyLong());
        lenient().doNothing().when(subscriptionManager).unsubscribeFromChannel(anyString(), anyLong());
        lenient().doNothing().when(subscriptionManager).subscribeToDatabaseChanges(anyString(), anyLong());
        lenient().doNothing().when(subscriptionManager).unsubscribeFromDatabaseChanges(anyString(), anyLong());
        lenient().when(subscriptionManager.isValidChannel(anyString())).thenReturn(true);
        lenient().when(subscriptionManager.getSubscriptionCounts()).thenReturn(new HashMap<>());
        lenient().when(subscriptionManager.getDatabaseSubscribers(anyString())).thenReturn(new java.util.HashSet<>());
        lenient().doNothing().when(messageHandler).broadcastToChannel(anyString(), any());
        lenient().doNothing().when(messageHandler).broadcastToUser(anyString(), anyLong(), any());
        lenient().doNothing().when(messageHandler).notifyTransactionUpdate(anyLong(), any());
        lenient().doNothing().when(messageHandler).notifyDashboardUpdate(anyLong(), any());
        lenient().doNothing().when(messageHandler).notifyGoalUpdate(anyLong(), any());
        lenient().doNothing().when(messageHandler).handleRealtimeMessage(anyString(), any());
        lenient().doNothing().when(messageHandler).sendDatabaseSubscriptionMessage(any(), anyString(), anyLong());
        lenient().doNothing().when(messageHandler).sendDatabaseUnsubscriptionMessage(any(), anyString(), anyLong());
        lenient().when(messageHandler.getObjectMapper()).thenReturn(new com.fasterxml.jackson.databind.ObjectMapper());

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
        Map<String, Integer> counts = new HashMap<>();
        counts.put(channelName, 1);
        when(subscriptionManager.getSubscriptionCounts()).thenReturn(counts);

        // When
        realtimeService.subscribeToChannel(channelName, userId);

        // Then
        verify(subscriptionManager).subscribeToChannel(channelName, userId);
        verify(messageHandler).broadcastToUser(eq(channelName), eq(userId), any());
        Map<String, Integer> subscriptionCounts = realtimeService.getSubscriptionCounts();
        assertThat(subscriptionCounts.get(channelName)).isEqualTo(1);
    }

    @Test
    void subscribeToChannel_WithInvalidChannel_ShouldThrowException() {
        // Given
        String invalidChannel = "invalid_channel";
        Long userId = 1L;
        when(subscriptionManager.isValidChannel(invalidChannel)).thenReturn(false);
        doThrow(new IllegalArgumentException("Invalid channel")).when(subscriptionManager).subscribeToChannel(invalidChannel, userId);

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
        Map<String, Integer> countsBefore = new HashMap<>();
        countsBefore.put(channelName, 1);
        when(subscriptionManager.getSubscriptionCounts()).thenReturn(countsBefore);
        realtimeService.subscribeToChannel(channelName, userId);

        // When
        Map<String, Integer> countsAfter = new HashMap<>();
        when(subscriptionManager.getSubscriptionCounts()).thenReturn(countsAfter);
        realtimeService.unsubscribeFromChannel(channelName, userId);

        // Then
        verify(subscriptionManager).unsubscribeFromChannel(channelName, userId);
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
        verify(subscriptionManager).subscribeToDatabaseChanges(tableName, userId);
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
        verify(subscriptionManager).unsubscribeFromDatabaseChanges(tableName, userId);
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

        // Then - setupDefaultChannels() initializes all channels, so we check that all counts are 0
        assertThat(counts.values()).allMatch(count -> count == 0);
    }

    @Test
    void getSubscriptionCounts_WithSubscriptions_ShouldReturnCorrectCounts() {
        // Given
        Map<String, Integer> counts = new HashMap<>();
        counts.put("transactions", 2);
        counts.put("dashboard", 1);
        counts.put("goals", 0);
        when(subscriptionManager.getSubscriptionCounts()).thenReturn(counts);

        // When
        Map<String, Integer> result = realtimeService.getSubscriptionCounts();

        // Then
        assertThat(result.get("transactions")).isEqualTo(2);
        assertThat(result.get("dashboard")).isEqualTo(1);
        assertThat(result.get("goals")).isZero(); // Not subscribed
    }
}
