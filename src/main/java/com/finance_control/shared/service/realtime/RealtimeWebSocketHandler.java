package com.finance_control.shared.service.realtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket handler for Supabase Realtime connections.
 */
@Slf4j
@RequiredArgsConstructor
public class RealtimeWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final RealtimeMessageHandler messageHandler;
    private final RealtimeWebSocketManager webSocketManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established with Supabase Realtime");
        webSocketManager.setConnected(true);
        startHeartbeat();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", status);
        webSocketManager.setConnected(false);

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
                log.debug("Received reply for topic: {}", topic);
            } else if ("broadcast".equals(event)) {
                JsonNode messagePayload = jsonNode.get("payload");
                if (messagePayload != null) {
                    messageHandler.handleRealtimeMessage(topic, objectMapper.treeToValue(messagePayload, Map.class));
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
        Thread heartbeatThread = new Thread(() -> {
            while (webSocketManager.isConnected() && webSocketManager.getSession() != null
                    && webSocketManager.getSession().isOpen()) {
                try {
                    Map<String, Object> heartbeat = new HashMap<>();
                    heartbeat.put("event", "heartbeat");
                    heartbeat.put("topic", "phoenix");
                    heartbeat.put("payload", new HashMap<>());
                    heartbeat.put("ref", String.valueOf(webSocketManager.getNextMessageId()));

                    String heartbeatMessage = objectMapper.writeValueAsString(heartbeat);
                    webSocketManager.getSession().sendMessage(new TextMessage(heartbeatMessage));

                    Thread.sleep(30000);

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
                Thread.sleep(5000);
                if (!webSocketManager.isConnected()) {
                    log.info("Attempting to reconnect to Supabase Realtime");
                    webSocketManager.connect(this);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        reconnectThread.setDaemon(true);
        reconnectThread.start();
    }
}
