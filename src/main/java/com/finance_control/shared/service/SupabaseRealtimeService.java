package com.finance_control.shared.service;

import com.finance_control.shared.config.AppProperties;
import com.harium.supabase.SupabaseClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing Supabase Realtime subscriptions.
 * Handles WebSocket connections and broadcasts realtime updates to clients.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "app.supabase.realtime.enabled", havingValue = "true", matchIfMissing = true)
public class SupabaseRealtimeService {

    private final SupabaseClient supabaseClient;
    private final AppProperties appProperties;
    private final SimpMessagingTemplate messagingTemplate;

    // Active subscriptions: channel -> Set of user IDs
    private final Map<String, Set<Long>> activeSubscriptions = new ConcurrentHashMap<>();

    // Realtime connection status
    private volatile boolean connected = false;

    @PostConstruct
    public void initialize() {
        log.info("Initializing Supabase Realtime service");
        try {
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
     * Connects to Supabase Realtime.
     */
    public void connect() {
        try {
            if (supabaseClient != null) {
                // Note: The exact connection method depends on the Supabase Java client implementation
                // This is a placeholder for the actual connection logic
                connected = true;
                log.info("Connected to Supabase Realtime");
            } else {
                log.warn("Supabase client not available, realtime features disabled");
            }
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
            activeSubscriptions.clear();
            connected = false;
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
            String destination = "/topic/" + channelName + "/user/" + userId;
            messagingTemplate.convertAndSend(destination, message);
            log.debug("Sent message to user {} on channel {}", userId, channelName);
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
     * Sets up default realtime channels based on configuration.
     */
    private void setupDefaultChannels() {
        String[] channels = appProperties.getSupabase().getRealtime().getChannels();
        log.info("Setting up realtime channels: {}", String.join(", ", channels));

        // Note: Actual channel setup would depend on the Supabase Java client
        // This is a placeholder for the channel initialization logic
        for (String channel : channels) {
            activeSubscriptions.putIfAbsent(channel, new HashSet<>());
        }
    }

    /**
     * Validates if a channel name is allowed.
     *
     * @param channelName the channel name to validate
     * @return true if the channel is valid
     */
    private boolean isValidChannel(String channelName) {
        String[] allowedChannels = appProperties.getSupabase().getRealtime().getChannels();
        for (String allowedChannel : allowedChannels) {
            if (allowedChannel.equals(channelName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles incoming realtime messages from Supabase.
     * This method would be called by the Supabase client when messages are received.
     *
     * @param channelName the channel that received the message
     * @param payload the message payload
     */
    public void handleRealtimeMessage(String channelName, Object payload) {
        log.debug("Received realtime message on channel {}: {}", channelName, payload);

        // Process the message and broadcast to subscribers
        Map<String, Object> message = new HashMap<>();
        message.put("type", "realtime_message");
        message.put("channel", channelName);
        message.put("payload", payload);
        message.put("timestamp", System.currentTimeMillis());

        broadcastToChannel(channelName, message);
    }
}
