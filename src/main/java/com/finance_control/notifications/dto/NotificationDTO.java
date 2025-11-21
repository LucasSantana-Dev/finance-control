package com.finance_control.notifications.dto;

import com.finance_control.notifications.enums.NotificationType;
import com.finance_control.shared.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationDTO extends BaseDTO<Long> {

    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Boolean isRead;
    private String metadata;
    private LocalDateTime readAt;
}
