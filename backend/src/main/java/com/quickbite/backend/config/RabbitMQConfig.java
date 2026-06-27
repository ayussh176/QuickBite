package com.quickbite.backend.config;

import com.quickbite.backend.constants.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ exchange, queue, and binding declarations.
 */
@Configuration
public class RabbitMQConfig {

    // ==================== Exchanges ====================

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(RabbitMQConstants.ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(RabbitMQConstants.NOTIFICATION_EXCHANGE);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(RabbitMQConstants.PAYMENT_EXCHANGE);
    }

    // ==================== Queues ====================

    @Bean
    public Queue orderQueue() {
        return new Queue(RabbitMQConstants.ORDER_QUEUE, true);
    }

    @Bean
    public Queue notificationEmailQueue() {
        return new Queue(RabbitMQConstants.NOTIFICATION_EMAIL_QUEUE, true);
    }

    @Bean
    public Queue notificationSmsQueue() {
        return new Queue(RabbitMQConstants.NOTIFICATION_SMS_QUEUE, true);
    }

    @Bean
    public Queue notificationPushQueue() {
        return new Queue(RabbitMQConstants.NOTIFICATION_PUSH_QUEUE, true);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(RabbitMQConstants.PAYMENT_QUEUE, true);
    }

    // ==================== Bindings ====================

    @Bean
    public Binding orderBinding(Queue orderQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with(RabbitMQConstants.ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding notificationEmailBinding(Queue notificationEmailQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationEmailQueue).to(notificationExchange)
                .with(RabbitMQConstants.NOTIFICATION_EMAIL_ROUTING_KEY);
    }

    @Bean
    public Binding notificationSmsBinding(Queue notificationSmsQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationSmsQueue).to(notificationExchange)
                .with(RabbitMQConstants.NOTIFICATION_SMS_ROUTING_KEY);
    }

    @Bean
    public Binding notificationPushBinding(Queue notificationPushQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationPushQueue).to(notificationExchange)
                .with(RabbitMQConstants.NOTIFICATION_PUSH_ROUTING_KEY);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with(RabbitMQConstants.PAYMENT_ROUTING_KEY);
    }

    // ==================== Message Converter ====================

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
