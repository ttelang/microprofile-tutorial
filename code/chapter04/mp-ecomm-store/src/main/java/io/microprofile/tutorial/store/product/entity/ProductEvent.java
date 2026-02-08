package io.microprofile.tutorial.store.product.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import java.time.LocalDateTime;

/**
 * Event payload sent to webhook subscribers when product-related events occur.
 * Demonstrates webhook payload schema in OpenAPI 3.1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description = "Event notification sent to webhook subscribers when product changes occur",
    example = """
        {
            "eventId": "evt_1234567890",
            "eventType": "product.created",
            "timestamp": "2026-02-08T10:30:00",
            "product": {
                "id": 4,
                "name": "Sony WH-1000XM5",
                "price": 399.99,
                "sku": "SON-WH1-XM5-BLK",
                "category": "ELECTRONICS"
            }
        }
        """
)
public class ProductEvent {
    
    @Schema(
        description = "Unique event identifier",
        example = "evt_1234567890",
        required = true,
        minLength = 10,
        maxLength = 50
    )
    private String eventId;
    
    @Schema(
        description = "Type of event that occurred",
        example = "product.created",
        required = true,
        enumeration = {
            "product.created",
            "product.updated",
            "product.deleted",
            "product.stock.low",
            "product.stock.out"
        }
    )
    private String eventType;
    
    @Schema(
        description = "Timestamp when the event occurred",
        example = "2026-02-08T10:30:00",
        required = true,
        type = SchemaType.STRING,
        format = "date-time"
    )
    private LocalDateTime timestamp;
    
    @Schema(
        description = "The product that triggered the event",
        required = true,
        implementation = Product.class
    )
    private Product product;
    
    @Schema(
        description = "Additional metadata about the event",
        nullable = true,
        example = "{\"triggeredBy\": \"system\", \"ipAddress\": \"192.168.1.1\"}"
    )
    private String metadata;
}
