package io.microprofile.tutorial.store.product.config;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.OASFilter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExtensionFilter implements OASFilter {
    
    @Override
    public Operation filterOperation(Operation operation) {
        if (operation == null) {
            return operation;
        }
        
        try {
            // Get existing extensions (or create new map if null)
            Map<String, Object> existingExtensions = operation.getExtensions();
            if (existingExtensions == null) {
                return operation; // No extensions to process
            }
            
            // Create a new map with existing extensions
            Map<String, Object> newExtensions = new LinkedHashMap<>(existingExtensions);
            
            // Check if a custom timeout extension exists
            if (newExtensions.containsKey("x-custom-timeout")) {
                Object timeout = newExtensions.get("x-custom-timeout");
                if (timeout != null) {
                    int timeoutValue = Integer.parseInt(timeout.toString());
                    if (timeoutValue > 30) {
                        newExtensions.put("x-requires-approval", "true");
                    }
                }
            }
            
            // Check for rate limiting configuration
            if (newExtensions.containsKey("x-rate-limit")) {
                Object rateLimit = newExtensions.get("x-rate-limit");
                if (rateLimit != null) {
                    int rateLimitValue = Integer.parseInt(rateLimit.toString());
                    if (rateLimitValue > 500) {
                        newExtensions.put("x-high-volume", "true");
                    }
                }
            }
            
            // Check for authentication requirements
            if (newExtensions.containsKey("x-requires-auth")) {
                Object authLevel = newExtensions.get("x-requires-auth");
                if (authLevel != null && "admin".equals(authLevel.toString())) {
                    newExtensions.put("x-security-notice", 
                        "This operation requires administrator privileges");
                }
            }
            
            // Set the modified extensions map back
            operation.setExtensions(newExtensions);
            
        } catch (Exception e) {
            System.err.println("Error in ExtensionFilter: " + e.getMessage());
            e.printStackTrace();
        }
        
        return operation;
    }
}