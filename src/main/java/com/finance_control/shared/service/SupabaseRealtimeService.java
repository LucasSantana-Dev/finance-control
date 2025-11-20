package com.finance_control.shared.service;

import com.finance_control.shared.service.realtime.RealtimeMessageHandler;
import com.finance_control.shared.service.realtime.RealtimeSubscriptionManager;
import com.finance_control.shared.service.realtime.RealtimeWebSocketHandler;
import com.finance_control.shared.service.realtime.RealtimeWebSocketManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;

/**
 * Service for managing Supabase Realtime subscriptions.
 * Handles WebSocket connections and broadcasts realtime updates to clients.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.realtime.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseRealtimeService {

    private final RealtimeWebSocketManager webSocketManager;
    private final RealtimeSubscriptionManager subscriptionManager;
    private final RealtimeMessageHandler messageHandler;

    @PostConstruct
    public void initialize() {
        log.info("Initializing Supabase Realtime service");
        try {
            webSocketManager.initialize();
            RealtimeWebSocketHandler handler = new RealtimeWebSocketHandler(
                    messageHandler.getObjectMapper(), messageHandler, webSocketManager);
            webSocketManager.connect(handler);
            subscriptionManager.setupDefaultChannels();
        } catch (Exception e) {
            log.error("Failed to initialize Supabase Realtime service", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Supabase Realtime service");
        webSocketManager.disconnect();
        subscriptionManager.clearAll();
    }

    public void connect() {
        RealtimeWebSocketHandler handler = new RealtimeWebSocketHandler(
                messageHandler.getObjectMapper(), messageHandler, webSocketManager);
        webSocketManager.connect(handler);
    }

    public void disconnect() {
        webSocketManager.disconnect();
        subscriptionManager.clearAll();
    }

    public void subscribeToChannel(String channelName, Long userId) {
        subscriptionManager.subscribeToChannel(channelName, userId);
        Map<String, Object> subscriptionMessage = new java.util.HashMap<>();
        subscriptionMessage.put("type", "subscription_confirmed");
        subscriptionMessage.put("channel", channelName);
        subscriptionMessage.put("userId", userId);
        subscriptionMessage.put("timestamp", System.currentTimeMillis());
        messageHandler.broadcastToUser(channelName, userId, subscriptionMessage);
    }

    public void unsubscribeFromChannel(String channelName, Long userId) {
        subscriptionManager.unsubscribeFromChannel(channelName, userId);
    }

    public void broadcastToChannel(String channelName, Object message) {
        if (!subscriptionManager.isValidChannel(channelName)) {
            log.warn("Attempted to broadcast to invalid channel: {}", channelName);
            return;
        }
        messageHandler.broadcastToChannel(channelName, message);
    }

    public void broadcastToUser(String channelName, Long userId, Object message) {
        messageHandler.broadcastToUser(channelName, userId, message);
    }

    public void notifyTransactionUpdate(Long userId, Object transactionData) {
        messageHandler.notifyTransactionUpdate(userId, transactionData);
    }

    public void notifyDashboardUpdate(Long userId, Object dashboardData) {
        messageHandler.notifyDashboardUpdate(userId, dashboardData);
    }

    public void notifyGoalUpdate(Long userId, Object goalData) {
        messageHandler.notifyGoalUpdate(userId, goalData);
    }

    public boolean isConnected() {
        return webSocketManager.isConnected();
    }

    public Map<String, Integer> getSubscriptionCounts() {
        return subscriptionManager.getSubscriptionCounts();
    }

    public void handleRealtimeMessage(String channelName, Object payload) {
        messageHandler.handleRealtimeMessage(channelName, payload);
    }

    public void subscribeToDatabaseChanges(String tableName, Long userId) {
        subscriptionManager.subscribeToDatabaseChanges(tableName, userId);
        if (webSocketManager.isConnected() && webSocketManager.getSession() != null) {
            messageHandler.sendDatabaseSubscriptionMessage(
                    webSocketManager.getSession(), tableName, webSocketManager.getNextMessageId());
        }
    }

    public void unsubscribeFromDatabaseChanges(String tableName, Long userId) {
        subscriptionManager.unsubscribeFromDatabaseChanges(tableName, userId);
        if (subscriptionManager.getDatabaseSubscribers(tableName).isEmpty()) {
            if (webSocketManager.isConnected() && webSocketManager.getSession() != null) {
                messageHandler.sendDatabaseUnsubscriptionMessage(
                        webSocketManager.getSession(), tableName, webSocketManager.getNextMessageId());
            }
        }
    }
}
