package com.quickbite.backend.notification.consumer;

import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.auth.repository.UserRepository;
import com.quickbite.backend.constants.RabbitMQConstants;
import com.quickbite.backend.notification.dto.NotificationMessage;
import com.quickbite.backend.notification.dto.NotificationResponse;
import com.quickbite.backend.notification.entity.Notification;
import com.quickbite.backend.notification.mapper.NotificationMapper;
import com.quickbite.backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    private final NotificationMapper notificationMapper;

    @Transactional
    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_PUSH_QUEUE)
    public void consumeNotification(NotificationMessage message) {
        log.info("Received notification from RabbitMQ for user ID: {}", message.getUserId());

        try {
            User user = userRepository.findById(message.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + message.getUserId()));

            // 1. Persist notification to MySQL
            Notification notification = Notification.builder()
                    .user(user)
                    .title(message.getTitle())
                    .message(message.getMessage())
                    .notificationType(message.getNotificationType())
                    .data(message.getData())
                    .read(false)
                    .build();

            Notification saved = notificationRepository.save(notification);
            log.info("Notification ID: {} persisted successfully", saved.getId());

            // 2. Broadcast via Spring WebSocket
            NotificationResponse response = notificationMapper.toResponse(saved);
            
            // Destination mappings will route to /user/{userEmail}/queue/notifications
            messagingTemplate.convertAndSendToUser(
                    user.getEmail(),
                    "/queue/notifications",
                    response
            );
            log.info("Notification broadcasted over WebSocket to user: {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to process notification message: {}", e.getMessage(), e);
        }
    }
}
