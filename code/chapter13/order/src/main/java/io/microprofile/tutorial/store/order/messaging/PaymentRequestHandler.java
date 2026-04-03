package io.microprofile.tutorial.store.order.messaging;

import io.microprofile.tutorial.store.order.entity.Order;
import io.microprofile.tutorial.store.order.entity.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

/**
 * Handles the payment hand-off after an order has been created in the store app.
 * The full payment-side logic now lives in the dedicated Chapter 13 `payment` service.
 */
@ApplicationScoped
public class PaymentRequestHandler {
    
    private static final Logger LOGGER = Logger.getLogger(PaymentRequestHandler.class.getName());
    
    @Incoming("orders")
    @Outgoing("paid-orders")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Order> requestPayment(Message<Order> message) {
        Order order = message.getPayload();
        LOGGER.info("Forwarding order to the Chapter 13 payment service: " + order.getOrderId());
        
        return CompletableFuture.supplyAsync(() -> {
            if (order.getOrderId() == null) {
                throw new IllegalArgumentException("Order ID cannot be null");
            }

            if (order.getTotalPrice() == null || order.getTotalPrice().signum() <= 0) {
                throw new IllegalArgumentException("Order total must be greater than zero");
            }

            // Simulate a successful async hand-off to the dedicated payment service.
            order.setStatus(OrderStatus.PAID);
            return order;
        }).thenCompose(paidOrder -> message.ack().thenApply(v -> {
            LOGGER.info("Payment hand-off completed for order: " + paidOrder.getOrderId());
            return paidOrder;
        })).exceptionallyCompose(throwable -> message.nack(throwable)
                .thenCompose(v -> CompletableFuture.<Order>failedStage(throwable)));
    }
}
