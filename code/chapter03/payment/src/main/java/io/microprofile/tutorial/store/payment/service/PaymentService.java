package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.interceptor.Logged;
import jakarta.enterprise.context.ApplicationScoped;
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
