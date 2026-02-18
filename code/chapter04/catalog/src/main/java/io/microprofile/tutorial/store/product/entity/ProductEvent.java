package io.microprofile.tutorial.store.product.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Event payload sent to webhook subscribers when product-related events occur.
 * 
 * <p>This class represents a webhook notification that is sent to subscribed URLs
 * when specific product events occur (creation, updates, stock changes, etc.).
 * 
 * <p><b>OpenAPI 3.1 Feature:</b> This schema is used in webhook callback documentation
 * via the {@code @Callback} annotation in WebhookResource.
 * 
 * <h3>Event Flow:</h3>
 * <ol>
 *   <li>Product event occurs (e.g., product created)</li>
 *   <li>System generates ProductEvent with unique ID and timestamp</li>
 *   <li>Event is sent to all subscribed webhook URLs</li>
 *   <li>Subscribers receive this JSON payload via HTTP POST</li>
 * </ol>
 * 
 * @see EventType for all supported event types
 * @see Product for the product object structure
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description = "Event notification sent to webhook subscribers when product changes occur"
)
public class ProductEvent {
    
    @NotBlank(message = "Event ID is required")
    @Size(min = 10, max = 50, message = "Event ID must be between 10 and 50 characters")
    @Schema(description = "Unique event identifier (typically UUID or generated ID)")
    private String eventId;
    
    @NotNull(message = "Event type is required")
    @Schema(
        description = "Type of event that occurred",
        implementation = EventType.class
    )
    private EventType eventType;
    
    @NotNull(message = "Timestamp is required")
    @Schema(
        description = "Timestamp when the event occurred (ISO-8601 format with UTC timezone)",
        type = SchemaType.STRING,
        format = "date-time"
    )
    private Instant timestamp;
    
    @NotNull(message = "Product is required")
    @Schema(
        description = "The product that triggered the event",
        implementation = Product.class
    )
    private Product product;
    
    @Schema(
        description = "Additional metadata about the event (optional, can contain contextual information)",
        nullable = true,
        maxLength = 1000
    )
    private String metadata;
}
