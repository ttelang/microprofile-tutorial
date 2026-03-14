package io.microprofile.tutorial.store.payment.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Configuration properties group for Payment Gateway settings.
 * 
 * This class demonstrates grouping related configuration properties for better organization.
 * Each property is injected individually using @ConfigProperty.
 * 
 * Instead of scattering configuration injections across multiple classes:
 * <pre>
 * {@code @Inject @ConfigProperty(name="payment.gateway.endpoint") String endpoint;}
 * {@code @Inject @ConfigProperty(name="payment.gateway.apiKey") String apiKey;}
 * </pre>
 * 
 * You can inject the entire configuration group:
 * <pre>
 * {@code @Inject PaymentGatewayConfig gatewayConfig;}
 * </pre>
 * 
 * This provides several benefits:
 * - Cleaner code with centralized configuration
 * - Grouped related configuration logically
 * - Single point of configuration for a feature
 * - Better testability and mockability
 */
@ApplicationScoped
public class PaymentGatewayConfig {
    
    @Inject
    @ConfigProperty(name = "payment.gateway.endpoint")
    private String endpoint;
    
    @Inject
    @ConfigProperty(name = "payment.gateway.api-key")
    private String apiKey;
    
    @Inject
    @ConfigProperty(name = "payment.gateway.timeout")
    private int timeout;
    
    @Inject
    @ConfigProperty(name = "payment.gateway.retry-attempts")
    private int retryAttempts;
    
    @Inject
    @ConfigProperty(name = "payment.gateway.sandbox-mode")
    private boolean sandboxMode;
    
    /**
     * Gets the payment gateway API endpoint URL.
     */
    public String getEndpoint() {
        return endpoint;
    }
    
    /**
     * Gets the payment gateway API key for authentication.
     */
    public String getApiKey() {
        return apiKey;
    }
    
    /**
     * Gets the connection timeout in milliseconds.
     */
    public int getTimeout() {
        return timeout;
    }
    
    /**
     * Gets the number of retry attempts for failed requests.
     */
    public int getRetryAttempts() {
        return retryAttempts;
    }
    
    /**
     * Checks if sandbox/test mode is enabled.
     */
    public boolean isSandboxMode() {
        return sandboxMode;
    }
    
    @Override
    public String toString() {
        return "PaymentGatewayConfig{" +
                "endpoint='" + endpoint + '\'' +
                ", apiKey='***MASKED***'" + // Don't expose API key in logs
                ", timeout=" + timeout +
                ", retryAttempts=" + retryAttempts +
                ", sandboxMode=" + sandboxMode +
                '}';
    }
}
