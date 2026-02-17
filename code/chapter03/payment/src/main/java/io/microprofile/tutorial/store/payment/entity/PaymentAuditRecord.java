package io.microprofile.tutorial.store.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing an audit trail record for payment transactions.
 * Captures all payment-related operations for compliance and troubleshooting.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentAuditRecord {
    
    /**
     * Unique audit record identifier
     */
    private String auditId;
    
    /**
     * Payment transaction identifier (if applicable)
     */
    private String paymentId;
    
    /**
     * Type of operation performed
     */
    private AuditOperationType operationType;
    
    /**
     * Timestamp when the operation occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * Operation status (SUCCESS, FAILED, VALIDATION_ERROR, etc.)
     */
    private AuditStatus status;
    
    /**
     * Card number (masked for security - last 4 digits only)
     */
    private String maskedCardNumber;
    
    /**
     * Cardholder name
     */
    private String cardHolderName;
    
    /**
     * Transaction amount
     */
    private BigDecimal amount;
    
    /**
     * Detailed message about the operation
     */
    private String message;
    
    /**
     * User/system that initiated the operation
     */
    private String initiatedBy;
    
    /**
     * IP address or client identifier
     */
    private String clientIdentifier;
    
    /**
     * Duration of the operation in milliseconds
     */
    private Long durationMs;
    
    /**
     * Whether this was a cached/idempotent response
     */
    private Boolean cachedResponse;
    
    /**
     * Additional metadata or context
     */
    private String metadata;
    
    /**
     * Enum for audit operation types
     */
    public enum AuditOperationType {
        PAYMENT_PROCESS,
        PAYMENT_VALIDATE,
        PAYMENT_REFUND,
        IDEMPOTENT_PAYMENT,
        IDEMPOTENT_CACHE_HIT
    }
    
    /**
     * Enum for audit status
     */
    public enum AuditStatus {
        SUCCESS,
        FAILED,
        VALIDATION_ERROR,
        CONFLICT,
        SYSTEM_ERROR
    }
    
    /**
     * Helper method to mask card number for security
     * Shows only last 4 digits: **** **** **** 1234
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }
}
