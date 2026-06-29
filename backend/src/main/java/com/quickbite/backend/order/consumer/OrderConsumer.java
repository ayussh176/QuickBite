package com.quickbite.backend.order.consumer;

import com.quickbite.backend.constants.RabbitMQConstants;
import com.quickbite.backend.order.dto.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConstants.ORDER_QUEUE)
    public void consumeOrderUpdate(OrderMessage message) {
        log.info("Received order status update from RabbitMQ: OrderNumber={}, Status={}",
                message.getOrderNumber(), message.getStatus());

        // Broadcast to customer
        if (message.getCustomerEmail() != null) {
            messagingTemplate.convertAndSendToUser(
                    message.getCustomerEmail(),
                    "/queue/orders",
                    message
            );
            log.info("Broadcasted order update to customer: {}", message.getCustomerEmail());
        }

        // Broadcast to restaurant owner
        if (message.getRestaurantEmail() != null) {
            messagingTemplate.convertAndSendToUser(
                    message.getRestaurantEmail(),
                    "/queue/orders",
                    message
            );
            log.info("Broadcasted order update to restaurant owner: {}", message.getRestaurantEmail());
        }

        // Broadcast to delivery partner (rider) if assigned
        if (message.getRiderEmail() != null) {
            messagingTemplate.convertAndSendToUser(
                    message.getRiderEmail(),
                    "/queue/orders",
                    message
            );
            log.info("Broadcasted order update to delivery rider: {}", message.getRiderEmail());
        }
    }
}
