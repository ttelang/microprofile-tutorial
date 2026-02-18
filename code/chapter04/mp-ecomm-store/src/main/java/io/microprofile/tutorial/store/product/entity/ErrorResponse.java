package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * Standardized error response for API errors.
 * 
 * <p>This entity provides a consistent error format across all endpoints,
 * making it easier for clients to handle errors programmatically.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ErrorResponse {
    
    @NotBlank(message = "Error message is required")
    @Schema(description = "Human-readable error description")
    private String error;
    
    @Schema(description = "Error timestamp (ISO-8601 format with UTC timezone)")
    private Instant timestamp;
    
    @Schema(description = "Additional error details", nullable = true)
    private String details;
    
    /**
     * Constructor for simple error messages without details.
     */
    public ErrorResponse(String error) {
        this.error = error;
        this.timestamp = Instant.now();
        this.details = null;
    }
    
    /**
     * Constructor for errors with additional details.
     */
    public ErrorResponse(String error, String details) {
        this.error = error;
        this.timestamp = Instant.now();
        this.details = details;
    }
}
