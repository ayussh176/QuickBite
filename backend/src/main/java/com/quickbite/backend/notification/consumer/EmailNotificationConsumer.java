package com.quickbite.backend.notification.consumer;

import com.quickbite.backend.constants.RabbitMQConstants;
import com.quickbite.backend.notification.dto.NotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationConsumer {

    @RabbitListener(queues = RabbitMQConstants.NOTIFICATION_EMAIL_QUEUE)
    public void consumeEmailNotification(NotificationMessage message) {
        log.info("[EMAIL SENDER] Dispatching email notification to user ID: {} - Title: {} - Message: {}",
                message.getUserId(), message.getTitle(), message.getMessage());
    }
}
