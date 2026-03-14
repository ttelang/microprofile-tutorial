package io.microprofile.tutorial.store.payment.resource;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

import io.microprofile.tutorial.store.payment.config.PaymentGatewayConfig;

/**
 * Resource to demonstrate the use of @ConfigMapping and custom ConfigSource.
 * This endpoint allows viewing the current payment gateway configuration.
 */
@ApplicationScoped
@Path("/payment-config")
public class PaymentConfigResource {
    
    @Inject
    private PaymentGatewayConfig gatewayConfig;
    
    /**
     * Get all payment gateway configuration properties.
     * Demonstrates accessing grouped configuration via @ConfigMapping.
     * 
     * @return Response with all payment gateway configuration
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPaymentConfig() {
        // Create a response object with all gateway configuration properties
        Map<String, Object> configValues = new HashMap<>();
        configValues.put("endpoint", gatewayConfig.getEndpoint());
        configValues.put("apiKey", gatewayConfig.getApiKey());
        configValues.put("timeout", gatewayConfig.getTimeout());
        configValues.put("retryAttempts", gatewayConfig.getRetryAttempts());
        configValues.put("sandboxMode", gatewayConfig.isSandboxMode());
        
        return Response.ok(configValues).build();
    }
}
