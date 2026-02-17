package io.microprofile.tutorial.store.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetails {
    private String paymentId; // Unique identifier for idempotency support
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate; // Format MM/YY
    private String securityCode;
    private java.math.BigDecimal amount;
}