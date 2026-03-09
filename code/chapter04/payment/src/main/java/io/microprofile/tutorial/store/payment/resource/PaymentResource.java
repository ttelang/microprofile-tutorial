package io.microprofile.tutorial.store.payment.resource;

import io.microprofile.tutorial.store.payment.entity.IdempotencyRecord;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.interceptor.Logged;
import io.microprofile.tutorial.store.payment.service.IdempotencyService;
import io.microprofile.tutorial.store.payment.service.PaymentService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;

/**
 * REST Resource for Payment operations.
 * 
 * This resource demonstrates CDI interceptors with @Logged annotation.
 * The interceptor automatically logs all method calls, parameters, and results.
 * No manual logging code is needed!
 */
@ApplicationScoped
@Logged  // Automatic logging for all REST endpoints
@Path("/payments")
public class PaymentResource {

    @Inject
    private PaymentService paymentService;
    
    @Inject
    private IdempotencyService idempotencyService;

    /**
     * Process a payment.
     *
     * @param paymentDetails Payment details
     * @return Response with payment status
     */
    @POST
    @Path("/process")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processPayment(PaymentDetails paymentDetails) {
        // Interceptor logs entry with paymentDetails parameter
        
        boolean success = paymentService.processPayment(paymentDetails);
        
        if (success) {
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"status\": \"success\", \"message\": \"Payment processed successfully\"}")
                    .build();
        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\": \"failed\", \"message\": \"Payment validation failed\"}")
                    .build();
        }
        // Interceptor logs exit with response and execution time
    }
    
    /**
     * Process a payment with idempotency support using PUT method.
     * 
     * The PUT method is inherently idempotent according to HTTP specification.
     * Multiple identical PUT requests will have the same effect as a single request.
     * 
     * The client provides a unique payment ID in the URL path to identify the payment transaction.
     * If a payment with the same ID is submitted again, the cached result is returned
     * without reprocessing the payment.
     * 
     * Example usage:
     *   PUT /payments/{paymentId}
     *   Body: { "cardNumber": "1234...", "amount": 100.00, ... }
     *
     * @param paymentId Unique payment identifier (UUID/GUID recommended)
     * @param paymentDetails Payment details (excluding paymentId which is in path)
     * @return Response with payment status (200 OK for success or cached result, 
     *         409 Conflict if same ID used with different details,
     *         400 Bad Request for validation failures)
     */
    @PUT
    @Path("/{paymentId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response processPaymentIdempotent(
            @PathParam("paymentId") String paymentId,
            PaymentDetails paymentDetails) {
        
        // Validate payment ID is provided
        if (paymentId == null || paymentId.trim().isEmpty()) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\": \"failed\", \"message\": \"Payment ID is required\"}")
                    .build();
        }
        
        // Set the payment ID in the details object for consistency
        paymentDetails.setPaymentId(paymentId);
        
        // Check if this payment was already processed
        var existingRecord = idempotencyService.getExistingRecord(paymentId);
        
        if (existingRecord.isPresent()) {
            IdempotencyRecord record = existingRecord.get();
            
            // Verify that the request details match the original request
            if (!idempotencyService.requestDetailsMatch(record, paymentDetails)) {
                // Same payment ID used with different payment details - this is an error
                return Response
                        .status(Response.Status.CONFLICT)
                        .entity("{\"status\": \"conflict\", " +
                                "\"message\": \"Payment ID already used with different payment details\"}")
                        .build();
            }
            
            // Return the cached result (idempotent behavior)
            String status = record.isSuccess() ? "success" : "failed";
            return Response
                    .status(record.isSuccess() ? Response.Status.OK : Response.Status.BAD_REQUEST)
                    .entity(String.format("{\"status\": \"%s\", \"message\": \"%s\", \"cached\": true}",
                            status, record.getResponseMessage()))
                    .build();
        }
        
        // Process the payment for the first time
        IdempotencyRecord record = paymentService.processPaymentIdempotent(paymentId, paymentDetails);
        
        return Response
                .status(record.isSuccess() ? Response.Status.OK : Response.Status.BAD_REQUEST)
                .entity(String.format("{\"status\": \"%s\", \"message\": \"%s\", \"cached\": false}",
                        record.isSuccess() ? "success" : "failed",
                        record.getResponseMessage()))
                .build();
    }
    
    /**
     * Validate payment details without processing.
     *
     * @param paymentDetails Payment details to validate
     * @return Response with validation status
     */
    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validatePayment(PaymentDetails paymentDetails) {
        // Interceptor handles logging automatically
        
        boolean isValid = paymentService.validatePaymentDetails(paymentDetails);
        
        if (isValid) {
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"valid\": true, \"message\": \"Payment details are valid\"}")
                    .build();
        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"valid\": false, \"message\": \"Payment details are invalid\"}")
                    .build();
        }
    }
    
    /**
     * Process a refund.
     *
     * @param amount Amount to refund
     * @return Response with refund status
     */
    @POST
    @Path("/refund")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response refundPayment(@QueryParam("amount") BigDecimal amount) {
        // Interceptor logs the refund request automatically
        
        boolean success = paymentService.refundPayment(amount);
        
        if (success) {
            return Response
                    .status(Response.Status.OK)
                    .entity("{\"status\": \"success\", \"message\": \"Refund processed successfully\"}")
                    .build();
        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\": \"failed\", \"message\": \"Invalid refund amount\"}")
                    .build();
        }
    }
}
