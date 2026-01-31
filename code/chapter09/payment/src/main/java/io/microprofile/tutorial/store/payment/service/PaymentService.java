package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;

import org.eclipse.microprofile.faulttolerance.Asynchronous;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
public class PaymentService {

    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    @Inject
    Tracer tracer;   // CDI-injected tracer for MicroProfile Telemetry 2.1

    /**
     * Process the payment request with automatic tracing via MicroProfile Telemetry 2.1.
     * The mpTelemetry-2.1 feature automatically creates spans for this method.
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
    @WithSpan("payment.process")  // MicroProfile Telemetry 2.1 - automatic span creation
    public CompletionStage<String> processPayment(PaymentDetails paymentDetails) throws PaymentProcessingException {
        // Create explicit span for additional payment processing details
        Span span = tracer.spanBuilder("payment.process.detailed")
            .setAttribute("payment.amount", paymentDetails.getAmount().toString())
            .setAttribute("payment.method", "credit_card")
            .setAttribute("payment.service", "payment-service") 
            .startSpan();
        
        try (var scope = span.makeCurrent()) {
            // MicroProfile Telemetry 2.1 automatically traces this method
            String maskedCardNumber = maskCardNumber(paymentDetails.getCardNumber());
            
            logger.info(String.format("Processing payment - Amount: %s, Card: %s", 
                    paymentDetails.getAmount(), maskedCardNumber));
            
            span.addEvent("Starting payment processing");
            simulateDelay();

            // Simulating a transient failure
            if (Math.random() > 0.7) {
                span.setStatus(StatusCode.ERROR, "Payment processing failed");
                span.addEvent("Payment processing failed due to transient error");
                logger.warning("Payment processing failed due to transient error");
                throw new PaymentProcessingException("Temporary payment processing failure");
            }

            // Simulating successful processing
            span.setStatus(StatusCode.OK);
            span.addEvent("Payment processed successfully");
            logger.info("Payment processed successfully");
            return CompletableFuture.completedFuture("{\"status\":\"success\", \"message\":\"Payment processed successfully.\"}");
        } catch (PaymentProcessingException e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    /**
     * Fallback method when payment processing fails.
     * Automatically traced by MicroProfile Telemetry 2.1.
     *
     * @param paymentDetails details of the payment
     * @return response message for fallback
     */
    @WithSpan("payment.fallback")
    public CompletionStage<String> fallbackProcessPayment(PaymentDetails paymentDetails) {
        logger.warning(() -> String.format("Fallback invoked for payment - Amount: %s", 
                paymentDetails.getAmount()));
        
        return CompletableFuture.completedFuture("{\"status\":\"failed\", \"message\":\"Payment service is currently unavailable.\"}");
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
     * This method will be automatically traced by MicroProfile Telemetry 2.1.
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
     * Each method call will be automatically traced by MicroProfile Telemetry 2.1.
     *
     * @param paymentDetails The payment details to verify
     * @param transactionId The unique transaction ID
     * @return A detailed verification result
     * @throws PaymentProcessingException if verification fails
     */
    @Asynchronous
    @WithSpan("payment.verify")
    public CompletionStage<String> verifyPaymentWithTelemetry(PaymentDetails paymentDetails, String transactionId) 
            throws PaymentProcessingException {
        
        logger.info(() -> String.format("Starting payment verification - Transaction ID: %s", transactionId));
        
        Span currentSpan = Span.current();
        currentSpan.setAttribute("transaction.id", transactionId);
        currentSpan.setAttribute("verification.type", "comprehensive");
        
        try {
            // Step 1: Validate payment details
            validatePaymentDetails(paymentDetails);
            
            // Step 2: Check for fraud indicators
            performFraudCheck(paymentDetails, transactionId);
            
            // Step 3: Verify funds with bank
            verifyFundsAvailability(paymentDetails);
            
            // Step 4: Record transaction
            recordTransaction(paymentDetails, transactionId);
            
            currentSpan.setStatus(StatusCode.OK);
            logger.info("Payment verification completed successfully");
            return CompletableFuture.completedFuture(
                    String.format("{\"status\":\"verified\", \"transaction_id\":\"%s\", \"message\":\"Payment verification complete.\"}", 
                            transactionId));
        } catch (Exception e) {
            currentSpan.setStatus(StatusCode.ERROR, e.getMessage());
            currentSpan.recordException(e);
            logger.severe(() -> String.format("Payment verification failed: %s", e.getMessage()));
            throw e;
        }
    }
    
    /**
     * Validates payment details - automatically traced with @WithSpan
     */
    @WithSpan("payment.validate")
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
     * Performs fraud check - automatically traced with @WithSpan
     */
    @WithSpan("payment.fraud_check")
    private void performFraudCheck(PaymentDetails details, String transactionId) throws PaymentProcessingException {
        logger.info(() -> String.format("Performing fraud check for transaction: %s", transactionId));
        
        Span currentSpan = Span.current();
        currentSpan.setAttribute("fraud_check.transaction_id", transactionId);
        
        // Simulate external service call
        simulateNetworkCall(300);
        
        // Simulate fraud detection (cards ending with "0000" are flagged)
        boolean isSafe = !details.getCardNumber().endsWith("0000");
        
        if (!isSafe) {
            currentSpan.setAttribute("fraud_check.result", "flagged");
            logger.warning("Potential fraud detected");
            throw new PaymentProcessingException("Fraud check failed");
        }
        
        currentSpan.setAttribute("fraud_check.result", "passed");
        logger.info("Fraud check passed");
    }
    
    /**
     * Verifies funds availability - automatically traced with @WithSpan
     */
    @WithSpan("payment.verify_funds")
    private void verifyFundsAvailability(PaymentDetails details) throws PaymentProcessingException {
        logger.info(() -> String.format("Verifying funds availability - Amount: %s", details.getAmount()));
        
        Span currentSpan = Span.current();
        currentSpan.setAttribute("funds_check.amount", details.getAmount().toString());
        
        // Simulate banking service call
        simulateNetworkCall(500);
        
        // Simulate funds verification (amounts over 1000 fail)
        boolean hasFunds = details.getAmount().doubleValue() <= 1000;
        
        if (!hasFunds) {
            currentSpan.setAttribute("funds_check.result", "insufficient");
            logger.warning("Insufficient funds detected");
            throw new PaymentProcessingException("Insufficient funds");
        }
        
        currentSpan.setAttribute("funds_check.result", "sufficient");
        logger.info("Sufficient funds verified");
    }
    
    /**
     * Records transaction - automatically traced with @WithSpan
     */
    @WithSpan("payment.record_transaction")
    private void recordTransaction(PaymentDetails details, String transactionId) {
        logger.info(() -> String.format("Recording transaction: %s", transactionId));
        
        Span currentSpan = Span.current();
        currentSpan.setAttribute("transaction.id", transactionId);
        currentSpan.setAttribute("transaction.amount", details.getAmount().toString());
        
        // Simulate database operation
        simulateNetworkCall(200);
        
        currentSpan.addEvent("Transaction recorded in database");
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
}
