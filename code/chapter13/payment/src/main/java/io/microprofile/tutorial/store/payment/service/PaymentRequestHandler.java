package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.Order;
import io.microprofile.tutorial.store.payment.entity.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.util.logging.Logger;

/**
 * Reactive messaging handler that runs in the payment service and marks
 * incoming orders as paid for downstream processing.
 */
@ApplicationScoped
public class PaymentRequestHandler {

    private static final Logger LOGGER = Logger.getLogger(PaymentRequestHandler.class.getName());

    @Incoming("order-created")
    @Outgoing("payment-authorized")
    public Order processPayment(Order order) {
        LOGGER.info(() -> "Processing payment for order " + order.getOrderId());
        order.setStatus(OrderStatus.PAID);
        return order;
    }
}
