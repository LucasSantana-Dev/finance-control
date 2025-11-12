package com.finance_control.shared.controller;

import com.finance_control.shared.service.SupabaseRealtimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for Supabase Realtime operations.
 * Provides endpoints for managing realtime subscriptions and broadcasting messages.
 */
@RestController
@RequestMapping("/api/realtime")
@Tag(name = "Realtime", description = "Endpoints for realtime messaging and subscriptions using Supabase Realtime")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.supabase.realtime.enabled", havingValue = "true", matchIfMissing = true)
public class RealtimeController {

    private final SupabaseRealtimeService realtimeService;

    @GetMapping("/status")
    @Operation(
            summary = "Get realtime service status",
            description = "Returns the current status of the Supabase Realtime service"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("Getting realtime service status");

        Map<String, Object> status = new HashMap<>();
        status.put("connected", realtimeService.isConnected());
        status.put("subscriptionCounts", realtimeService.getSubscriptionCounts());
        status.put("timestamp", System.currentTimeMillis());

        HttpStatus httpStatus = realtimeService.isConnected() ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return new ResponseEntity<>(status, httpStatus);
    }

    @PostMapping("/subscribe/{channelName}")
    @Operation(
            summary = "Subscribe to a realtime channel",
            description = "Subscribes the current user to the specified realtime channel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscribed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid channel name"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "503", description = "Realtime service unavailable")
    })
    public ResponseEntity<Map<String, Object>> subscribeToChannel(
            @Parameter(description = "Name of the realtime channel", required = true)
            @PathVariable String channelName,

            @Parameter(description = "User ID subscribing to the channel", required = true)
            @RequestParam Long userId
    ) {
        log.info("Subscribing user {} to channel {}", userId, channelName);

        if (!realtimeService.isConnected()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Realtime service is not available"));
        }

        try {
            realtimeService.subscribeToChannel(channelName, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("channel", channelName);
            response.put("userId", userId);
            response.put("message", "Subscribed to channel successfully");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid channel subscription request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error subscribing to channel {}: {}", channelName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to subscribe to channel"));
        }
    }

    @DeleteMapping("/unsubscribe/{channelName}")
    @Operation(
            summary = "Unsubscribe from a realtime channel",
            description = "Unsubscribes the current user from the specified realtime channel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unsubscribed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid channel name"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "503", description = "Realtime service unavailable")
    })
    public ResponseEntity<Map<String, Object>> unsubscribeFromChannel(
            @Parameter(description = "Name of the realtime channel", required = true)
            @PathVariable String channelName,

            @Parameter(description = "User ID unsubscribing from the channel", required = true)
            @RequestParam Long userId
    ) {
        log.info("Unsubscribing user {} from channel {}", userId, channelName);

        if (!realtimeService.isConnected()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Realtime service is not available"));
        }

        try {
            realtimeService.unsubscribeFromChannel(channelName, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("channel", channelName);
            response.put("userId", userId);
            response.put("message", "Unsubscribed from channel successfully");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error unsubscribing from channel {}: {}", channelName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to unsubscribe from channel"));
        }
    }

    @PostMapping("/broadcast/{channelName}")
    @Operation(
            summary = "Broadcast message to channel",
            description = "Broadcasts a message to all subscribers of the specified realtime channel"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message broadcasted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid channel name or message"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "503", description = "Realtime service unavailable")
    })
    public ResponseEntity<Map<String, Object>> broadcastToChannel(
            @Parameter(description = "Name of the realtime channel", required = true)
            @PathVariable String channelName,

            @Parameter(description = "Message to broadcast", required = true)
            @RequestBody Map<String, Object> message
    ) {
        log.info("Broadcasting message to channel {}", channelName);

        if (!realtimeService.isConnected()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Realtime service is not available"));
        }

        try {
            realtimeService.broadcastToChannel(channelName, message);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("channel", channelName);
            response.put("message", "Message broadcasted successfully");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error broadcasting message to channel {}: {}", channelName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to broadcast message"));
        }
    }

    @PostMapping("/notify/transaction")
    @Operation(
            summary = "Notify transaction update",
            description = "Sends a notification about transaction updates to subscribed users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "503", description = "Realtime service unavailable")
    })
    public ResponseEntity<Map<String, Object>> notifyTransactionUpdate(
            @Parameter(description = "User ID who owns the transaction", required = true)
            @RequestParam Long userId,

            @Parameter(description = "Transaction data", required = true)
            @RequestBody Map<String, Object> transactionData
    ) {
        log.info("Notifying transaction update for user {}", userId);

        if (!realtimeService.isConnected()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Realtime service is not available"));
        }

        try {
            realtimeService.notifyTransactionUpdate(userId, transactionData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("type", "transaction_update");
            response.put("userId", userId);
            response.put("message", "Transaction update notification sent");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending transaction update notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send notification"));
        }
    }

    @PostMapping("/notify/dashboard")
    @Operation(
            summary = "Notify dashboard update",
            description = "Sends a notification about dashboard updates to subscribed users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "503", description = "Realtime service unavailable")
    })
    public ResponseEntity<Map<String, Object>> notifyDashboardUpdate(
            @Parameter(description = "User ID whose dashboard was updated", required = true)
            @RequestParam Long userId,

            @Parameter(description = "Dashboard data", required = true)
            @RequestBody Map<String, Object> dashboardData
    ) {
        log.info("Notifying dashboard update for user {}", userId);

        if (!realtimeService.isConnected()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Realtime service is not available"));
        }

        try {
            realtimeService.notifyDashboardUpdate(userId, dashboardData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("type", "dashboard_update");
            response.put("userId", userId);
            response.put("message", "Dashboard update notification sent");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending dashboard update notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send notification"));
        }
    }

    @PostMapping("/notify/goal")
    @Operation(
            summary = "Notify goal update",
            description = "Sends a notification about goal updates to subscribed users"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "503", description = "Realtime service unavailable")
    })
    public ResponseEntity<Map<String, Object>> notifyGoalUpdate(
            @Parameter(description = "User ID who owns the goal", required = true)
            @RequestParam Long userId,

            @Parameter(description = "Goal data", required = true)
            @RequestBody Map<String, Object> goalData
    ) {
        log.info("Notifying goal update for user {}", userId);

        if (!realtimeService.isConnected()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Realtime service is not available"));
        }

        try {
            realtimeService.notifyGoalUpdate(userId, goalData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("type", "goal_update");
            response.put("userId", userId);
            response.put("message", "Goal update notification sent");
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error sending goal update notification: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send notification"));
        }
    }
}
