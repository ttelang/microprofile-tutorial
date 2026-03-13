package io.microprofile.tutorial.store.payment.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description = "Payment details for processing credit card transactions",
    example = """
        {
          "paymentId": "payment-550e8400-e29b-41d4-a716-446655440000",
          "cardNumber": "4111111111111111",
          "cardHolderName": "John Doe",
          "expiryDate": "12/25",
          "securityCode": "123",
          "amount": 99.99
        }
        """
)
public class PaymentDetails {
    
    @Schema(
        description = "Unique identifier for idempotency support (optional for POST, required for PUT)",
        example = "payment-550e8400-e29b-41d4-a716-446655440000",
        nullable = true,
        pattern = "^[a-zA-Z0-9-_]+$",
        minLength = 5,
        maxLength = 100
    )
    private String paymentId;
    
    @Schema(
        description = "Credit card number (13-19 digits). Will be masked in audit logs for PCI-DSS compliance.",
        example = "4111111111111111",
        required = true,
        pattern = "^[0-9]{13,19}$",
        minLength = 13,
        maxLength = 19
    )
    private String cardNumber;
    
    @Schema(
        description = "Name of the card holder as it appears on the card",
        example = "John Doe",
        required = true,
        minLength = 2,
        maxLength = 100
    )
    private String cardHolderName;
    
    @Schema(
        description = "Card expiry date in MM/YY format",
        example = "12/25",
        required = true,
        pattern = "^(0[1-9]|1[0-2])/[0-9]{2}$"
    )
    private String expiryDate;
    
    @Schema(
        description = "Card security code (CVV/CVC) - 3 or 4 digits",
        example = "123",
        required = true,
        pattern = "^[0-9]{3,4}$",
        minLength = 3,
        maxLength = 4
    )
    private String securityCode;
    
    @Schema(
        description = "Payment amount in USD. Must be greater than $0.00 (exclusive).",
        example = "99.99",
        required = true,
        exclusiveMinimum = true,
        minimum = "0.00",
        format = "double",
        multipleOf = 0.01
    )
    private java.math.BigDecimal amount;
}