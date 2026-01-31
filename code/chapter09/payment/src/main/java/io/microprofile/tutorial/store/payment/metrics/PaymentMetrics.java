package io.microprofile.tutorial.store.payment.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.Meter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.annotation.PostConstruct;

import java.util.logging.Logger;

/**
 * Metrics collection for Payment Service using MicroProfile Telemetry 2.1.
 * Demonstrates Counter and Histogram instruments from OpenTelemetry Metrics API.
 */
@ApplicationScoped
public class PaymentMetrics {
    
    private static final Logger logger = Logger.getLogger(PaymentMetrics.class.getName());
    
    @Inject
    Meter meter;
    
    private LongCounter paymentCounter;
    private LongCounter paymentSuccessCounter;
    private LongCounter paymentFailureCounter;
    private DoubleHistogram paymentAmountHistogram;
    private DoubleHistogram processingDurationHistogram;
    
    @PostConstruct
    public void init() {
        // Counter for total payment attempts
        paymentCounter = meter.counterBuilder("payment.attempts.total")
            .setDescription("Total number of payment attempts")
            .setUnit("payments")
            .build();
        
        // Counter for successful payments
        paymentSuccessCounter = meter.counterBuilder("payment.success.total")
            .setDescription("Total number of successful payments")
            .setUnit("payments")
            .build();
        
        // Counter for failed payments
        paymentFailureCounter = meter.counterBuilder("payment.failure.total")
            .setDescription("Total number of failed payments")
            .setUnit("payments")
            .build();
        
        // Histogram for payment amounts
        paymentAmountHistogram = meter.histogramBuilder("payment.amount")
            .setDescription("Distribution of payment amounts")
            .setUnit("USD")
            .build();
        
        // Histogram for processing duration
        processingDurationHistogram = meter.histogramBuilder("payment.processing.duration")
            .setDescription("Payment processing duration in milliseconds")
            .setUnit("ms")
            .build();
        
        logger.info("Payment metrics initialized successfully");
    }
    
    /**
     * Records a payment attempt with the specified amount and payment method.
     * 
     * @param amount Payment amount
     * @param paymentMethod Payment method (e.g., "credit_card", "debit_card")
     */
    public void recordPaymentAttempt(double amount, String paymentMethod) {
        paymentCounter.add(1, 
            Attributes.builder()
                .put("payment.method", paymentMethod)
                .build());
        
        paymentAmountHistogram.record(amount,
            Attributes.builder()
                .put("payment.method", paymentMethod)
                .build());
    }
    
    /**
     * Records a successful payment.
     * 
     * @param amount Payment amount
     * @param paymentMethod Payment method
     * @param durationMs Processing duration in milliseconds
     */
    public void recordPaymentSuccess(double amount, String paymentMethod, long durationMs) {
        paymentSuccessCounter.add(1,
            Attributes.builder()
                .put("payment.method", paymentMethod)
                .build());
        
        processingDurationHistogram.record(durationMs,
            Attributes.builder()
                .put("payment.method", paymentMethod)
                .put("payment.status", "success")
                .build());
        
        logger.fine(() -> String.format("Recorded successful payment: amount=%.2f, method=%s, duration=%dms", 
            amount, paymentMethod, durationMs));
    }
    
    /**
     * Records a failed payment.
     * 
     * @param amount Payment amount
     * @param paymentMethod Payment method
     * @param failureReason Reason for failure
     * @param durationMs Processing duration in milliseconds
     */
    public void recordPaymentFailure(double amount, String paymentMethod, String failureReason, long durationMs) {
        paymentFailureCounter.add(1,
            Attributes.builder()
                .put("payment.method", paymentMethod)
                .put("failure.reason", failureReason)
                .build());
        
        processingDurationHistogram.record(durationMs,
            Attributes.builder()
                .put("payment.method", paymentMethod)
                .put("payment.status", "failed")
                .put("failure.reason", failureReason)
                .build());
        
        logger.warning(() -> String.format("Recorded failed payment: amount=%.2f, method=%s, reason=%s, duration=%dms", 
            amount, paymentMethod, failureReason, durationMs));
    }
}
