package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.config.PaymentGatewayConfig;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditOperationType;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditStatus;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.interceptor.Logged;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;

/**
 * Service class for Payment operations with grouped configuration.
 * 
 * Chapter 5 Enhancement: This class demonstrates advanced MicroProfile Config concepts:
 * - Using @ConfigProperties to inject grouped configuration (PaymentGatewayConfig)
 * - Integration with external payment gateways using configured settings
 * - Custom ConfigSource for externalized configuration (demonstrated separately)
 * 
 * This approach is cleaner than injecting individual properties when you have
 * multiple related configuration values that belong together (like gateway settings).
 * 
 * All operations are logged to the audit trail for compliance.
 */
@ApplicationScoped
@Logged  // Automatic logging for all methods
public class PaymentService {
    
    @Inject
    private IdempotencyService idempotencyService;
    
    @Inject
    private PaymentAuditService auditService;

    // NEW: Inject grouped configuration using @ConfigProperties
    @Inject
    private PaymentGatewayConfig gatewayConfig;

    /**
     * Processes a payment using configured payment gateway settings.
     * Demonstrates using grouped configuration properties.
     *
     * @param paymentDetails Payment details to process
     * @return true if payment successful, false otherwise
     */
    public boolean processPayment(PaymentDetails paymentDetails) {
        long startTime = System.currentTimeMillis();
        
        if (!validatePaymentDetails(paymentDetails)) {
            long duration = System.currentTimeMillis() - startTime;
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
        
        // Use grouped configuration from PaymentGatewayConfig
        System.out.println("Processing payment via gateway: " + gatewayConfig.endpoint);
        System.out.println("Gateway timeout: " + gatewayConfig.timeout + "ms");
        System.out.println("Sandbox mode: " + gatewayConfig.sandboxMode);
        
        // Simulate payment processing
        boolean success = true;
        long duration = System.currentTimeMillis() - startTime;
        
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
     *
     * @param paymentId Unique payment identifier
     * @param paymentDetails Payment details to process
     * @return IdempotencyRecord with processing result
     */
    public io.microprofile.tutorial.store.payment.entity.IdempotencyRecord 
            processPaymentIdempotent(String paymentId, PaymentDetails paymentDetails) {
        long startTime = System.currentTimeMillis();
        
        var existingRecord = idempotencyService.getExistingRecord(paymentId);
        
        if (existingRecord.isPresent()) {
            long duration = System.currentTimeMillis() - startTime;
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
        
        boolean success = processPayment(paymentDetails);
        String message = success ? "Payment processed successfully" : "Payment validation failed";
        long duration = System.currentTimeMillis() - startTime;
        
        idempotencyService.storeRecord(paymentId, paymentDetails, success, message);
        
        auditService.logAudit(
                paymentId,
                AuditOperationType.IDEMPOTENT_PAYMENT,
                success ? AuditStatus.SUCCESS : AuditStatus.VALIDATION_ERROR,
                paymentDetails,
                message,
                duration,
                false
        );
        
        return idempotencyService.getExistingRecord(paymentId)
                .orElseThrow(() -> new IllegalStateException("Failed to store idempotency record"));
    }
    
    /**
     * Validates payment details using configured amount limits.
     *
     * @param paymentDetails Payment details to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePaymentDetails(PaymentDetails paymentDetails) {
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
        
        // Basic validation
        if (paymentDetails.getAmount() == null || paymentDetails.getAmount().doubleValue() <= 0) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logAudit(
                    paymentDetails.getPaymentId(),
                    AuditOperationType.PAYMENT_VALIDATE,
                    AuditStatus.VALIDATION_ERROR,
                    paymentDetails,
                    "Invalid payment amount",
                    duration,
                    false
            );
            return false;
        }
        
        if (paymentDetails.getCardNumber() == null || paymentDetails.getCardNumber().length() < 13) {
            return false;
        }
        
        if (paymentDetails.getSecurityCode() == null || paymentDetails.getSecurityCode().length() != 3) {
            return false;
        }
        
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
        
        return true;
    }
    
    /**
     * Processes a refund using configured gateway.
     *
     * @param amount Amount to refund
     * @return true if successful, false otherwise
     */
    public boolean refundPayment(BigDecimal amount) {
        long startTime = System.currentTimeMillis();
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            long duration = System.currentTimeMillis() - startTime;
            auditService.logAudit(
                    null,
                    AuditOperationType.PAYMENT_REFUND,
                    AuditStatus.VALIDATION_ERROR,
                    null,
                    "Invalid refund amount",
                    duration,
                    false
            );
            return false;
        }
        
        // Use grouped configuration from PaymentGatewayConfig
        System.out.println("Processing refund via gateway: " + gatewayConfig.endpoint);
        
        boolean success = true;
        long duration = System.currentTimeMillis() - startTime;
        
        auditService.logAudit(
                null,
                AuditOperationType.PAYMENT_REFUND,
                success ? AuditStatus.SUCCESS : AuditStatus.FAILED,
                null,
                "Refund processed for amount: " + amount,
                duration,
                false
        );
        
        return success;
    }
    
    /**
     * Gets current payment gateway configuration for demonstration.
     * Shows how to access @ConfigProperties values.
     *
     * @return Configuration summary
     */
    public String getGatewayConfiguration() {
        return String.format(
                "Gateway: %s, Timeout: %dms, Sandbox Mode: %s, Retry Attempts: %d",
                gatewayConfig.endpoint, 
                gatewayConfig.timeout, 
                gatewayConfig.sandboxMode,
                gatewayConfig.retryAttempts
        );
    }
}
