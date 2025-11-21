package com.finance_control.notifications.service;

import com.finance_control.notifications.dto.CreateNotificationDTO;
import com.finance_control.notifications.dto.NotificationDTO;
import com.finance_control.notifications.enums.NotificationType;
import com.finance_control.notifications.model.Notification;
import com.finance_control.notifications.repository.NotificationRepository;
import com.finance_control.shared.context.UserContext;
import com.finance_control.shared.exception.EntityNotFoundException;
import com.finance_control.shared.service.BaseService;
import com.finance_control.users.model.User;
import com.finance_control.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional
public class NotificationService extends BaseService<Notification, Long, NotificationDTO> {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        super(notificationRepository);
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean isUserAware() {
        return true;
    }

    @Override
    protected Notification mapToEntity(NotificationDTO dto) {
        Notification notification = new Notification();
        if (dto.getId() != null) {
            notification.setId(dto.getId());
        }
        notification.setType(dto.getType());
        notification.setTitle(dto.getTitle());
        notification.setMessage(dto.getMessage());
        notification.setIsRead(dto.getIsRead() != null ? dto.getIsRead() : false);
        notification.setMetadata(dto.getMetadata());
        notification.setReadAt(dto.getReadAt());

        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("User", "id", dto.getUserId()));
            notification.setUser(user);
        }

        return notification;
    }

    @Override
    protected void updateEntityFromDTO(Notification entity, NotificationDTO dto) {
        if (dto.getType() != null) {
            entity.setType(dto.getType());
        }
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getMessage() != null) {
            entity.setMessage(dto.getMessage());
        }
        if (dto.getMetadata() != null) {
            entity.setMetadata(dto.getMetadata());
        }
    }

    @Override
    protected NotificationDTO mapToResponseDTO(Notification entity) {
        NotificationDTO dto = NotificationDTO.builder()
                .userId(entity.getUser() != null ? entity.getUser().getId() : null)
                .type(entity.getType())
                .title(entity.getTitle())
                .message(entity.getMessage())
                .isRead(entity.getIsRead())
                .metadata(entity.getMetadata())
                .readAt(entity.getReadAt())
                .build();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    @Override
    protected boolean belongsToUser(Notification entity, Long userId) {
        return entity.getUser() != null && entity.getUser().getId().equals(userId);
    }

    @Override
    protected void setUserId(Notification entity, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User", "id", userId));
        entity.setUser(user);
    }

    @Override
    protected String getEntityName() {
        return "Notification";
    }

    public NotificationDTO createFromDTO(CreateNotificationDTO createDTO) {
        log.debug("Creating notification from CreateNotificationDTO");
        NotificationDTO dto = NotificationDTO.builder()
                .type(createDTO.getType())
                .title(createDTO.getTitle())
                .message(createDTO.getMessage())
                .metadata(createDTO.getMetadata())
                .isRead(false)
                .build();
        return create(dto);
    }

    public Page<NotificationDTO> findByUser(Pageable pageable) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Finding notifications for user {}", userId);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToResponseDTO);
    }

    public Page<NotificationDTO> findByUserAndType(NotificationType type, Pageable pageable) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Finding notifications for user {} and type {}", userId, type);
        Page<Notification> notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        return notifications.map(this::mapToResponseDTO);
    }

    public Page<NotificationDTO> findByUserAndReadStatus(Boolean isRead, Pageable pageable) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Finding notifications for user {} with read status {}", userId, isRead);
        Page<Notification> notifications = notificationRepository.findByUserIdAndIsReadOrderByCreatedAtDesc(userId, isRead, pageable);
        return notifications.map(this::mapToResponseDTO);
    }

    public long countUnread() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        return notificationRepository.countUnreadByUserId(userId);
    }

    public List<NotificationDTO> findUnread() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Finding unread notifications for user {}", userId);
        List<Notification> notifications = notificationRepository.findUnreadByUserId(userId);
        return notifications.stream().map(this::mapToResponseDTO).toList();
    }

    public NotificationDTO markAsRead(Long id) {
        log.debug("Marking notification {} as read", id);
        Notification notification = getEntityById(id);
        validateUserOwnership(notification, id);
        notification.markAsRead();
        Notification saved = notificationRepository.save(notification);
        return mapToResponseDTO(saved);
    }

    public NotificationDTO markAsUnread(Long id) {
        log.debug("Marking notification {} as unread", id);
        Notification notification = getEntityById(id);
        validateUserOwnership(notification, id);
        notification.markAsUnread();
        Notification saved = notificationRepository.save(notification);
        return mapToResponseDTO(saved);
    }

    public void markAllAsRead() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Marking all notifications as read for user {}", userId);
        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteReadNotifications() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new SecurityException("User context not available");
        }
        log.debug("Deleting read notifications for user {}", userId);
        notificationRepository.deleteByUserIdAndIsReadTrue(userId);
    }

    private void validateUserOwnership(Notification entity, Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new SecurityException("User context not available");
        }
        if (!belongsToUser(entity, currentUserId)) {
            throw new SecurityException("Access denied: notification does not belong to current user");
        }
    }
}
