package com.finance_control.shared.service.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handles realtime message processing and broadcasting.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeMessageHandler {

    private final ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    private final RealtimeSubscriptionManager subscriptionManager;

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void handleRealtimeMessage(String channelName, Object payload) {
        log.debug("Received realtime message on channel {}: {}", channelName, payload);

        try {
            if (payload instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadMap = (Map<String, Object>) payload;

                String eventType = (String) payloadMap.get("eventType");
                if ("INSERT".equals(eventType) || "UPDATE".equals(eventType) || "DELETE".equals(eventType)) {
                    handleDatabaseChangeEvent(channelName, payloadMap);
                }
            }

            broadcastToChannel(channelName, payload);

        } catch (Exception e) {
            log.error("Error handling realtime message", e);
        }
    }

    public void broadcastToChannel(String channelName, Object message) {
        Set<Long> subscribers = subscriptionManager.getChannelSubscribers(channelName);
        if (subscribers != null && !subscribers.isEmpty()) {
            log.debug("Broadcasting message to {} subscribers in channel {}", subscribers.size(), channelName);
            for (Long userId : subscribers) {
                broadcastToUser(channelName, userId, message);
            }
        } else {
            log.debug("No subscribers for channel {}", channelName);
        }
    }

    public void broadcastToUser(String channelName, Long userId, Object message) {
        try {
            log.debug("Message to user {} on channel {} would be sent: {} - TEMPORARILY DISABLED", userId, channelName, message);
        } catch (Exception e) {
            log.error("Failed to send message to user {} on channel {}: {}", userId, channelName, e.getMessage(), e);
        }
    }

    public void notifyTransactionUpdate(Long userId, Object transactionData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "transaction_update");
        message.put("userId", userId);
        message.put("data", transactionData);
        message.put("timestamp", System.currentTimeMillis());
        broadcastToChannel("transactions", message);
    }

    public void notifyDashboardUpdate(Long userId, Object dashboardData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "dashboard_update");
        message.put("userId", userId);
        message.put("data", dashboardData);
        message.put("timestamp", System.currentTimeMillis());
        broadcastToChannel("dashboard", message);
    }

    public void notifyGoalUpdate(Long userId, Object goalData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "goal_update");
        message.put("userId", userId);
        message.put("data", goalData);
        message.put("timestamp", System.currentTimeMillis());
        broadcastToChannel("goals", message);
    }

    public void sendDatabaseSubscriptionMessage(WebSocketSession session, String tableName, long messageId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("event", "phx_join");
            message.put("topic", "realtime:" + tableName);
            message.put("payload", new HashMap<>());
            message.put("ref", String.valueOf(messageId));

            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));

            log.debug("Sent database subscription message for table: {}", tableName);

        } catch (Exception e) {
            log.error("Failed to send database subscription message for table {}", tableName, e);
        }
    }

    public void sendDatabaseUnsubscriptionMessage(WebSocketSession session, String tableName, long messageId) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("event", "phx_leave");
            message.put("topic", "realtime:" + tableName);
            message.put("payload", new HashMap<>());
            message.put("ref", String.valueOf(messageId));

            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));

            log.debug("Sent database unsubscription message for table: {}", tableName);

        } catch (Exception e) {
            log.error("Failed to send database unsubscription message for table {}", tableName, e);
        }
    }

    private void handleDatabaseChangeEvent(String tableName, Map<String, Object> payload) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> record = (Map<String, Object>) payload.get("new");
            if (record == null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> oldRecord = (Map<String, Object>) payload.get("old");
                record = oldRecord;
            }

            if (record != null) {
                Object userIdObj = record.get("user_id");
                if (userIdObj instanceof Number) {
                    Long recordUserId = ((Number) userIdObj).longValue();
                    notifyDatabaseChange(tableName, recordUserId, payload);
                } else {
                    broadcastDatabaseChange(tableName, payload);
                }
            }

        } catch (Exception e) {
            log.error("Error handling database change event for table {}", tableName, e);
        }
    }

    private void notifyDatabaseChange(String tableName, Long userId, Map<String, Object> payload) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "database_change");
        message.put("table", tableName);
        message.put("userId", userId);
        message.put("data", payload);
        message.put("timestamp", System.currentTimeMillis());
        broadcastToUser(tableName, userId, message);
    }

    private void broadcastDatabaseChange(String tableName, Map<String, Object> payload) {
        Set<Long> subscribers = subscriptionManager.getDatabaseSubscribers(tableName);
        if (subscribers != null && !subscribers.isEmpty()) {
            Map<String, Object> message = new HashMap<>();
            message.put("type", "database_change");
            message.put("table", tableName);
            message.put("data", payload);
            message.put("timestamp", System.currentTimeMillis());

            for (Long userId : subscribers) {
                broadcastToUser(tableName, userId, message);
            }
        }
    }
}
