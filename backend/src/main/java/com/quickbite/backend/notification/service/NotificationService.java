package com.quickbite.backend.notification.service;

import com.quickbite.backend.auth.entity.User;
import com.quickbite.backend.auth.repository.UserRepository;
import com.quickbite.backend.common.enums.NotificationType;
import com.quickbite.backend.constants.RabbitMQConstants;
import com.quickbite.backend.exception.ForbiddenException;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.notification.dto.NotificationMessage;
import com.quickbite.backend.notification.dto.NotificationResponse;
import com.quickbite.backend.notification.entity.Notification;
import com.quickbite.backend.notification.mapper.NotificationMapper;
import com.quickbite.backend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    private final NotificationMapper notificationMapper;

    public void sendNotification(Long userId, String title, String message, NotificationType type, String data) {
        log.info("Publishing notification to RabbitMQ for user ID: {}", userId);
        NotificationMessage notifMessage = NotificationMessage.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .notificationType(type)
                .data(data)
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConstants.NOTIFICATION_EXCHANGE,
                RabbitMQConstants.NOTIFICATION_PUSH_ROUTING_KEY,
                notifMessage
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.NOTIFICATION_EXCHANGE,
                RabbitMQConstants.NOTIFICATION_EMAIL_ROUTING_KEY,
                notifMessage
        );
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.NOTIFICATION_EXCHANGE,
                RabbitMQConstants.NOTIFICATION_SMS_ROUTING_KEY,
                notifMessage
        );
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getHistory(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationResponse markAsRead(String email, Long notificationId) {
        log.info("Marking notification ID: {} as read by: {}", notificationId, email);
        User user = getUserByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to modify this notification.");
        }

        notification.setRead(true);
        Notification saved = notificationRepository.save(notification);
        return notificationMapper.toResponse(saved);
    }

    @Transactional
    public void markAllAsRead(String email) {
        log.info("Marking all notifications as read for: {}", email);
        User user = getUserByEmail(email);
        notificationRepository.markAllAsRead(user.getId());
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
