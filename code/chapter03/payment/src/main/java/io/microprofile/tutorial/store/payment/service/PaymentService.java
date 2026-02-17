package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.interceptor.Logged;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.math.BigDecimal;

/**
 * Service class for Payment operations.
 * 
 * This class demonstrates CDI interceptors with @Logged annotation.
 * Notice: No manual logging code needed! The interceptor automatically logs:
 * - Method entry with parameter values
 * - Method exit with return value
 * - Execution time
 * - Any exceptions thrown
 */
@ApplicationScoped
@Logged  // This annotation enables automatic logging for all methods
public class PaymentService {
    
    @Inject
    private IdempotencyService idempotencyService;

    /**
     * Processes a payment.
     *
     * @param paymentDetails Payment details to process
     * @return true if payment successful, false otherwise
     */
    public boolean processPayment(PaymentDetails paymentDetails) {
        // No manual logging needed - interceptor handles it!
        
        if (!validatePaymentDetails(paymentDetails)) {
            return false;
        }
        
        // Simulate payment processing
        return true;
    }
    
    /**
     * Processes a payment with idempotency support.
     * This method ensures that duplicate payment requests with the same paymentId
     * are handled safely and consistently.
     *
     * @param paymentId Unique payment identifier
     * @param paymentDetails Payment details to process
     * @return IdempotencyService result or newly processed payment result
     */
    public io.microprofile.tutorial.store.payment.entity.IdempotencyRecord 
            processPaymentIdempotent(String paymentId, PaymentDetails paymentDetails) {
        // Check if this payment was already processed
        var existingRecord = idempotencyService.getExistingRecord(paymentId);
        
        if (existingRecord.isPresent()) {
            // Payment already processed - return cached result
            return existingRecord.get();
        }
        
        // Process the payment
        boolean success = processPayment(paymentDetails);
        String message = success ? "Payment processed successfully" : "Payment validation failed";
        
        // Store the result for future idempotency checks
        idempotencyService.storeRecord(paymentId, paymentDetails, success, message);
        
        // Return the newly created record
        return idempotencyService.getExistingRecord(paymentId)
                .orElseThrow(() -> new IllegalStateException("Failed to store idempotency record"));
    }
    
    /**
     * Validates payment details.
     *
     * @param paymentDetails Payment details to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePaymentDetails(PaymentDetails paymentDetails) {
        // Interceptor will log entry with the paymentDetails parameter
        
        if (paymentDetails == null) {
            return false;
        }
        
        // Basic validation checks
        if (paymentDetails.getCardNumber() == null || 
            paymentDetails.getCardNumber().trim().isEmpty()) {
            return false;
        }
        
        if (paymentDetails.getCardHolderName() == null || 
            paymentDetails.getCardHolderName().trim().isEmpty()) {
            return false;
        }
        
        if (paymentDetails.getAmount() == null || 
            paymentDetails.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (paymentDetails.getExpiryDate() == null || 
            paymentDetails.getExpiryDate().trim().isEmpty()) {
            return false;
        }
        
        if (paymentDetails.getSecurityCode() == null || 
            paymentDetails.getSecurityCode().trim().isEmpty()) {
            return false;
        }
        
        // Interceptor will log exit with return value (true)
        return true;
    }
    
    /**
     * Refunds a payment.
     *
     * @param amount Amount to refund
     * @return true if refund successful, false otherwise
     */
    public boolean refundPayment(BigDecimal amount) {
        // Interceptor logs this method call with the amount parameter
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // Simulate refund processing
        return true;
    }
}
