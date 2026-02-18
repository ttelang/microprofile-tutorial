package io.microprofile.tutorial.store.product.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * Webhook subscription for receiving product event notifications.
 * 
 * <p>Demonstrates webhook subscription management in OpenAPI 3.1.
 * When created, the system generates a unique ID and secret for verifying
 * webhook authenticity using HMAC-SHA256 signatures.
 * 
 * <h3>Webhook Delivery:</h3>
 * <ul>
 *   <li>POST requests sent to configured URL when events occur</li>
 *   <li>X-Webhook-Signature header contains HMAC-SHA256(secret, body)</li>
 *   <li>Failed deliveries (5xx) trigger automatic retry with exponential backoff</li>
 *   <li>After 5 consecutive failures, subscription marked inactive</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Webhook subscription configuration for receiving product event notifications")
public class WebhookSubscription {
    
    @Schema(
        description = "Unique subscription identifier (auto-generated)",
        readOnly = true
    )
    private String id;
    
    @NotBlank(message = "Callback URL is required")
    @Pattern(
        regexp = "^https://.*",
        message = "Callback URL must use HTTPS protocol"
    )
    @Schema(
        description = "Callback URL where webhook events will be sent (must be HTTPS)",
        format = "uri"
    )
    private String url;
    
    @NotNull(message = "Events list is required")
    @Size(min = 1, message = "At least one event type must be specified")
    @Schema(
        description = "List of event types to subscribe to (must specify at least one)",
        type = SchemaType.ARRAY,
        enumeration = {
            "product.created",
            "product.updated",
            "product.deleted",
            "product.stock.low",
            "product.stock.out"
        }
    )
    private List<String> events;
    
    @Schema(
        description = "Secret key for webhook signature verification using HMAC-SHA256 (auto-generated)",
        readOnly = true
    )
    private String secret;
    
    @Schema(
        description = "Whether the webhook subscription is active (default: true)",
        defaultValue = "true"
    )
    private Boolean active;
    
    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Schema(
        description = "Optional description of the webhook's purpose",
        nullable = true
    )
    private String description;
}
