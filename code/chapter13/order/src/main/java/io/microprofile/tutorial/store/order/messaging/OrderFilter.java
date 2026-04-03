package io.microprofile.tutorial.store.order.messaging;

import io.microprofile.tutorial.store.order.entity.Order;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;

import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Filters the paid order stream to identify high-value orders that may need
 * extra review or special handling in the store workflow.
 */
@ApplicationScoped
public class OrderFilter {
    
    private static final Logger LOGGER = Logger.getLogger(OrderFilter.class.getName());
    private static final BigDecimal LARGE_ORDER_THRESHOLD = BigDecimal.valueOf(1000.00);
    
    @Incoming("paid-orders")
    @Outgoing("large-orders")
    public PublisherBuilder<Order> filterLargeOrders(PublisherBuilder<Order> orders) {
        return orders
                .filter(order -> order.getTotalPrice() != null
                        && order.getTotalPrice().compareTo(LARGE_ORDER_THRESHOLD) > 0)
                .peek(order -> LOGGER.info("High-value paid order routed for review: "
                        + order.getOrderId() + " - $" + order.getTotalPrice()));
    }
}
