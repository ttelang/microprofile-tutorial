package io.microprofile.tutorial.store.order.messaging;

import io.microprofile.tutorial.store.order.entity.Order;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.util.logging.Logger;

/**
 * Terminal consumer for large orders so the demo has a complete internal flow.
 */
@ApplicationScoped
public class LargeOrderReviewHandler {

    private static final Logger LOGGER = Logger.getLogger(LargeOrderReviewHandler.class.getName());

    @Incoming("large-orders")
    public void reviewLargeOrder(Order order) {
        LOGGER.info("Large order queued for review: " + order.getOrderId());
    }
}
