package com.quickbite.backend.notification.dto;

import com.quickbite.backend.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String title;
    private String message;
    private NotificationType notificationType;
    private String data;
}
