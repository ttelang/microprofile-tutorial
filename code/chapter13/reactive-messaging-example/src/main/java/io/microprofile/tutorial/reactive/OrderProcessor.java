package io.microprofile.tutorial.reactive;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

/**
 * Example demonstrating message acknowledgment strategies and async processing.
 */
@ApplicationScoped
public class OrderProcessor {
    
    private static final Logger LOGGER = Logger.getLogger(OrderProcessor.class.getName());
    
    /**
     * Processes orders with manual acknowledgment control.
     * Demonstrates async processing with proper message acknowledgment.
     * 
     * @param message the order message
     * @return CompletionStage indicating processing completion
     */
    @Incoming("orders")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> processOrder(Message<Order> message) {
        Order order = message.getPayload();
        LOGGER.info("Processing order: " + order.getId());
        
        // Simulate async order processing
        return processOrderAsync(order)
                .thenCompose(v -> {
                    LOGGER.info("Order processed successfully: " + order.getId());
                    return message.ack();
                })
                .exceptionally(throwable -> {
                    LOGGER.severe("Order processing failed: " + order.getId() + 
                                 " - " + throwable.getMessage());
                    message.nack(throwable);
                    return null;
                });
    }
    
    /**
     * Simulates asynchronous order processing.
     * 
     * @param order the order to process
     * @return CompletionStage indicating completion
     */
    private CompletionStage<Void> processOrderAsync(Order order) {
        return java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                // Simulate processing time
                Thread.sleep(100);
                
                // Validate order
                if (order.getAmount() <= 0) {
                    throw new IllegalArgumentException("Invalid order amount");
                }
                
                LOGGER.info("Order validated and processed: " + order.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Processing interrupted", e);
            }
        });
    }
}
