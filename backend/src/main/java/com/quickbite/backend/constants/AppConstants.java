package com.quickbite.backend.constants;

/**
 * Application-wide constants.
 */
public final class AppConstants {

    private AppConstants() {
        // utility class
    }

    // ---------- Pagination ----------
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIR = "desc";

    // ---------- API Versioning ----------
    public static final String API_V1 = "/v1";

    // ---------- Roles ----------
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_RESTAURANT = "ROLE_RESTAURANT";
    public static final String ROLE_DELIVERY = "ROLE_DELIVERY";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // ---------- Order Statuses ----------
    public static final String ORDER_PLACED = "PLACED";
    public static final String ORDER_CONFIRMED = "CONFIRMED";
    public static final String ORDER_PREPARING = "PREPARING";
    public static final String ORDER_READY = "READY";
    public static final String ORDER_PICKED_UP = "PICKED_UP";
    public static final String ORDER_DELIVERED = "DELIVERED";
    public static final String ORDER_CANCELLED = "CANCELLED";

    // ---------- Payment Statuses ----------
    public static final String PAYMENT_PENDING = "PENDING";
    public static final String PAYMENT_COMPLETED = "COMPLETED";
    public static final String PAYMENT_FAILED = "FAILED";
    public static final String PAYMENT_REFUNDED = "REFUNDED";
}
