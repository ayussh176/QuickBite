package com.quickbite.backend.notification.entity;

import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.common.BaseEntity;
import com.quickbite.backend.common.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_user_id", columnList = "user_id"),
    @Index(name = "idx_notif_read", columnList = "is_read")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private boolean read = false;

    // Metadata payload stored as JSON/TEXT
    @Column(columnDefinition = "TEXT")
    private String data;
}
