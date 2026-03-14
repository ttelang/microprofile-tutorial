package io.microprofile.tutorial.store.payment.config;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Custom ConfigSource for Payment Service.
 * This config source provides payment-specific configuration with high priority.
 * Ordinal 600 ensures it overrides system properties (400), environment variables (300),
 * and microprofile-config.properties (100) for properties it provides.
 * 
 * This demonstrates how custom ConfigSources can enforce certain configurations
 * that cannot be accidentally overridden by typical deployment-time settings.
 */
public class PaymentServiceConfigSource implements ConfigSource {

    private static final Logger LOGGER = Logger.getLogger(PaymentServiceConfigSource.class.getName());
    
    private static final Map<String, String> properties = new HashMap<>();

    private static final String NAME = "PaymentServiceConfigSource";
    private static final int ORDINAL = 600; // Higher ordinal means higher priority
    
    public PaymentServiceConfigSource() {
       LOGGER.info("Initializing PaymentServiceConfigSource with ordinal: " + ORDINAL);
       
       // Load payment service configurations dynamically
       // This example uses hardcoded values for demonstration
       properties.put("payment.gateway.endpoint", "https://api.paymentgateway.com/v1");
       
       LOGGER.info("PaymentServiceConfigSource loaded with " + properties.size() + " properties");
   }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public Set<String> getPropertyNames() {
        return properties.keySet();
    }

    @Override
    public String getValue(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public int getOrdinal() {
        return ORDINAL;
    }
    
    /**
     * Updates a configuration property at runtime.
     * 
     * @param key the property key
     * @param value the property value
     */
    public static void setProperty(String key, String value) {
        properties.put(key, value);
    }
}
