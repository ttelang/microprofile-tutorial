package io.microprofile.tutorial.store.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Idempotency record for storing payment request/response pairs.
 * Ensures that repeated payment requests with the same ID are handled idempotently.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyRecord {
    private String paymentId;
    private PaymentDetails requestDetails;
    private boolean success;
    private String responseMessage;
    private LocalDateTime timestamp;
    private LocalDateTime expiresAt;
}
