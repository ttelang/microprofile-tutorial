package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class PaymentService {

    @ConfigProperty(name = "payment.gateway.endpoint", defaultValue = "https://defaultapi.paymentgateway.com")
    private String endpoint;

    /**
     * Process the payment request.
     *
     * @param paymentDetails details of the payment
     * @return response message indicating success or failure
     * @throws PaymentProcessingException if a transient issue occurs
     */
    @Asynchronous
    @Timeout(3000)
    @Retry(maxRetries = 3, 
        delay = 2000, 
        jitter = 500, 
        retryOn = PaymentProcessingException.class, 
        abortOn = CriticalPaymentException.class)
    @Fallback(fallbackMethod = "fallbackProcessPayment")
    @Bulkhead(value=5)
    @CircuitBreaker(
        failureRatio = 0.5,
        requestVolumeThreshold = 4,
        delay = 3000
    )
    public CompletionStage<String> processPayment(PaymentDetails paymentDetails) throws PaymentProcessingException {
        
        // Example logic to call the payment gateway API
        System.out.println("Calling payment gateway API at: " + endpoint);

        simulateDelay();

        System.out.println("Processing payment for amount: " + paymentDetails.getAmount());

        // Simulating a transient failure
        if (Math.random() > 0.7) {
            throw new PaymentProcessingException("Temporary payment processing failure");
        }

        // Simulating successful processing
        return CompletableFuture.completedFuture("{\"status\":\"success\", \"message\":\"Payment processed successfully.\"}");
    }

    /**
     * Fallback method when payment processing fails.
     *
     * @param paymentDetails details of the payment
     * @return response message for fallback
     */
    public CompletionStage<String> fallbackProcessPayment(PaymentDetails paymentDetails) {
        System.out.println("Fallback invoked for payment of amount: " + paymentDetails.getAmount());
        return CompletableFuture.completedFuture("{\"status\":\"failed\", \"message\":\"Payment service is currently unavailable.\"}");
    }

    /**
     * Simulate a delay in processing to demonstrate timeout.
     */
    private void simulateDelay() {
        try {
            Thread.sleep(1500); // Simulated long-running task
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted");
        }
    }
}
