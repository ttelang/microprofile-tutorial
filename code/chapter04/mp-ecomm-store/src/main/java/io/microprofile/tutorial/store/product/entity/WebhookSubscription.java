package io.microprofile.tutorial.store.product.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import java.util.List;

/**
 * Represents a webhook subscription for receiving product event notifications.
 * Demonstrates webhook subscription management in OpenAPI 3.1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description = "Webhook subscription configuration for receiving product event notifications",
    example = """
        {
            "id": "sub_abc123",
            "url": "https://example.com/webhooks/products",
            "events": ["product.created", "product.updated"],
            "secret": "whsec_abc123...",
            "active": true
        }
        """
)
public class WebhookSubscription {
    
    @Schema(
        description = "Unique subscription identifier",
        example = "sub_abc123",
        readOnly = true,
        minLength = 8,
        maxLength = 50
    )
    private String id;
    
    @Schema(
        description = "Callback URL where webhook events will be sent (must be HTTPS)",
        example = "https://example.com/webhooks/products",
        required = true,
        format = "uri",
        pattern = "^https://.*"
    )
    private String url;
    
    @Schema(
        description = "List of event types to subscribe to",
        required = true,
        type = SchemaType.ARRAY,
        minItems = 1,
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
        description = "Secret key for webhook signature verification (HMAC-SHA256)",
        example = "whsec_abc123def456...",
        readOnly = true,
        pattern = "^whsec_[a-zA-Z0-9]{32,}$"
    )
    private String secret;
    
    @Schema(
        description = "Whether the webhook subscription is active",
        example = "true",
        defaultValue = "true"
    )
    private Boolean active;
    
    @Schema(
        description = "Optional description of the webhook's purpose",
        example = "Sync product catalog to warehouse system",
        maxLength = 200,
        nullable = true
    )
    private String description;
}
