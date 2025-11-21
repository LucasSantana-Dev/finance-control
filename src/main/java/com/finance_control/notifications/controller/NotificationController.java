package com.finance_control.notifications.controller;

import com.finance_control.notifications.dto.CreateNotificationDTO;
import com.finance_control.notifications.dto.NotificationDTO;
import com.finance_control.notifications.enums.NotificationType;
import com.finance_control.notifications.model.Notification;
import com.finance_control.notifications.service.NotificationService;
import com.finance_control.shared.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notifications")
@Tag(name = "Notifications", description = "User notification management endpoints")
public class NotificationController extends BaseController<Notification, Long, NotificationDTO> {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        super(notificationService);
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create notification", description = "Create a new notification for the current user")
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody CreateNotificationDTO createDTO) {
        log.debug("POST request to create notification");
        NotificationDTO created = notificationService.createFromDTO(createDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieve all unread notifications for the current user")
    public ResponseEntity<Page<NotificationDTO>> getUnreadNotifications(Pageable pageable) {
        log.debug("GET request to retrieve unread notifications");
        Page<NotificationDTO> notifications = notificationService.findByUserAndReadStatus(false, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/read")
    @Operation(summary = "Get read notifications", description = "Retrieve all read notifications for the current user")
    public ResponseEntity<Page<NotificationDTO>> getReadNotifications(Pageable pageable) {
        log.debug("GET request to retrieve read notifications");
        Page<NotificationDTO> notifications = notificationService.findByUserAndReadStatus(true, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get notifications by type", description = "Retrieve notifications filtered by type for the current user")
    public ResponseEntity<Page<NotificationDTO>> getNotificationsByType(
            @Parameter(description = "Notification type") @PathVariable NotificationType type,
            Pageable pageable) {
        log.debug("GET request to retrieve notifications by type: {}", type);
        Page<NotificationDTO> notifications = notificationService.findByUserAndType(type, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/count/unread")
    @Operation(summary = "Count unread notifications", description = "Get the count of unread notifications for the current user")
    public ResponseEntity<Long> countUnreadNotifications() {
        log.debug("GET request to count unread notifications");
        long count = notificationService.countUnread();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<NotificationDTO> markAsRead(
            @Parameter(description = "Notification ID") @PathVariable Long id) {
        log.debug("PUT request to mark notification {} as read", id);
        NotificationDTO updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/unread")
    @Operation(summary = "Mark notification as unread", description = "Mark a specific notification as unread")
    public ResponseEntity<NotificationDTO> markAsUnread(
            @Parameter(description = "Notification ID") @PathVariable Long id) {
        log.debug("PUT request to mark notification {} as unread", id);
        NotificationDTO updated = notificationService.markAsUnread(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Mark all unread notifications as read for the current user")
    public ResponseEntity<Void> markAllAsRead() {
        log.debug("PUT request to mark all notifications as read");
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/read")
    @Operation(summary = "Delete read notifications", description = "Delete all read notifications for the current user")
    public ResponseEntity<Void> deleteReadNotifications() {
        log.debug("DELETE request to delete read notifications");
        notificationService.deleteReadNotifications();
        return ResponseEntity.noContent().build();
    }
}
