package com.finance_control.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for managing Supabase Realtime subscriptions.
 * Handles WebSocket connections and broadcasts realtime updates to clients.
 */
@Slf4j
@Service
@ConditionalOnProperty(value = "app.supabase.realtime.enabled", havingValue = "true", matchIfMissing = false)
public class SupabaseRealtimeService {

    @Autowired
    private AppProperties appProperties;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    // WebSocket client and session
    private WebSocketClient webSocketClient;
    private WebSocketSession webSocketSession;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Active subscriptions: channel -> Set of user IDs
    private final Map<String, Set<Long>> activeSubscriptions = new ConcurrentHashMap<>();

    // Database change subscriptions: table -> Set of user IDs
    private final Map<String, Set<Long>> databaseSubscriptions = new ConcurrentHashMap<>();

    // Realtime connection status
    private volatile boolean connected = false;

    // Message ID counter for WebSocket messages
    private final AtomicLong messageIdCounter = new AtomicLong(1);

    @PostConstruct
    public void initialize() {
        log.info("Initializing Supabase Realtime service");
        try {
            webSocketClient = new StandardWebSocketClient();
            connect();
            setupDefaultChannels();
        } catch (Exception e) {
            log.error("Failed to initialize Supabase Realtime service", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Supabase Realtime service");
        disconnect();
    }

    /**
     * Connects to Supabase Realtime via WebSocket.
     */
    public void connect() {
        if (connected) {
            log.debug("Already connected to Supabase Realtime");
            return;
        }

        try {
            String supabaseUrl = appProperties.supabase().url();
            String anonKey = appProperties.supabase().anonKey();

            if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(anonKey)) {
                log.warn("Supabase URL or anon key not configured, realtime features disabled");
                return;
            }

            // Convert HTTPS URL to WSS for WebSocket
            String websocketUrl = supabaseUrl.replace("https://", "wss://") + "/realtime/v1/websocket";

            // Add query parameters for authentication
            String fullUrl = websocketUrl + "?apikey=" + anonKey;

            log.info("Connecting to Supabase Realtime: {}", websocketUrl);

            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            SupabaseRealtimeWebSocketHandler handler = new SupabaseRealtimeWebSocketHandler();

            webSocketSession = webSocketClient.execute(handler, headers, URI.create(fullUrl)).get();

            connected = true;
            log.info("Successfully connected to Supabase Realtime");

        } catch (Exception e) {
            log.error("Failed to connect to Supabase Realtime", e);
            connected = false;
        }
    }

    /**
     * Disconnects from Supabase Realtime.
     */
    public void disconnect() {
        try {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.close(CloseStatus.NORMAL);
            }
            activeSubscriptions.clear();
            databaseSubscriptions.clear();
            connected = false;
            webSocketSession = null;
            log.info("Disconnected from Supabase Realtime");
        } catch (Exception e) {
            log.error("Error during realtime disconnect", e);
        }
    }

    /**
     * Subscribes a user to a realtime channel.
     *
     * @param channelName the name of the channel
     * @param userId the ID of the user subscribing
     */
    public void subscribeToChannel(String channelName, Long userId) {
        if (!isValidChannel(channelName)) {
            throw new IllegalArgumentException("Invalid channel: " + channelName);
        }

        activeSubscriptions.computeIfAbsent(channelName, k -> new HashSet<>()).add(userId);

        log.info("User {} subscribed to channel {}", userId, channelName);

        // Broadcast subscription confirmation
        Map<String, Object> subscriptionMessage = new HashMap<>();
        subscriptionMessage.put("type", "subscription_confirmed");
        subscriptionMessage.put("channel", channelName);
        subscriptionMessage.put("userId", userId);
        subscriptionMessage.put("timestamp", System.currentTimeMillis());

        broadcastToUser(channelName, userId, subscriptionMessage);
    }

    /**
     * Unsubscribes a user from a realtime channel.
     *
     * @param channelName the name of the channel
     * @param userId the ID of the user unsubscribing
     */
    public void unsubscribeFromChannel(String channelName, Long userId) {
        Set<Long> subscribers = activeSubscriptions.get(channelName);
        if (subscribers != null) {
            subscribers.remove(userId);
            if (subscribers.isEmpty()) {
                activeSubscriptions.remove(channelName);
            }
            log.info("User {} unsubscribed from channel {}", userId, channelName);
        }
    }

    /**
     * Broadcasts a message to all subscribers of a channel.
     *
     * @param channelName the name of the channel
     * @param message the message to broadcast
     */
    public void broadcastToChannel(String channelName, Object message) {
        if (!isValidChannel(channelName)) {
            log.warn("Attempted to broadcast to invalid channel: {}", channelName);
            return;
        }

        Set<Long> subscribers = activeSubscriptions.get(channelName);
        if (subscribers != null && !subscribers.isEmpty()) {
            log.debug("Broadcasting message to {} subscribers in channel {}", subscribers.size(), channelName);

            for (Long userId : subscribers) {
                broadcastToUser(channelName, userId, message);
            }
        } else {
            log.debug("No subscribers for channel {}", channelName);
        }
    }

    /**
     * Broadcasts a message to a specific user on a channel.
     *
     * @param channelName the name of the channel
     * @param userId the ID of the target user
     * @param message the message to send
     */
    public void broadcastToUser(String channelName, Long userId, Object message) {
        try {
            // String destination = "/topic/" + channelName + "/user/" + userId;
            // Temporarily disabled until messaging template is available
            // messagingTemplate.convertAndSend(destination, message);
            log.debug("Message to user {} on channel {} would be sent: {} - TEMPORARILY DISABLED", userId, channelName, message);
        } catch (Exception e) {
            log.error("Failed to send message to user {} on channel {}: {}", userId, channelName, e.getMessage(), e);
        }
    }

    /**
     * Notifies subscribers about transaction updates.
     *
     * @param userId the ID of the user who owns the transaction
     * @param transactionData the transaction data
     */
    public void notifyTransactionUpdate(Long userId, Object transactionData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "transaction_update");
        message.put("userId", userId);
        message.put("data", transactionData);
        message.put("timestamp", System.currentTimeMillis());

        broadcastToChannel("transactions", message);
    }

    /**
     * Notifies subscribers about dashboard updates.
     *
     * @param userId the ID of the user whose dashboard was updated
     * @param dashboardData the updated dashboard data
     */
    public void notifyDashboardUpdate(Long userId, Object dashboardData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "dashboard_update");
        message.put("userId", userId);
        message.put("data", dashboardData);
        message.put("timestamp", System.currentTimeMillis());

        broadcastToChannel("dashboard", message);
    }

    /**
     * Notifies subscribers about goal updates.
     *
     * @param userId the ID of the user who owns the goal
     * @param goalData the goal data
     */
    public void notifyGoalUpdate(Long userId, Object goalData) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "goal_update");
        message.put("userId", userId);
        message.put("data", goalData);
        message.put("timestamp", System.currentTimeMillis());

        broadcastToChannel("goals", message);
    }

    /**
     * Gets the current connection status.
     *
     * @return true if connected to Supabase Realtime
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Gets the active subscriptions count.
     *
     * @return map of channel names to subscriber counts
     */
    public Map<String, Integer> getSubscriptionCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<String, Set<Long>> entry : activeSubscriptions.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    /**
     * Sets up default realtime channels and database subscriptions based on configuration.
     */
    private void setupDefaultChannels() {
        String[] channels = appProperties.supabase().realtime().channels().toArray(new String[0]);
        log.info("Setting up realtime channels: {}", String.join(", ", channels));

        // Setup default channels
        for (String channel : channels) {
            activeSubscriptions.putIfAbsent(channel, new HashSet<>());
        }

        // Setup database subscriptions for common tables
        String[] defaultTables = {"transactions", "goals", "profiles"};
        for (String table : defaultTables) {
            databaseSubscriptions.putIfAbsent(table, new HashSet<>());
            if (connected && webSocketSession != null && webSocketSession.isOpen()) {
                sendDatabaseSubscriptionMessage(table);
            }
        }
    }

    /**
     * Validates if a channel name is allowed.
     *
     * @param channelName the channel name to validate
     * @return true if the channel is valid
     */
    private boolean isValidChannel(String channelName) {
        String[] allowedChannels = appProperties.supabase().realtime().channels().toArray(new String[0]);
        for (String allowedChannel : allowedChannels) {
            if (allowedChannel.equals(channelName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles incoming realtime messages from Supabase.
     * This method is called by the WebSocket handler when messages are received.
     *
     * @param channelName the channel that received the message
     * @param payload the message payload
     */
    public void handleRealtimeMessage(String channelName, Object payload) {
        log.debug("Received realtime message on channel {}: {}", channelName, payload);

        try {
            // Process database change events
            if (payload instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadMap = (Map<String, Object>) payload;

                String eventType = (String) payloadMap.get("eventType");
                if ("INSERT".equals(eventType) || "UPDATE".equals(eventType) || "DELETE".equals(eventType)) {
                    handleDatabaseChangeEvent(channelName, payloadMap);
                }
            }

            // Broadcast to subscribers
            broadcastToChannel(channelName, payload);

        } catch (Exception e) {
            log.error("Error handling realtime message", e);
        }
    }

    /**
     * Subscribes to database changes for a specific table.
     *
     * @param tableName the name of the table to monitor
     * @param userId the user ID subscribing to changes
     */
    public void subscribeToDatabaseChanges(String tableName, Long userId) {
        databaseSubscriptions.computeIfAbsent(tableName, k -> new HashSet<>()).add(userId);

        if (connected && webSocketSession != null && webSocketSession.isOpen()) {
            sendDatabaseSubscriptionMessage(tableName);
        }

        log.info("User {} subscribed to database changes for table {}", userId, tableName);
    }

    /**
     * Unsubscribes a user from database changes for a specific table.
     *
     * @param tableName the name of the table
     * @param userId the user ID unsubscribing
     */
    public void unsubscribeFromDatabaseChanges(String tableName, Long userId) {
        Set<Long> subscribers = databaseSubscriptions.get(tableName);
        if (subscribers != null) {
            subscribers.remove(userId);
            if (subscribers.isEmpty()) {
                databaseSubscriptions.remove(tableName);
                if (connected && webSocketSession != null && webSocketSession.isOpen()) {
                    sendDatabaseUnsubscriptionMessage(tableName);
                }
            }
            log.info("User {} unsubscribed from database changes for table {}", userId, tableName);
        }
    }

    /**
     * Handles database change events.
     */
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
                // Extract user ID from the record to determine who should receive the notification
                Object userIdObj = record.get("user_id");
                if (userIdObj instanceof Number) {
                    Long recordUserId = ((Number) userIdObj).longValue();
                    notifyDatabaseChange(tableName, recordUserId, payload);
                } else {
                    // If no user_id field, broadcast to all subscribers of the table
                    broadcastDatabaseChange(tableName, payload);
                }
            }

        } catch (Exception e) {
            log.error("Error handling database change event for table {}", tableName, e);
        }
    }

    /**
     * Notifies a specific user about database changes.
     */
    private void notifyDatabaseChange(String tableName, Long userId, Map<String, Object> payload) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "database_change");
        message.put("table", tableName);
        message.put("userId", userId);
        message.put("data", payload);
        message.put("timestamp", System.currentTimeMillis());

        broadcastToUser(tableName, userId, message);
    }

    /**
     * Broadcasts database changes to all subscribers of a table.
     */
    private void broadcastDatabaseChange(String tableName, Map<String, Object> payload) {
        Set<Long> subscribers = databaseSubscriptions.get(tableName);
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

    /**
     * Sends a subscription message for database changes.
     */
    private void sendDatabaseSubscriptionMessage(String tableName) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("event", "phx_join");
            message.put("topic", "realtime:" + tableName);
            message.put("payload", new HashMap<>());
            message.put("ref", String.valueOf(messageIdCounter.incrementAndGet()));

            String jsonMessage = objectMapper.writeValueAsString(message);
            webSocketSession.sendMessage(new TextMessage(jsonMessage));

            log.debug("Sent database subscription message for table: {}", tableName);

        } catch (Exception e) {
            log.error("Failed to send database subscription message for table {}", tableName, e);
        }
    }

    /**
     * Sends an unsubscription message for database changes.
     */
    private void sendDatabaseUnsubscriptionMessage(String tableName) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("event", "phx_leave");
            message.put("topic", "realtime:" + tableName);
            message.put("payload", new HashMap<>());
            message.put("ref", String.valueOf(messageIdCounter.incrementAndGet()));

            String jsonMessage = objectMapper.writeValueAsString(message);
            webSocketSession.sendMessage(new TextMessage(jsonMessage));

            log.debug("Sent database unsubscription message for table: {}", tableName);

        } catch (Exception e) {
            log.error("Failed to send database unsubscription message for table {}", tableName, e);
        }
    }

    /**
     * WebSocket handler for Supabase Realtime connections.
     */
    private class SupabaseRealtimeWebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            log.info("WebSocket connection established with Supabase Realtime");
            connected = true;

            // Send heartbeat to keep connection alive
            startHeartbeat();
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            log.info("WebSocket connection closed: {}", status);
            connected = false;
            webSocketSession = null;

            // Attempt to reconnect after a delay
            reconnectAfterDelay();
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            try {
                String payload = message.getPayload();
                JsonNode jsonNode = objectMapper.readTree(payload);

                String event = jsonNode.get("event").asText();
                String topic = jsonNode.get("topic").asText();

                if ("phx_reply".equals(event)) {
                    // Handle replies to our messages
                    log.debug("Received reply for topic: {}", topic);
                } else if ("broadcast".equals(event)) {
                    // Handle broadcast messages
                    JsonNode messagePayload = jsonNode.get("payload");
                    if (messagePayload != null) {
                        handleRealtimeMessage(topic, objectMapper.treeToValue(messagePayload, Map.class));
                    }
                } else {
                    log.debug("Received {} event for topic: {}", event, topic);
                }

            } catch (Exception e) {
                log.error("Error handling WebSocket message", e);
            }
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            log.error("WebSocket transport error", exception);
        }

        private void startHeartbeat() {
            // Send periodic heartbeat messages to keep the connection alive
            Thread heartbeatThread = new Thread(() -> {
                while (connected && webSocketSession != null && webSocketSession.isOpen()) {
                    try {
                        Map<String, Object> heartbeat = new HashMap<>();
                        heartbeat.put("event", "heartbeat");
                        heartbeat.put("topic", "phoenix");
                        heartbeat.put("payload", new HashMap<>());
                        heartbeat.put("ref", String.valueOf(messageIdCounter.incrementAndGet()));

                        String heartbeatMessage = objectMapper.writeValueAsString(heartbeat);
                        webSocketSession.sendMessage(new TextMessage(heartbeatMessage));

                        Thread.sleep(30000); // Send heartbeat every 30 seconds

                    } catch (Exception e) {
                        log.error("Error sending heartbeat", e);
                        break;
                    }
                }
            });
            heartbeatThread.setDaemon(true);
            heartbeatThread.start();
        }

        private void reconnectAfterDelay() {
            Thread reconnectThread = new Thread(() -> {
                try {
                    Thread.sleep(5000); // Wait 5 seconds before reconnecting
                    if (!connected) {
                        log.info("Attempting to reconnect to Supabase Realtime");
                        connect();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            reconnectThread.setDaemon(true);
            reconnectThread.start();
        }
    }
}
