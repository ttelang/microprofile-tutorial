package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PaymentService with 100% code coverage.
 * Tests all methods and all code branches.
 */
class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Nested
    @DisplayName("processPayment() tests")
    class ProcessPaymentTests {

        @Test
        @DisplayName("should return true when payment details are valid")
        void testProcessPayment_ValidDetails_ReturnsTrue() {
            // Given
            PaymentDetails validDetails = createValidPaymentDetails();

            // When
            boolean result = paymentService.processPayment(validDetails);

            // Then
            assertTrue(result, "Valid payment details should be processed successfully");
        }

        @Test
        @DisplayName("should return false when payment details are invalid")
        void testProcessPayment_InvalidDetails_ReturnsFalse() {
            // Given
            PaymentDetails invalidDetails = new PaymentDetails();
            invalidDetails.setCardNumber(null); // Invalid

            // When
            boolean result = paymentService.processPayment(invalidDetails);

            // Then
            assertFalse(result, "Invalid payment details should not be processed");
        }
    }

    @Nested
    @DisplayName("validatePaymentDetails() tests")
    class ValidatePaymentDetailsTests {

        @Test
        @DisplayName("should return false when paymentDetails is null")
        void testValidatePaymentDetails_NullPaymentDetails_ReturnsFalse() {
            // When
            boolean result = paymentService.validatePaymentDetails(null);

            // Then
            assertFalse(result, "Null payment details should be invalid");
        }

        @Test
        @DisplayName("should return false when cardNumber is null")
        void testValidatePaymentDetails_NullCardNumber_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setCardNumber(null);

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Null card number should be invalid");
        }

        @Test
        @DisplayName("should return false when cardNumber is empty")
        void testValidatePaymentDetails_EmptyCardNumber_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setCardNumber("");

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Empty card number should be invalid");
        }

        @Test
        @DisplayName("should return false when cardNumber contains only whitespace")
        void testValidatePaymentDetails_WhitespaceCardNumber_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setCardNumber("   ");

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Whitespace-only card number should be invalid");
        }

        @Test
        @DisplayName("should return false when cardHolderName is null")
        void testValidatePaymentDetails_NullCardHolderName_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setCardHolderName(null);

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Null cardholder name should be invalid");
        }

        @Test
        @DisplayName("should return false when cardHolderName is empty")
        void testValidatePaymentDetails_EmptyCardHolderName_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setCardHolderName("");

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Empty cardholder name should be invalid");
        }

        @Test
        @DisplayName("should return false when cardHolderName contains only whitespace")
        void testValidatePaymentDetails_WhitespaceCardHolderName_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setCardHolderName("   ");

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Whitespace-only cardholder name should be invalid");
        }

        @Test
        @DisplayName("should return false when amount is null")
        void testValidatePaymentDetails_NullAmount_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setAmount(null);

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Null amount should be invalid");
        }

        @Test
        @DisplayName("should return false when amount is zero")
        void testValidatePaymentDetails_ZeroAmount_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setAmount(BigDecimal.ZERO);

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Zero amount should be invalid");
        }

        @Test
        @DisplayName("should return false when amount is negative")
        void testValidatePaymentDetails_NegativeAmount_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setAmount(new BigDecimal("-10.00"));

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Negative amount should be invalid");
        }

        @Test
        @DisplayName("should return false when expiryDate is null")
        void testValidatePaymentDetails_NullExpiryDate_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setExpiryDate(null);

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Null expiry date should be invalid");
        }

        @Test
        @DisplayName("should return false when expiryDate is empty")
        void testValidatePaymentDetails_EmptyExpiryDate_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setExpiryDate("");

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Empty expiry date should be invalid");
        }

        @Test
        @DisplayName("should return false when expiryDate contains only whitespace")
        void testValidatePaymentDetails_WhitespaceExpiryDate_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setExpiryDate("   ");

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Whitespace-only expiry date should be invalid");
        }

        @Test
        @DisplayName("should return false when securityCode is null")
        void testValidatePaymentDetails_NullSecurityCode_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setSecurityCode(null);

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Null security code should be invalid");
        }

        @Test
        @DisplayName("should return false when securityCode is empty")
        void testValidatePaymentDetails_EmptySecurityCode_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setSecurityCode("");

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Empty security code should be invalid");
        }

        @Test
        @DisplayName("should return false when securityCode contains only whitespace")
        void testValidatePaymentDetails_WhitespaceSecurityCode_ReturnsFalse() {
            // Given
            PaymentDetails details = createValidPaymentDetails();
            details.setSecurityCode("   ");

            // When
            boolean result = paymentService.validatePaymentDetails(details);

            // Then
            assertFalse(result, "Whitespace-only security code should be invalid");
        }

        @Test
        @DisplayName("should return true when all payment details are valid")
        void testValidatePaymentDetails_AllFieldsValid_ReturnsTrue() {
            // Given
            PaymentDetails validDetails = createValidPaymentDetails();

            // When
            boolean result = paymentService.validatePaymentDetails(validDetails);

            // Then
            assertTrue(result, "Valid payment details should pass validation");
        }
    }

    @Nested
    @DisplayName("refundPayment() tests")
    class RefundPaymentTests {

        @Test
        @DisplayName("should return false when amount is null")
        void testRefundPayment_NullAmount_ReturnsFalse() {
            // When
            boolean result = paymentService.refundPayment(null);

            // Then
            assertFalse(result, "Null refund amount should be invalid");
        }

        @Test
        @DisplayName("should return false when amount is zero")
        void testRefundPayment_ZeroAmount_ReturnsFalse() {
            // When
            boolean result = paymentService.refundPayment(BigDecimal.ZERO);

            // Then
            assertFalse(result, "Zero refund amount should be invalid");
        }

        @Test
        @DisplayName("should return false when amount is negative")
        void testRefundPayment_NegativeAmount_ReturnsFalse() {
            // When
            boolean result = paymentService.refundPayment(new BigDecimal("-50.00"));

            // Then
            assertFalse(result, "Negative refund amount should be invalid");
        }

        @Test
        @DisplayName("should return true when amount is positive")
        void testRefundPayment_PositiveAmount_ReturnsTrue() {
            // When
            boolean result = paymentService.refundPayment(new BigDecimal("100.00"));

            // Then
            assertTrue(result, "Positive refund amount should be processed successfully");
        }

        @Test
        @DisplayName("should return true when amount is a small positive value")
        void testRefundPayment_SmallPositiveAmount_ReturnsTrue() {
            // When
            boolean result = paymentService.refundPayment(new BigDecimal("0.01"));

            // Then
            assertTrue(result, "Small positive refund amount should be processed successfully");
        }
    }

    /**
     * Helper method to create valid payment details for testing.
     */
    private PaymentDetails createValidPaymentDetails() {
        PaymentDetails details = new PaymentDetails();
        details.setCardNumber("4111111111111111");
        details.setCardHolderName("John Doe");
        details.setExpiryDate("12/25");
        details.setSecurityCode("123");
        details.setAmount(new BigDecimal("99.99"));
        return details;
    }
}
