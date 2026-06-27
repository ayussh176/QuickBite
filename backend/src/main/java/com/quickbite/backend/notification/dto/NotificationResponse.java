package com.quickbite.backend.notification.dto;

import com.quickbite.backend.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private NotificationType notificationType;
    private boolean read;
    private String data;
    private LocalDateTime createdAt;
}
