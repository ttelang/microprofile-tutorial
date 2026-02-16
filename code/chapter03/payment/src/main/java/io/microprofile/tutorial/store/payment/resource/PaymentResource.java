package io.microprofile.tutorial.store.payment.resource;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import io.microprofile.tutorial.store.payment.interceptor.Logged;
import io.microprofile.tutorial.store.payment.service.PaymentService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
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
