package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Type-safe enumeration for webhook event types.
 * 
 * <p>Defines all possible product-related events that can trigger webhook notifications.
 * Using an enum provides:
 * <ul>
 *   <li>Compile-time type safety</li>
 *   <li>IDE auto-completion</li>
 *   <li>Prevention of invalid event types</li>
 *   <li>Clear documentation of all event types</li>
 * </ul>
 */
@Schema(
    description = "Product event types for webhook notifications",
    enumeration = {
        "product.created", "product.updated", "product.deleted",
        "product.stock.low", "product.stock.out"
    }
)
public enum EventType {
    
    /**
     * Fired when a new product is created
     */
    PRODUCT_CREATED("product.created", "New product created"),
    
    /**
     * Fired when an existing product is updated
     */
    PRODUCT_UPDATED("product.updated", "Product information updated"),
    
    /**
     * Fired when a product is deleted
     */
    PRODUCT_DELETED("product.deleted", "Product removed from catalog"),
    
    /**
     * Fired when product stock falls below threshold
     */
    PRODUCT_STOCK_LOW("product.stock.low", "Product stock is running low"),
    
    /**
     * Fired when product is out of stock
     */
    PRODUCT_STOCK_OUT("product.stock.out", "Product is out of stock");
    
    private final String value;
    private final String description;
    
    /**
     * Constructor for event type with value and description.
     * 
     * @param value The event type value (e.g., "product.created")
     * @param description Human-readable description
     */
    EventType(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    /**
     * Get the string value of the event type.
     * Used for JSON serialization.
     * 
     * @return Event type value (e.g., "product.created")
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the human-readable description.
     * 
     * @return Event type description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Parse a string to EventType enum.
     * Case-insensitive and trims whitespace.
     * 
     * @param value String representation of event type (null-safe)
     * @return EventType enum value, or null if input is null
     * @throws IllegalArgumentException if value is not a valid event type
     */
    public static EventType fromString(String value) {
        if (value == null) {
            return null;
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return null;
        }
        
        for (EventType type : EventType.values()) {
            if (type.value.equalsIgnoreCase(trimmedValue)) {
                return type;
            }
        }
        
        throw new IllegalArgumentException(
            "Invalid event type: '" + value + "'. Allowed values: " + getAllEventTypes()
        );
    }
    
    /**
     * Check if a string is a valid event type.
     * 
     * @param value String to validate (null-safe)
     * @return true if valid event type, false otherwise
     */
    public static boolean isValid(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get all event type values as a comma-separated string.
     * Useful for error messages and documentation.
     * 
     * @return Comma-separated list of all event type values
     */
    public static String getAllEventTypes() {
        return String.join(", ",
            java.util.Arrays.stream(EventType.values())
                .map(EventType::getValue)
                .toArray(String[]::new)
        );
    }
    
    /**
     * Override toString to return the value for JSON serialization.
     * 
     * @return Event type value
     */
    @Override
    public String toString() {
        return value;
    }
}
