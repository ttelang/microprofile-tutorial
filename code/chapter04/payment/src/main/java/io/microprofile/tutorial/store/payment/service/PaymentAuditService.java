package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditOperationType;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditStatus;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service for managing payment audit trails.
 * Provides comprehensive logging of all payment operations for compliance and debugging.
 */
@ApplicationScoped
public class PaymentAuditService {
    
    private static final Logger AUDIT_LOGGER = Logger.getLogger("PaymentAudit");
    
    // In-memory storage for audit records (in production, use a database)
    private final ConcurrentHashMap<String, PaymentAuditRecord> auditRecords = new ConcurrentHashMap<>();
    
    /**
     * Log a payment operation to the audit trail
     * 
     * @param paymentId Payment transaction ID
     * @param operationType Type of operation
     * @param status Operation status
     * @param paymentDetails Payment details (card will be masked)
     * @param message Descriptive message
     * @param durationMs Operation duration in milliseconds
     * @param cachedResponse Whether this was a cached response
     * @return The created audit record
     */
    public PaymentAuditRecord logAudit(
            String paymentId,
            AuditOperationType operationType,
            AuditStatus status,
            PaymentDetails paymentDetails,
            String message,
            Long durationMs,
            Boolean cachedResponse) {
        
        String auditId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        
        // Mask sensitive card information
        String maskedCard = paymentDetails != null && paymentDetails.getCardNumber() != null
                ? PaymentAuditRecord.maskCardNumber(paymentDetails.getCardNumber())
                : "N/A";
        
        String cardHolderName = paymentDetails != null ? paymentDetails.getCardHolderName() : "N/A";
        BigDecimal amount = paymentDetails != null ? paymentDetails.getAmount() : BigDecimal.ZERO;
        
        PaymentAuditRecord auditRecord = new PaymentAuditRecord();
        auditRecord.setAuditId(auditId);
        auditRecord.setPaymentId(paymentId);
        auditRecord.setOperationType(operationType);
        auditRecord.setTimestamp(timestamp);
        auditRecord.setStatus(status);
        auditRecord.setMaskedCardNumber(maskedCard);
        auditRecord.setCardHolderName(cardHolderName);
        auditRecord.setAmount(amount);
        auditRecord.setMessage(message);
        auditRecord.setInitiatedBy("SYSTEM"); // Could be enhanced with actual user context
        auditRecord.setClientIdentifier("localhost"); // Could be enhanced with actual client IP
        auditRecord.setDurationMs(durationMs);
        auditRecord.setCachedResponse(cachedResponse != null ? cachedResponse : false);
        
        // Store the audit record
        auditRecords.put(auditId, auditRecord);
        
        // Log to audit logger
        AUDIT_LOGGER.log(Level.INFO, 
                "AUDIT: [{0}] {1} - PaymentId: {2}, Status: {3}, Card: {4}, Amount: {5}, Duration: {6}ms, Message: {7}",
                new Object[]{
                        timestamp,
                        operationType,
                        paymentId != null ? paymentId : "N/A",
                        status,
                        maskedCard,
                        amount,
                        durationMs,
                        message
                });
        
        return auditRecord;
    }
    
    /**
     * Log a refund operation to the audit trail
     * 
     * @param operationType Type of operation
     * @param status Operation status
     * @param amount Refund amount
     * @param message Descriptive message
     * @param durationMs Operation duration in milliseconds
     * @return The created audit record
     */
    public PaymentAuditRecord logRefundAudit(
            AuditOperationType operationType,
            AuditStatus status,
            BigDecimal amount,
            String message,
            Long durationMs) {
        
        String auditId = UUID.randomUUID().toString();
        LocalDateTime timestamp = LocalDateTime.now();
        
        PaymentAuditRecord auditRecord = new PaymentAuditRecord();
        auditRecord.setAuditId(auditId);
        auditRecord.setPaymentId(null);
        auditRecord.setOperationType(operationType);
        auditRecord.setTimestamp(timestamp);
        auditRecord.setStatus(status);
        auditRecord.setMaskedCardNumber("N/A");
        auditRecord.setCardHolderName("N/A");
        auditRecord.setAmount(amount);
        auditRecord.setMessage(message);
        auditRecord.setInitiatedBy("SYSTEM");
        auditRecord.setClientIdentifier("localhost");
        auditRecord.setDurationMs(durationMs);
        auditRecord.setCachedResponse(false);
        
        // Store the audit record
        auditRecords.put(auditId, auditRecord);
        
        // Log to audit logger
        AUDIT_LOGGER.log(Level.INFO, 
                "AUDIT: [{0}] {1} - Status: {2}, Amount: {3}, Duration: {4}ms, Message: {5}",
                new Object[]{
                        timestamp,
                        operationType,
                        status,
                        amount,
                        durationMs,
                        message
                });
        
        return auditRecord;
    }
    
    /**
     * Retrieve all audit records
     * 
     * @return List of all audit records ordered by timestamp (newest first)
     */
    public List<PaymentAuditRecord> getAllAuditRecords() {
        List<PaymentAuditRecord> records = new ArrayList<>(auditRecords.values());
        records.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp())); // Newest first
        return records;
    }
    
    /**
     * Retrieve audit records for a specific payment ID
     * 
     * @param paymentId Payment transaction ID
     * @return List of audit records for the payment
     */
    public List<PaymentAuditRecord> getAuditRecordsByPaymentId(String paymentId) {
        if (paymentId == null) {
            return Collections.emptyList();
        }
        
        return auditRecords.values().stream()
                .filter(record -> paymentId.equals(record.getPaymentId()))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieve audit records by operation type
     * 
     * @param operationType Type of operation
     * @return List of audit records for the operation type
     */
    public List<PaymentAuditRecord> getAuditRecordsByOperationType(AuditOperationType operationType) {
        if (operationType == null) {
            return Collections.emptyList();
        }
        
        return auditRecords.values().stream()
                .filter(record -> operationType == record.getOperationType())
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieve audit records by status
     * 
     * @param status Audit status
     * @return List of audit records with the specified status
     */
    public List<PaymentAuditRecord> getAuditRecordsByStatus(AuditStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        
        return auditRecords.values().stream()
                .filter(record -> status == record.getStatus())
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }
    
    /**
     * Get total count of audit records
     * 
     * @return Total number of audit records
     */
    public int getAuditRecordCount() {
        return auditRecords.size();
    }
    
    /**
     * Clear all audit records (for testing purposes only)
     */
    public void clearAuditRecords() {
        auditRecords.clear();
        AUDIT_LOGGER.log(Level.WARNING, "All audit records have been cleared");
    }
}
