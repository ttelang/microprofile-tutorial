package io.microprofile.tutorial.reactive;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Example demonstrating stream filtering and transformation with reactive operators.
 */
@ApplicationScoped
public class OrderFilter {
    
    private static final Logger LOGGER = Logger.getLogger(OrderFilter.class.getName());
    private static final double LARGE_ORDER_THRESHOLD = 1000.0;
    
    /**
     * Filters orders to identify large orders (amount > $1000).
     * Demonstrates stream filtering using reactive operators.
     * 
     * @param orders stream of all orders
     * @return stream of large orders only
     */
    @Incoming("all-orders")
    @Outgoing("large-orders")
    public PublisherBuilder<Order> filterLargeOrders(PublisherBuilder<Order> orders) {
        return orders
                .filter(order -> order.getAmount() > LARGE_ORDER_THRESHOLD)
                .peek(order -> LOGGER.info("Large order detected: " + order.getId() + 
                                          " - $" + order.getAmount()));
    }
}
