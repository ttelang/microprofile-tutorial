package io.microprofile.tutorial.store.payment.resource;

import io.microprofile.tutorial.store.payment.entity.IdempotencyRecord;
import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.interceptor.Logged;
import io.microprofile.tutorial.store.payment.service.IdempotencyService;
import io.microprofile.tutorial.store.payment.service.PaymentService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

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
@Tag(name = "Payments", description = "Payment processing operations")
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
    @Operation(
        summary = "Process a payment",
        description = """
            Process a payment transaction with the provided card details.
            
            **Note:** This endpoint is NOT idempotent. Multiple identical requests will create multiple charges.
            For idempotent payment processing, use `PUT /payments/{paymentId}` instead.
            
            **Automatic Logging:** This method is logged automatically via CDI @Logged interceptor.
            """
    )
    @RequestBody(
        description = "Payment details including card information and amount",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = PaymentDetails.class),
            examples = {
                @ExampleObject(
                    name = "Valid Payment",
                    summary = "Valid payment with all required fields",
                    value = """
                        {
                          "cardNumber": "4111111111111111",
                          "cardHolderName": "John Doe",
                          "expiryDate": "12/25",
                          "securityCode": "123",
                          "amount": 99.99
                        }
                        """
                ),
                @ExampleObject(
                    name = "Large Payment",
                    summary = "Payment with larger amount",
                    value = """
                        {
                          "cardNumber": "5500000000000004",
                          "cardHolderName": "Jane Smith",
                          "expiryDate": "06/26",
                          "securityCode": "456",
                          "amount": 1299.99
                        }
                        """
                )
            }
        )
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Payment processed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "success",
                          "message": "Payment processed successfully"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid payment details or validation failed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "failed",
                          "message": "Payment validation failed"
                        }
                        """
                )
            )
        )
    })
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
    @Operation(
        summary = "Process idempotent payment",
        description = """
            Process a payment with idempotency guarantee using HTTP PUT semantics.
            
            ### Idempotency Behavior
            - **Same Payment ID + Same Details = Same Result** (cached, no reprocessing)
            - **Same Payment ID + Different Details = 409 Conflict** (prevents accidental duplicates)
            - **New Payment ID = New Payment Processing**
            
            ### Cache Duration
            Idempotency records are cached for **24 hours**. After expiration, the same payment ID
            can be reused for a new payment.
            
            ### Best Practices
            - Use UUID or GUID for payment IDs (e.g., `payment-550e8400-e29b-41d4-a716-446655440000`)
            - Store payment ID on client side before making request
            - Retry the same request with same ID on network failures
            - Check `cached: true` in response to detect retry scenarios
            
            **Automatic Logging:** This method is logged automatically via CDI @Logged interceptor.
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Payment processed successfully (or cached result from previous request)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = String.class),
                examples = {
                    @ExampleObject(
                        name = "New Payment Success",
                        summary = "Payment processed for the first time",
                        value = """
                            {
                              "status": "success",
                              "message": "Payment processed successfully",
                              "cached": false
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Cached Payment Success",
                        summary = "Payment result returned from cache (idempotent retry)",
                        value = """
                            {
                              "status": "success",
                              "message": "Payment processed successfully",
                              "cached": true
                            }
                            """
                    )
                }
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Payment ID missing or payment validation failed",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = {
                    @ExampleObject(
                        name = "Missing Payment ID",
                        value = """
                            {
                              "status": "failed",
                              "message": "Payment ID is required"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Validation Failed",
                        value = """
                            {
                              "status": "failed",
                              "message": "Payment validation failed",
                              "cached": false
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "Cached Failure",
                        value = """
                            {
                              "status": "failed",
                              "message": "Invalid card number",
                              "cached": true
                            }
                            """
                    )
                }
            )
        ),
        @APIResponse(
            responseCode = "409",
            description = "Conflict: Same payment ID used with different payment details",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "conflict",
                          "message": "Payment ID already used with different payment details"
                        }
                        """
                )
            )
        )
    })
    public Response processPaymentIdempotent(
            @Parameter(
                description = "Unique payment identifier (UUID recommended). Must be provided by client to enable idempotency.",
                required = true,
                schema = @Schema(
                    type = SchemaType.STRING,
                    example = "payment-550e8400-e29b-41d4-a716-446655440000",
                    pattern = "^[a-zA-Z0-9-_]+$",
                    minLength = 5,
                    maxLength = 100
                )
            )
            @PathParam("paymentId") String paymentId,
            @RequestBody(
                description = "Payment details (paymentId will be set from path parameter)",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PaymentDetails.class),
                    examples = @ExampleObject(
                        name = "Idempotent Payment",
                        value = """
                            {
                              "cardNumber": "4111111111111111",
                              "cardHolderName": "John Doe",
                              "expiryDate": "12/25",
                              "securityCode": "123",
                              "amount": 99.99
                            }
                            """
                    )
                )
            )
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
    @Operation(
        summary = "Validate payment details",
        description = """
            Validates payment details without processing the actual payment.
            
            This endpoint is useful for:
            - Client-side validation before submitting full payment
            - Testing card details before checkout
            - Pre-flight checks in payment forms
            
            **Automatic Logging:** This method is logged automatically via CDI @Logged interceptor.
            """
    )
    @RequestBody(
        description = "Payment details to validate",
        required = true,
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = PaymentDetails.class),
            examples = {
                @ExampleObject(
                    name = "Valid Payment Details",
                    value = """
                        {
                          "cardNumber": "4111111111111111",
                          "cardHolderName": "John Doe",
                          "expiryDate": "12/25",
                          "securityCode": "123",
                          "amount": 99.99
                        }
                        """
                ),
                @ExampleObject(
                    name = "Invalid Payment Details",
                    description = "Missing required fields",
                    value = """
                        {
                          "cardNumber": null,
                          "cardHolderName": "Jane Doe",
                          "amount": 50.00
                        }
                        """
                )
            }
        )
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Payment details are valid",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "valid": true,
                          "message": "Payment details are valid"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Payment details are invalid",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "valid": false,
                          "message": "Payment details are invalid"
                        }
                        """
                )
            )
        )
    })
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
    @Operation(
        summary = "Process a refund",
        description = """
            Process a refund for a previous payment.
            
            **Note:** This is a simplified refund implementation for demonstration purposes.
            Production systems would require payment ID reference and additional validation.
            
            **Automatic Logging:** This method is logged automatically via CDI @Logged interceptor.
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Refund processed successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "success",
                          "message": "Refund processed successfully"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid refund amount",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "failed",
                          "message": "Invalid refund amount"
                        }
                        """
                )
            )
        )
    })
    public Response refundPayment(
            @Parameter(
                description = "Amount to refund (must be positive)",
                required = true,
                schema = @Schema(
                    type = SchemaType.NUMBER,
                    format = "double",
                    minimum = "0.01",
                    example = "99.99"
                )
            )
            @QueryParam("amount") BigDecimal amount) {
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
