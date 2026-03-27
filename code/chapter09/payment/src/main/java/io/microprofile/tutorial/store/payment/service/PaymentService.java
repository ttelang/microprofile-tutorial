package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
public class PaymentService {

    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());
    private static final AttributeKey<String> PAYMENT_METHOD_KEY = AttributeKey.stringKey("payment.method");
    private static final AttributeKey<String> PAYMENT_RESULT_KEY = AttributeKey.stringKey("payment.result");

    @Inject
    Tracer tracer;

    @Inject
    Meter meter;

    @Inject
    @ConfigProperty(name = "payment.gateway.endpoint", defaultValue = "https://api.paymentgateway.com")
    String endpoint;

    private LongCounter paymentAttemptsCounter;

    @PostConstruct
    public void init() {
        paymentAttemptsCounter = meter
                .counterBuilder("payment.attempts")
                .setDescription("Number of payment attempts by result")
                .setUnit("1")
                .build();
        logger.info("Telemetry instruments initialized successfully");
    }

    /**
     * Process the payment request with automatic tracing via MicroProfile Telemetry.
     * The mpTelemetry feature automatically creates spans for this method.
     *
     * @param paymentDetails details of the payment
     * @return response message indicating success or failure
     * @throws PaymentProcessingException if a transient issue occurs
     */
    @Asynchronous
    @Timeout(3000)
    @Retry(maxRetries = 3, delay = 2000, jitter = 500, retryOn = PaymentProcessingException.class, abortOn = CriticalPaymentException.class)
    @Fallback(fallbackMethod = "fallbackProcessPayment")
    @Bulkhead(value=5)
    public CompletionStage<String> processPayment(PaymentDetails paymentDetails) throws PaymentProcessingException {
        paymentAttemptsCounter.add(1, Attributes.of(PAYMENT_METHOD_KEY, "credit_card", PAYMENT_RESULT_KEY, "attempt"));

        // Create explicit span for payment processing to enrich business context.
        Span span = tracer.spanBuilder("payment.process")
            .setAttribute("order.id", "unknown")
            .setAttribute("payment.amount", paymentDetails.getAmount().doubleValue())
            .setAttribute("payment.method", "credit_card")
            .setAttribute("payment.service", "payment-service")
            .setAttribute("payment.status", "IN_PROGRESS")
            .startSpan();
        
        try (io.opentelemetry.context.Scope scope = span.makeCurrent()) {
            String maskedCardNumber = maskCardNumber(paymentDetails.getCardNumber());
            
            logger.info(String.format("Processing payment - Amount: %s, Card: %s", 
                    paymentDetails.getAmount(), maskedCardNumber));
            
            span.addEvent("Starting payment processing");
            simulateDelay();

            // Simulating a transient failure
            if (Math.random() > 0.7) {
                span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "Payment processing failed");
                span.addEvent("Payment processing failed due to transient error");
                span.setAttribute("payment.status", "FAILED");
                paymentAttemptsCounter.add(1, Attributes.of(PAYMENT_METHOD_KEY, "credit_card", PAYMENT_RESULT_KEY, "failed"));
                logger.warning("Payment processing failed due to transient error");
                throw new PaymentProcessingException("Temporary payment processing failure");
            }

            // Simulating successful processing
            span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
            span.setAttribute("payment.status", "SUCCESS");
            span.addEvent("Payment processed successfully");
            paymentAttemptsCounter.add(1, Attributes.of(PAYMENT_METHOD_KEY, "credit_card", PAYMENT_RESULT_KEY, "success"));
            logger.info("Payment processed successfully");
            return CompletableFuture.completedFuture("{\"status\":\"success\", \"message\":\"Payment processed successfully.\"}");
        } finally {
            span.end();
        }
    }

    /**
     * Fallback method when payment processing fails.
     * Automatically traced by MicroProfile Telemetry.
     *
     * @param paymentDetails details of the payment
     * @return response message for fallback
     */
    public CompletionStage<String> fallbackProcessPayment(PaymentDetails paymentDetails) {
        logger.warning(() -> String.format("Fallback invoked for payment - Amount: %s", 
                paymentDetails.getAmount()));
        
        return CompletableFuture.completedFuture("{\"status\":\"failed\", \"message\":\"Payment service is currently unavailable.\"}");
    }

    /**
     * Check payment gateway health with circuit breaker protection.
     */
    @CircuitBreaker(
        failureRatio = 0.5,
        requestVolumeThreshold = 4,
        delay = 5000,
        successThreshold = 2
    )
    @Timeout(2000)
    public boolean checkGatewayHealth() {
        logger.info("Checking payment gateway health at: " + endpoint);

        simulateDelay(500);

        if (Math.random() > 0.6) {
            throw new RuntimeException("Gateway health check failed");
        }

        return true;
    }

    /**
     * Send payment notification asynchronously with bulkhead isolation.
     */
    @Asynchronous
    @Bulkhead(value = 10, waitingTaskQueue = 20)
    @Timeout(5000)
    @Fallback(fallbackMethod = "fallbackSendNotification")
    public CompletionStage<String> sendPaymentNotification(String paymentId, String recipient) {
        logger.info("Notification queued for payment: " + paymentId + " to " + recipient);

        CompletableFuture.runAsync(() -> {
            try {
                simulateDelay(2000);

                if (Math.random() > 0.8) {
                    logger.warning("Notification service unavailable for payment: " + paymentId);
                    throw new RuntimeException("Notification service unavailable");
                }

                logger.info("Notification sent successfully for payment: " + paymentId);
            } catch (Exception e) {
                logger.severe("Failed to send notification for payment: " + paymentId + " - " + e.getMessage());
            }
        });

        return CompletableFuture.completedFuture("Notification queued for processing");
    }

    public CompletionStage<String> fallbackSendNotification(String paymentId, String recipient) {
        logger.warning("Failed to send notification for payment: " + paymentId);
        return CompletableFuture.completedFuture("Notification queued for retry");
    }

    /**
     * Masks a credit card number for security in logs and traces.
     * Only the last 4 digits are shown, all others are replaced with 'X'.
     *
     * @param cardNumber The full card number
     * @return A masked card number (e.g., "XXXXXXXXXXXX1234")
     */
    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "INVALID_CARD";
        }
        int visibleDigits = 4;
        int length = cardNumber.length();
        
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < length - visibleDigits; i++) {
            masked.append('X');
        }
        masked.append(cardNumber.substring(length - visibleDigits));
        
        return masked.toString();
    }

    /**
     * Simulate a delay in processing to demonstrate timeout.
     * This method will be automatically traced by MicroProfile Telemetry.
     */
    private void simulateDelay() {
        try {
            logger.fine("Starting payment processing delay simulation");
            Thread.sleep(1500); // Simulated long-running task
            logger.fine("Payment processing delay simulation completed");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Payment processing interrupted");
            throw new RuntimeException("Processing interrupted");
        }
    }

    /**
     * Processes a comprehensive payment verification with multiple steps.
     * Each method call will be automatically traced by MicroProfile Telemetry.
     *
     * @param paymentDetails The payment details to verify
     * @param transactionId The unique transaction ID
     * @return A detailed verification result
     * @throws PaymentProcessingException if verification fails
     */
    @Asynchronous
    public CompletionStage<String> verifyPaymentWithTelemetry(PaymentDetails paymentDetails, String transactionId) 
            throws PaymentProcessingException {
        logger.info(() -> String.format("Starting payment verification - Transaction ID: %s", transactionId));

        Span span = tracer.spanBuilder("payment.verify")
            .setAttribute("payment.method", "credit_card")
            .setAttribute("payment.status", "IN_PROGRESS")
            .setAttribute("payment.service", "payment-service")
            .setAttribute("payment.transaction_id", transactionId)
            .startSpan();

        if (paymentDetails != null && paymentDetails.getAmount() != null) {
            span.setAttribute("payment.amount", paymentDetails.getAmount().doubleValue());
        }

        try (io.opentelemetry.context.Scope scope = span.makeCurrent()) {
            // Step 1: Validate payment details
            validatePaymentDetails(paymentDetails);
            
            // Step 2: Check for fraud indicators
            performFraudCheck(paymentDetails, transactionId);
            
            // Step 3: Verify funds with bank
            verifyFundsAvailability(paymentDetails);
            
            // Step 4: Record transaction
            recordTransaction(paymentDetails, transactionId);

            span.setStatus(io.opentelemetry.api.trace.StatusCode.OK);
            span.setAttribute("payment.status", "SUCCESS");
            logger.info("Payment verification completed successfully");
            return CompletableFuture.completedFuture(
                    String.format("{\"status\":\"verified\", \"transaction_id\":\"%s\", \"message\":\"Payment verification complete.\"}", 
                            transactionId));
        } catch (Exception e) {
            span.recordException(e);
            span.setAttribute("payment.status", "FAILED");
            span.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, e.getMessage());
            logger.severe(() -> String.format("Payment verification failed: %s", e.getMessage()));
            throw e;
        } finally {
            span.end();
        }
    }
    
    /**
     * Validates payment details - automatically traced
     */
    private void validatePaymentDetails(PaymentDetails details) throws PaymentProcessingException {
        logger.info("Validating payment details");
        
        boolean isValid = details.getCardNumber() != null && 
                         details.getCardNumber().length() >= 15 &&
                         details.getExpiryDate() != null &&
                         details.getAmount() != null &&
                         details.getAmount().doubleValue() > 0;
        
        if (!isValid) {
            logger.warning("Payment details validation failed");
            throw new PaymentProcessingException("Payment details validation failed");
        }
        
        logger.info("Payment details validation successful");
    }
    
    /**
     * Performs fraud check - automatically traced
     */
    private void performFraudCheck(PaymentDetails details, String transactionId) throws PaymentProcessingException {
        logger.info(() -> String.format("Performing fraud check for transaction: %s", transactionId));
        
        // Simulate external service call
        simulateNetworkCall(300);
        
        // Simulate fraud detection (cards ending with "0000" are flagged)
        boolean isSafe = !details.getCardNumber().endsWith("0000");
        
        if (!isSafe) {
            logger.warning("Potential fraud detected");
            throw new PaymentProcessingException("Fraud check failed");
        }
        
        logger.info("Fraud check passed");
    }
    
    /**
     * Verifies funds availability - automatically traced
     */
    private void verifyFundsAvailability(PaymentDetails details) throws PaymentProcessingException {
        logger.info(() -> String.format("Verifying funds availability - Amount: %s", details.getAmount()));
        
        // Simulate banking service call
        simulateNetworkCall(500);
        
        // Simulate funds verification (amounts over 1000 fail)
        boolean hasFunds = details.getAmount().doubleValue() <= 1000;
        
        if (!hasFunds) {
            logger.warning("Insufficient funds detected");
            throw new PaymentProcessingException("Insufficient funds");
        }
        
        logger.info("Sufficient funds verified");
    }
    
    /**
     * Records transaction - automatically traced
     */
    private void recordTransaction(PaymentDetails details, String transactionId) {
        logger.info(() -> String.format("Recording transaction: %s", transactionId));
        
        // Simulate database operation
        simulateNetworkCall(200);
        
        logger.info("Transaction recorded successfully");
    }
    
    /**
     * Simulates network calls or database operations - automatically traced
     */
    private void simulateNetworkCall(int milliseconds) {
        try {
            logger.fine(() -> String.format("Simulating network call - Duration: %dms", milliseconds));
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.severe("Network call interrupted");
            throw new RuntimeException("Network call interrupted");
        }
    }

    private void simulateDelay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
}
