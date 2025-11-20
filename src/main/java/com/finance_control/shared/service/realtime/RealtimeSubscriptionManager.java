package com.finance_control.shared.service.realtime;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages realtime channel and database subscriptions.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeSubscriptionManager {

    private final AppProperties appProperties;
    private final Map<String, Set<Long>> activeSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, Set<Long>> databaseSubscriptions = new ConcurrentHashMap<>();

    public void subscribeToChannel(String channelName, Long userId) {
        if (!isValidChannel(channelName)) {
            throw new IllegalArgumentException("Invalid channel: " + channelName);
        }

        activeSubscriptions.computeIfAbsent(channelName, k -> new HashSet<>()).add(userId);
        log.info("User {} subscribed to channel {}", userId, channelName);
    }

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

    public Set<Long> getChannelSubscribers(String channelName) {
        return activeSubscriptions.getOrDefault(channelName, Set.of());
    }

    public void subscribeToDatabaseChanges(String tableName, Long userId) {
        databaseSubscriptions.computeIfAbsent(tableName, k -> new HashSet<>()).add(userId);
        log.info("User {} subscribed to database changes for table {}", userId, tableName);
    }

    public void unsubscribeFromDatabaseChanges(String tableName, Long userId) {
        Set<Long> subscribers = databaseSubscriptions.get(tableName);
        if (subscribers != null) {
            subscribers.remove(userId);
            if (subscribers.isEmpty()) {
                databaseSubscriptions.remove(tableName);
            }
            log.info("User {} unsubscribed from database changes for table {}", userId, tableName);
        }
    }

    public Set<Long> getDatabaseSubscribers(String tableName) {
        return databaseSubscriptions.getOrDefault(tableName, Set.of());
    }

    public void setupDefaultChannels() {
        String[] channels = appProperties.supabase().realtime().channels().toArray(new String[0]);
        log.info("Setting up realtime channels: {}", String.join(", ", channels));

        for (String channel : channels) {
            activeSubscriptions.putIfAbsent(channel, new HashSet<>());
        }

        String[] defaultTables = {"transactions", "goals", "profiles"};
        for (String table : defaultTables) {
            databaseSubscriptions.putIfAbsent(table, new HashSet<>());
        }
    }

    public boolean isValidChannel(String channelName) {
        String[] allowedChannels = appProperties.supabase().realtime().channels().toArray(new String[0]);
        for (String allowedChannel : allowedChannels) {
            if (allowedChannel.equals(channelName)) {
                return true;
            }
        }
        return false;
    }

    public Map<String, Integer> getSubscriptionCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<String, Set<Long>> entry : activeSubscriptions.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    public void clearAll() {
        activeSubscriptions.clear();
        databaseSubscriptions.clear();
    }
}

