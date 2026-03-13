package io.microprofile.tutorial.store.payment.config;

import org.eclipse.microprofile.config.inject.ConfigProperties;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Configuration properties group for Payment Gateway settings.
 * 
 * This class demonstrates the @ConfigProperties annotation from MicroProfile Config 3.1,
 * which allows grouping related configuration properties under a common prefix.
 * 
 * Instead of injecting individual properties:
 * <pre>
 * {@code @Inject @ConfigProperty(name="payment.gateway.endpoint") String endpoint;}
 * {@code @Inject @ConfigProperty(name="payment.gateway.apiKey") String apiKey;}
 * {@code @Inject @ConfigProperty(name="payment.gateway.timeout") int timeout;}
 * </pre>
 * 
 * You can inject the entire configuration group:
 * <pre>
 * {@code @Inject PaymentGatewayConfig gatewayConfig;}
 * </pre>
 * 
 * This provides several benefits:
 * - Cleaner code with less injection boilerplate
 * - Grouped related configuration logically
 * - Single point of configuration for a feature
 * - Better testability and mockability
 */
@ConfigProperties(prefix = "payment.gateway")
@ApplicationScoped
public class PaymentGatewayConfig {
    
    /**
     * Payment gateway API endpoint URL.
     * Maps to: payment.gateway.endpoint
     */
    public String endpoint;
    
    /**
     * Payment gateway API key for authentication.
     * Maps to: payment.gateway.apiKey
     */
    public String apiKey;
    
    /**
     * Connection timeout in milliseconds.
     * Maps to: payment.gateway.timeout
     */
    public int timeout;
    
    /**
     * Number of retry attempts for failed requests.
     * Maps to: payment.gateway.retryAttempts
     */
    public int retryAttempts;
    
    /**
     * Whether to use sandbox/test mode.
     * Maps to: payment.gateway.sandboxMode
     */
    public boolean sandboxMode;
    
    // Getters and setters
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getRetryAttempts() {
        return retryAttempts;
    }
    
    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
    
    public boolean isSandboxMode() {
        return sandboxMode;
    }
    
    public void setSandboxMode(boolean sandboxMode) {
        this.sandboxMode = sandboxMode;
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
