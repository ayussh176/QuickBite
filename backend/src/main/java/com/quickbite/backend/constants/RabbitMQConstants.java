package com.quickbite.backend.constants;

/**
 * RabbitMQ exchange, queue, and routing key constants.
 */
public final class RabbitMQConstants {

    private RabbitMQConstants() {
        // utility class
    }

    // ---------- Exchanges ----------
    public static final String ORDER_EXCHANGE = "quickbite.order.exchange";
    public static final String NOTIFICATION_EXCHANGE = "quickbite.notification.exchange";
    public static final String PAYMENT_EXCHANGE = "quickbite.payment.exchange";

    // ---------- Queues ----------
    public static final String ORDER_QUEUE = "quickbite.order.queue";
    public static final String NOTIFICATION_EMAIL_QUEUE = "quickbite.notification.email.queue";
    public static final String NOTIFICATION_SMS_QUEUE = "quickbite.notification.sms.queue";
    public static final String NOTIFICATION_PUSH_QUEUE = "quickbite.notification.push.queue";
    public static final String PAYMENT_QUEUE = "quickbite.payment.queue";

    // ---------- Routing Keys ----------
    public static final String ORDER_ROUTING_KEY = "order.#";
    public static final String NOTIFICATION_EMAIL_ROUTING_KEY = "notification.email";
    public static final String NOTIFICATION_SMS_ROUTING_KEY = "notification.sms";
    public static final String NOTIFICATION_PUSH_ROUTING_KEY = "notification.push";
    public static final String PAYMENT_ROUTING_KEY = "payment.#";
}
