package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditOperationType;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditStatus;
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
 * 
 * Additionally, all operations are logged to the audit trail for compliance.
 */
@ApplicationScoped
@Logged  // This annotation enables automatic logging for all methods
public class PaymentService {
    
    @Inject
    private IdempotencyService idempotencyService;
    
    @Inject
    private PaymentAuditService auditService;

    /**
     * Processes a payment.
     *
     * @param paymentDetails Payment details to process
     * @return true if payment successful, false otherwise
     */
    public boolean processPayment(PaymentDetails paymentDetails) {
        // No manual logging needed - interceptor handles it!
        long startTime = System.currentTimeMillis();
        
        if (!validatePaymentDetails(paymentDetails)) {
            long duration = System.currentTimeMillis() - startTime;
            // Log validation failure to audit trail
            auditService.logAudit(
                    paymentDetails.getPaymentId(),
                    AuditOperationType.PAYMENT_PROCESS,
                    AuditStatus.VALIDATION_ERROR,
                    paymentDetails,
                    "Payment validation failed",
                    duration,
                    false
            );
            return false;
        }
        
        // Simulate payment processing
        boolean success = true;
        long duration = System.currentTimeMillis() - startTime;
        
        // Log to audit trail
        auditService.logAudit(
                paymentDetails.getPaymentId(),
                AuditOperationType.PAYMENT_PROCESS,
                success ? AuditStatus.SUCCESS : AuditStatus.FAILED,
                paymentDetails,
                success ? "Payment processed successfully" : "Payment processing failed",
                duration,
                false
        );
        
        return success;
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
        long startTime = System.currentTimeMillis();
        
        // Check if this payment was already processed
        var existingRecord = idempotencyService.getExistingRecord(paymentId);
        
        if (existingRecord.isPresent()) {
            long duration = System.currentTimeMillis() - startTime;
            // Payment already processed - log cache hit and return cached result
            auditService.logAudit(
                    paymentId,
                    AuditOperationType.IDEMPOTENT_CACHE_HIT,
                    existingRecord.get().isSuccess() ? AuditStatus.SUCCESS : AuditStatus.FAILED,
                    paymentDetails,
                    "Idempotent request - returning cached result",
                    duration,
                    true
            );
            return existingRecord.get();
        }
        
        // Process the payment
        boolean success = processPayment(paymentDetails);
        String message = success ? "Payment processed successfully" : "Payment validation failed";
        long duration = System.currentTimeMillis() - startTime;
        
        // Store the result for future idempotency checks
        idempotencyService.storeRecord(paymentId, paymentDetails, success, message);
        
        // Log to audit trail
        auditService.logAudit(
                paymentId,
                AuditOperationType.IDEMPOTENT_PAYMENT,
                success ? AuditStatus.SUCCESS : AuditStatus.VALIDATION_ERROR,
                paymentDetails,
                message,
                duration,
                false
        );
        
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
        long startTime = System.currentTimeMillis();
        
        if (paymentDetails == null) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logAudit(
                    null,
                    AuditOperationType.PAYMENT_VALIDATE,
                    AuditStatus.VALIDATION_ERROR,
                    null,
                    "Payment details are null",
                    duration,
                    false
            );
            return false;
        }
        
        // Basic validation checks
        if (paymentDetails.getCardNumber() == null || 
            paymentDetails.getCardNumber().trim().isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logAudit(
                    paymentDetails.getPaymentId(),
                    AuditOperationType.PAYMENT_VALIDATE,
                    AuditStatus.VALIDATION_ERROR,
                    paymentDetails,
                    "Card number is missing or empty",
                    duration,
                    false
            );
            return false;
        }
        
        if (paymentDetails.getCardHolderName() == null || 
            paymentDetails.getCardHolderName().trim().isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logAudit(
                    paymentDetails.getPaymentId(),
                    AuditOperationType.PAYMENT_VALIDATE,
                    AuditStatus.VALIDATION_ERROR,
                    paymentDetails,
                    "Cardholder name is missing or empty",
                    duration,
                    false
            );
            return false;
        }
        
        if (paymentDetails.getAmount() == null || 
            paymentDetails.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logAudit(
                    paymentDetails.getPaymentId(),
                    AuditOperationType.PAYMENT_VALIDATE,
                    AuditStatus.VALIDATION_ERROR,
                    paymentDetails,
                    "Amount is invalid (null or <= 0)",
                    duration,
                    false
            );
            return false;
        }
        
        if (paymentDetails.getExpiryDate() == null || 
            paymentDetails.getExpiryDate().trim().isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logAudit(
                    paymentDetails.getPaymentId(),
                    AuditOperationType.PAYMENT_VALIDATE,
                    AuditStatus.VALIDATION_ERROR,
                    paymentDetails,
                    "Expiry date is missing or empty",
                    duration,
                    false
            );
            return false;
        }
        
        if (paymentDetails.getSecurityCode() == null || 
            paymentDetails.getSecurityCode().trim().isEmpty()) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logAudit(
                    paymentDetails.getPaymentId(),
                    AuditOperationType.PAYMENT_VALIDATE,
                    AuditStatus.VALIDATION_ERROR,
                    paymentDetails,
                    "Security code is missing or empty",
                    duration,
                    false
            );
            return false;
        }
        
        // All validations passed
        long duration = System.currentTimeMillis() - startTime;
        auditService.logAudit(
                paymentDetails.getPaymentId(),
                AuditOperationType.PAYMENT_VALIDATE,
                AuditStatus.SUCCESS,
                paymentDetails,
                "Payment details validated successfully",
                duration,
                false
        );
        
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
        long startTime = System.currentTimeMillis();
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logRefundAudit(
                    AuditOperationType.PAYMENT_REFUND,
                    AuditStatus.VALIDATION_ERROR,
                    amount,
                    "Invalid refund amount (null or <= 0)",
                    duration
            );
            return false;
        }
        
        // Simulate refund processing
        boolean success = true;
        long duration = System.currentTimeMillis() - startTime;
        
        auditService.logRefundAudit(
                AuditOperationType.PAYMENT_REFUND,
                success ? AuditStatus.SUCCESS : AuditStatus.FAILED,
                amount,
                success ? "Refund processed successfully" : "Refund processing failed",
                duration
        );
        
        return success;
    }
}
