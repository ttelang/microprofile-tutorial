package io.microprofile.tutorial.store.product.config;

import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.OASFilter;

public class ExtensionFilter implements OASFilter {
    
    @Override
    public Operation filterOperation(Operation operation) {
        if (operation == null) {
            return operation;
        }
        
        try {
            // Check if a custom timeout extension exists
            Object timeout = operation.getExtension("x-custom-timeout");
            if (timeout != null) {
                int timeoutValue = Integer.parseInt(timeout.toString());
                if (timeoutValue > 30) {
                    operation.addExtension("x-requires-approval", "true");
                }
            }
            
            // Check for rate limiting configuration
            Object rateLimit = operation.getExtension("x-rate-limit");
            if (rateLimit != null) {
                int rateLimitValue = Integer.parseInt(rateLimit.toString());
                if (rateLimitValue > 500) {
                    operation.addExtension("x-high-volume", "true");
                }
            }
            
            // Check for authentication requirements
            Object authLevel = operation.getExtension("x-requires-auth");
            if (authLevel != null && "admin".equals(authLevel.toString())) {
                operation.addExtension("x-security-notice", 
                    "This operation requires administrator privileges");
            }
            
        } catch (Exception e) {
            System.err.println("Error in ExtensionFilter: " + e.getMessage());
            e.printStackTrace();
        }
        
        return operation;
    }
}