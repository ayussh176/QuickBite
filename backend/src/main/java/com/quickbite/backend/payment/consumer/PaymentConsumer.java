package com.quickbite.backend.payment.consumer;

import com.quickbite.backend.constants.RabbitMQConstants;
import com.quickbite.backend.payment.dto.PaymentMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConstants.PAYMENT_QUEUE)
    public void consumePaymentUpdate(PaymentMessage message) {
        log.info("Received payment update from RabbitMQ: OrderNumber={}, Status={}, Amount=₹{}",
                message.getOrderNumber(), message.getStatus(), message.getAmount());

        // Broadcast to customer
        if (message.getCustomerEmail() != null) {
            messagingTemplate.convertAndSendToUser(
                    message.getCustomerEmail(),
                    "/queue/payments",
                    message
            );
            log.info("Broadcasted payment update to customer: {}", message.getCustomerEmail());
        }
    }
}
