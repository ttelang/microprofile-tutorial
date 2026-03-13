package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.IdempotencyRecord;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.interceptor.Logged;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing idempotency of payment operations.
 * Uses an in-memory cache to store processed payment requests.
 * Ensures that duplicate payment requests with the same ID are handled safely.
 */
@ApplicationScoped
@Logged
public class IdempotencyService {
    
    // Thread-safe in-memory cache for idempotency records
    private final Map<String, IdempotencyRecord> idempotencyCache = new ConcurrentHashMap<>();
    
    // Default TTL: 24 hours
    private static final long EXPIRY_HOURS = 24;
    
    /**
     * Check if a payment with the given ID has already been processed.
     *
     * @param paymentId Unique payment identifier
     * @return Optional containing the existing record if found and not expired
     */
    public Optional<IdempotencyRecord> getExistingRecord(String paymentId) {
        IdempotencyRecord record = idempotencyCache.get(paymentId);
        
        if (record == null) {
            return Optional.empty();
        }
        
        // Check if record has expired
        if (LocalDateTime.now().isAfter(record.getExpiresAt())) {
            idempotencyCache.remove(paymentId);
            return Optional.empty();
        }
        
        return Optional.of(record);
    }
    
    /**
     * Store a payment processing result for idempotency.
     *
     * @param paymentId Unique payment identifier
     * @param requestDetails Original payment request details
     * @param success Whether payment was successful
     * @param responseMessage Response message
     */
    public void storeRecord(String paymentId, PaymentDetails requestDetails, 
                           boolean success, String responseMessage) {
        LocalDateTime now = LocalDateTime.now();
        IdempotencyRecord record = new IdempotencyRecord(
            paymentId,
            requestDetails,
            success,
            responseMessage,
            now,
            now.plusHours(EXPIRY_HOURS)
        );
        
        idempotencyCache.put(paymentId, record);
    }
    
    /**
     * Check if the incoming payment details match the stored record.
     * This prevents using the same payment ID for different payment details.
     *
     * @param record Existing idempotency record
     * @param newDetails New payment details to compare
     * @return true if details match, false otherwise
     */
    public boolean requestDetailsMatch(IdempotencyRecord record, PaymentDetails newDetails) {
        PaymentDetails storedDetails = record.getRequestDetails();
        
        if (storedDetails == null || newDetails == null) {
            return storedDetails == newDetails;
        }
        
        // Compare relevant payment fields
        return equals(storedDetails.getCardNumber(), newDetails.getCardNumber()) &&
               equals(storedDetails.getCardHolderName(), newDetails.getCardHolderName()) &&
               equals(storedDetails.getExpiryDate(), newDetails.getExpiryDate()) &&
               equals(storedDetails.getSecurityCode(), newDetails.getSecurityCode()) &&
               (storedDetails.getAmount() != null && 
                storedDetails.getAmount().compareTo(newDetails.getAmount()) == 0);
    }
    
    /**
     * Helper method for null-safe string comparison.
     */
    private boolean equals(String s1, String s2) {
        return s1 != null ? s1.equals(s2) : s2 == null;
    }
    
    /**
     * Clear expired records from cache (for maintenance).
     * In production, this would be called by a scheduled task.
     */
    public void clearExpiredRecords() {
        LocalDateTime now = LocalDateTime.now();
        idempotencyCache.entrySet().removeIf(
            entry -> now.isAfter(entry.getValue().getExpiresAt())
        );
    }
    
    /**
     * Get the number of records in cache (for monitoring/testing).
     */
    public int getCacheSize() {
        return idempotencyCache.size();
    }
}
