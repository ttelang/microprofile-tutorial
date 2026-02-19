package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Type-safe enumeration for product categories.
 * 
 * <p>Using Java enums with OpenAPI provides:
 * <ul>
 *   <li>Compile-time type safety</li>
 *   <li>IDE auto-completion</li>
 *   <li>Automatic OpenAPI schema generation with enum values</li>
 *   <li>Prevention of invalid category values</li>
 *   <li>Clear documentation of allowed values</li>
 * </ul>
 * 
 * <p>Each category includes:
 * <ul>
 *   <li><b>Enum Constant:</b> Java enum value (e.g., ELECTRONICS)</li>
 *   <li><b>Display Name:</b> User-friendly name for UI (e.g., "Electronics")</li>
 *   <li><b>Description:</b> Detailed explanation of the category</li>
 * </ul>
 * 
 * <p><b>OpenAPI Integration:</b> The class-level {@code @Schema} annotation
 * defines the enumeration values that appear in the OpenAPI specification.
 * Individual enum constants use JavaDoc for code documentation.
 */
@Schema(
    description = "Product category enumeration",
    enumeration = {
        "ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GARDEN", 
        "SPORTS", "TOYS", "FOOD", "BEAUTY"
    }
)
public enum ProductCategory {
    
    /**
     * Electronic devices, computers, accessories
     */
    ELECTRONICS("Electronics", "Electronic devices and accessories"),
    
    /**
     * Clothing, shoes, fashion accessories
     */
    CLOTHING("Clothing", "Apparel and fashion items"),
    
    /**
     * Books, e-books, audiobooks
     */
    BOOKS("Books", "Books and reading materials"),
    
    /**
     * Home decor, furniture, garden supplies
     */
    HOME_GARDEN("Home & Garden", "Home and garden products"),
    
    /**
     * Sports equipment, fitness gear
     */
    SPORTS("Sports", "Sports and fitness equipment"),
    
    /**
     * Toys, games, puzzles
     */
    TOYS("Toys", "Toys and games"),
    
    /**
     * Food, beverages, groceries
     */
    FOOD("Food", "Food and beverage products"),
    
    /**
     * Beauty products, cosmetics, personal care
     */
    BEAUTY("Beauty", "Beauty and personal care products");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructor for enum values with display name and description.
     * 
     * @param displayName Human-readable name for UI display
     * @param description Detailed description of the category
     */
    ProductCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Get the display name for UI rendering.
     * 
     * @return Human-readable category name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the category description.
     * 
     * @return Category description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Parse a string to ProductCategory enum.
     * Case-insensitive and trims whitespace.
     * 
     * @param value String representation of category (null-safe)
     * @return ProductCategory enum value, or null if input is null
     * @throws IllegalArgumentException if value is not a valid category
     */
    public static ProductCategory fromString(String value) {
        if (value == null) {
            return null;
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return null;
        }
        
        try {
            return ProductCategory.valueOf(trimmedValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid category: '" + value + "'. Allowed values: " + getAllCategoryNames()
            );
        }
    }
    
    /**
     * Check if a string is a valid category.
     * Case-insensitive and trims whitespace.
     * 
     * @param value String to validate (null-safe)
     * @return true if valid category, false otherwise
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            return false;
        }
        
        try {
            ProductCategory.valueOf(trimmedValue.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Get all enum constant names as a comma-separated string.
     * Useful for error messages and documentation.
     * 
     * @return Comma-separated list of all category names
     */
    public static String getAllCategoryNames() {
        return String.join(", ", 
            java.util.Arrays.stream(ProductCategory.values())
                .map(Enum::name)
                .toArray(String[]::new)
        );
    }
}
