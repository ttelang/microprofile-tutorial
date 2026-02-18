package io.microprofile.tutorial.store.product.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;

/**
 * Product entity demonstrating MicroProfile OpenAPI 4.1 and JSON Schema 2020-12 features:
 * - Pattern-based validation for strings
 * - Numeric constraints with exclusiveMinimum and multipleOf
 * - Format specifications (int64, double)
 * - Nullable handling aligned with JSON Schema
 * - Type-safe enum values for categories
 * - Rich descriptions and examples
 * - Read-only properties
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(
    description = "Product entity representing an item in the e-commerce store"
)
public class Product {
    
    @Schema(
        description = "Unique identifier for the product",
        readOnly = true,
        type = SchemaType.INTEGER,
        format = "int64",
        minimum = "1"
    )
    private Long id;
    
    @NotBlank(message = "Product name is required")
    @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-]+$", message = "Product name can only contain alphanumeric characters, spaces, and hyphens")
    @Schema(
        description = "Product name - alphanumeric characters, spaces, and hyphens allowed",
        required = true,
        minLength = 1,
        maxLength = 100
    )
    private String name;
    
    @Size(max = 500, message = "Product description cannot exceed 500 characters")
    @Schema(
        description = "Detailed product description",
        maxLength = 500,
        nullable = true
    )
    private String description;
    
    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", inclusive = false, message = "Price must be greater than $0.01")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed $999,999.99")
    @Digits(integer = 6, fraction = 2, message = "Price must be rounded to 2 decimal places and cannot exceed $999,999.99")
    @Schema(
        description = "Product price in USD - must be greater than $0.00 and rounded to 2 decimal places",
        required = true,
        type = SchemaType.NUMBER
    )
    private Double price;
    
    @Pattern(regexp = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$", 
             message = "SKU must follow format: 3 uppercase letters, dash, alphanumeric, dash, alphanumeric (e.g., ELC-MS001-BLK)")
    @Size(min = 5, max = 50, message = "SKU must be between 5 and 50 characters")
    @Schema(
        description = "Stock Keeping Unit - unique product identifier following format XXX-XXXXX-XXXX",
        pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$",
        minLength = 5,
        maxLength = 50,
        nullable = true
    )
    private String sku;
    
    @Schema(
        description = """
            Product category - Type-safe enum value.
            
            Using ProductCategory enum provides:
            - Compile-time type checking
            - IDE auto-completion
            - Automatic OpenAPI enum value extraction
            - Prevention of invalid category values
            
            OpenAPI automatically generates dropdown with all enum values in Swagger UI.
            """,
        implementation = ProductCategory.class,
        nullable = true
    )
    private ProductCategory category;
    
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Schema(
        description = "Available quantity in stock",
        type = SchemaType.INTEGER,
        format = "int32",
        minimum = "0",
        defaultValue = "0"
    )
    private Integer stockQuantity;
    
    @Schema(
        description = "Indicates if the product is currently in stock",
        type = SchemaType.BOOLEAN,
        defaultValue = "true"
    )
    private Boolean inStock;
}