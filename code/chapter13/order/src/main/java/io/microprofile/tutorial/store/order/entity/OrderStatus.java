package io.microprofile.tutorial.store.order.entity;

/**
 * Minimal order states reused from the tutorial's store application.
 */
public enum OrderStatus {
    CREATED,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
