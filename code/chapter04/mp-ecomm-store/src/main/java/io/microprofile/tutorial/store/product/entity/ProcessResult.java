package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

/**
 * Result of asynchronous product processing operation.
 * 
 * <p>This entity is sent to the callback URL when async processing completes.
 * It contains the processing outcome, generated product ID (if successful),
 * and a descriptive message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product processing result sent to callback URL")
public class ProcessResult {
    
    @Schema(
        description = "ID of the processed product (null if processing failed)",
        nullable = true
    )
    private Long productId;
    
    @NotNull(message = "Status is required")
    @Schema(
        description = "Processing status",
        implementation = ProcessingStatus.class
    )
    private ProcessingStatus status;
    
    @NotBlank(message = "Message is required")
    @Schema(description = "Result message describing the outcome")
    private String message;
    
    @NotNull(message = "Timestamp is required")
    @Schema(
        description = "Processing completion timestamp (ISO-8601 format with UTC timezone)",
        type = SchemaType.STRING,
        format = "date-time"
    )
    private Instant timestamp;
}