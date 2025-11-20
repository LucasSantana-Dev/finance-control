package com.finance_control.shared.service.realtime;

import com.finance_control.shared.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Manages WebSocket connection to Supabase Realtime.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RealtimeWebSocketManager {

    private final AppProperties appProperties;
    private WebSocketClient webSocketClient;
    private WebSocketSession webSocketSession;
    private volatile boolean connected = false;
    private final AtomicLong messageIdCounter = new AtomicLong(1);

    public void initialize() {
        log.info("Initializing WebSocket manager");
        webSocketClient = new StandardWebSocketClient();
    }

    public boolean connect(RealtimeWebSocketHandler handler) {
        if (connected) {
            log.debug("Already connected to Supabase Realtime");
            return true;
        }

        try {
            String supabaseUrl = appProperties.supabase().url();
            String anonKey = appProperties.supabase().anonKey();

            if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(anonKey)) {
                log.warn("Supabase URL or anon key not configured, realtime features disabled");
                return false;
            }

            String websocketUrl = supabaseUrl.replace("https://", "wss://") + "/realtime/v1/websocket";
            String fullUrl = websocketUrl + "?apikey=" + anonKey;

            log.info("Connecting to Supabase Realtime: {}", websocketUrl);

            WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
            webSocketSession = webSocketClient.execute(handler, headers, URI.create(fullUrl)).get();

            connected = true;
            log.info("Successfully connected to Supabase Realtime");
            return true;

        } catch (Exception e) {
            log.error("Failed to connect to Supabase Realtime", e);
            connected = false;
            return false;
        }
    }

    public void disconnect() {
        try {
            if (webSocketSession != null && webSocketSession.isOpen()) {
                webSocketSession.close(CloseStatus.NORMAL);
            }
            connected = false;
            webSocketSession = null;
            log.info("Disconnected from Supabase Realtime");
        } catch (Exception e) {
            log.error("Error during realtime disconnect", e);
        }
    }

    public boolean isConnected() {
        return connected && webSocketSession != null && webSocketSession.isOpen();
    }

    public WebSocketSession getSession() {
        return webSocketSession;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public long getNextMessageId() {
        return messageIdCounter.incrementAndGet();
    }
}

