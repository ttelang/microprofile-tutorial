package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product processing result")
public class ProcessResult {
    
    @Schema(description = "ID of the processed product", example = "12345")
    private Long productId;
    
    @Schema(description = "Processing status", example = "SUCCESS")
    private ProcessingStatus status;
    
    @Schema(description = "Result message", example = "Product processed successfully")
    private String message;
    
    @Schema(description = "Processing timestamp", example = "2024-02-04T10:30:00Z")
    private String timestamp;
    
    @Schema(description = "Processing status values")
    public enum ProcessingStatus {
        SUCCESS,
        FAILED,
        PARTIAL
    }
}