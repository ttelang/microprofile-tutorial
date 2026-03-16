package io.microprofile.tutorial.store.payment.resource;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.exception.CriticalPaymentException;
import io.microprofile.tutorial.store.payment.exception.PaymentProcessingException;
import io.microprofile.tutorial.store.payment.service.PaymentService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

/**
 * REST resource for payment operations with MicroProfile Fault Tolerance.
 * 
 * Demonstrates proper async Jakarta REST endpoint patterns that don't block on async results.
 */
@RequestScoped
@Path("/")
public class PaymentResource {
    
    private static final Logger logger = 
        Logger.getLogger(PaymentResource.class.getName());

    @Inject
    @ConfigProperty(name = "payment.gateway.endpoint", 
        defaultValue = "https://api.paymentgateway.com")
    private String endpoint;

    @Inject
    private PaymentService paymentService;

    /**
     * Authorize payment - demonstrates Retry + Timeout + Fallback.
     * 
     * Returns CompletionStage to avoid blocking on async service method.
     * Jakarta REST will handle the async response when the CompletionStage 
     * completes.
     */
    @POST
    @Path("/authorize")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Authorize payment", description = "Authorize payment with retry, timeout, and fallback protection")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Payment authorized successfully"),
        @APIResponse(responseCode = "400", description = "Invalid payment amount"),
        @APIResponse(responseCode = "500", description = "Payment processing failed")
    })
    public Response authorizePayment(@QueryParam("amount") Double amount) {
        
        // Input validation
        if (amount == null || amount <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\":\"Invalid payment amount: " + amount + "\"}")
                .build();
        }

        try {
            // Create PaymentDetails
            PaymentDetails paymentDetails = new PaymentDetails(
                "****-****-****-1111", // cardNumber - placeholder for demo
                "Demo User", // cardHolderName
                "12/25", // expiryDate
                "***", // securityCode
                BigDecimal.valueOf(amount) // amount
            );

            // Call service method with fault tolerance
            // Note: This method has @Retry and @Fallback, so it returns either:
            // - Success result, or
            // - Fallback result (if retries exhausted or critical error)
            String result = paymentService.authorizePayment(paymentDetails);
            
            return Response.ok(result, MediaType.APPLICATION_JSON).build();
            
        } catch (Exception e) {
            // This should rarely happen since fallback handles most failures
            logger.severe("Unexpected payment error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\":\"An unexpected error occurred\"}")
                .build();
        }
    }

    /**
     * Check payment gateway health - demonstrates CircuitBreaker + Timeout.
     * 
     * Circuit breaker prevents hammering a failed gateway with repeated health checks.
     */
    @GET
    @Path("/health/gateway")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Check gateway health", description = "Check payment gateway health with circuit breaker protection")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Gateway is healthy"),
        @APIResponse(responseCode = "503", description = "Gateway is unavailable")
    })
    public Response checkGatewayHealth() {
        try {
            boolean healthy = paymentService.checkGatewayHealth();
            
            if (healthy) {
                return Response.ok()
                    .entity("{\"status\":\"healthy\",\"message\":\"Payment gateway is operational\"}")
                    .build();
            } else {
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"status\":\"unhealthy\",\"message\":\"Payment gateway is not responding\"}")
                    .build();
            }
        } catch (Exception e) {
            // Circuit breaker is OPEN or timeout occurred
            logger.warning("Gateway health check failed: " + e.getMessage());
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("{\"status\":\"circuit_open\",\"message\":\"Circuit breaker is open - gateway appears to be down\"}")
                .build();
        }
    }

    /**
     * Send payment notification - demonstrates Asynchronous + Bulkhead + Fallback.
     * 
     * Returns CompletionStage for proper async handling without blocking.
     * JAX-RS automatically handles the async response.
     */
    @POST
    @Path("/notify/{paymentId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Send payment notification", description = "Send asynchronous payment notification with bulkhead protection")
    @APIResponses(value = {
        @APIResponse(responseCode = "200", description = "Notification sent or queued"),
        @APIResponse(responseCode = "503", description = "Bulkhead rejected request")
    })
    public CompletionStage<Response> sendNotification(
        @PathParam("paymentId") String paymentId,
        @QueryParam("recipient") String recipient
    ) {
        
        if (recipient == null || recipient.isEmpty()) {
            recipient = "default@example.com";
        }

        // Call async service method and map result to Response
        // This does NOT block - JAX-RS handles the CompletionStage
        return paymentService.sendPaymentNotification(paymentId, recipient)
            .thenApply(result -> {
                logger.info("Notification result: " + result);
                return Response.ok()
                    .entity("{\"status\":\"success\",\"message\":\"" + result + "\"}")
                    .build();
            })
            .exceptionally(ex -> {
                // Handle bulkhead rejection or other failures
                logger.warning("Notification failed: " + ex.getMessage());
                
                if (ex.getMessage() != null && ex.getMessage().contains("BulkheadException")) {
                    return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity("{\"status\":\"rejected\",\"message\":\"Too many concurrent notifications - please try again later\"}")
                        .build();
                }
                
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"Notification processing failed\"}")
                    .build();
            });
    }
}
