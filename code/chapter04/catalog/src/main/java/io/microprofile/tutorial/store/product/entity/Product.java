package io.microprofile.tutorial.store.product.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity demonstrating MicroProfile OpenAPI 4.1 alignment with OpenAPI v3.1
 * and JSON Schema 2020-12.
 * 
 * Key JSON Schema 2020-12 features demonstrated:
 * - Pattern-based validation for strings
 * - Numeric constraints (minimum, maximum, exclusiveMinimum, multipleOf)
 * - String length constraints (minLength, maxLength)
 * - Format specifications (double, int64, date-time, uuid)
 * - Nullable handling aligned with JSON Schema
 * - Rich descriptions and examples
 * - Enumeration support
 * - Default values
 * - Read-only properties
 */
@Entity
@Table(name = "PRODUCTS")
@NamedQueries({
    @NamedQuery(name = "Product.findAll", query = "SELECT p FROM Product p"),
    @NamedQuery(name = "Product.findById", query = "SELECT p FROM Product p WHERE p.id = :id"),
    @NamedQuery(name = "Product.findByCategory", 
                query = "SELECT p FROM Product p WHERE p.category = :category"),
    @NamedQuery(name = "Product.searchByPriceRange", 
                query = "SELECT p FROM Product p WHERE p.price > :minPrice AND p.price <= :maxPrice")
})
@Schema(
    description = "Product entity representing an item in the e-commerce catalog",
    example = """
        {
            "id": 1,
            "name": "iPhone 15 Pro",
            "description": "Apple iPhone 15 Pro with 256GB storage and titanium design",
            "price": 999.99,
            "sku": "APL-IPH15P-256",
            "category": "ELECTRONICS",
            "stockQuantity": 50,
            "rating": 4.8,
            "weight": 0.187,
            "active": true,
            "manufacturer": "Apple Inc.",
            "warrantyMonths": 12
        }
        """
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    @Schema(
        description = "Unique identifier for the product",
        example = "1",
        readOnly = true,
        type = SchemaType.INTEGER,
        format = "int64",
        minimum = "1"
    )
    private Long id;

    @NotNull(message = "Product name cannot be null")
    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
    @Column(name = "NAME", nullable = false, length = 100)
    @Schema(
        description = "Product name - alphanumeric characters, spaces, and hyphens allowed",
        example = "iPhone 15 Pro",
        required = true,
        minLength = 1,
        maxLength = 100,
        pattern = "^[a-zA-Z0-9\\s\\-]+$"
    )
    private String name;

    @Size(max = 500, message = "Product description cannot exceed 500 characters")
    @Column(name = "DESCRIPTION", length = 500)
    @Schema(
        description = "Detailed product description",
        example = "Apple iPhone 15 Pro with 256GB storage, titanium design, and advanced camera system",
        maxLength = 500,
        nullable = true
    )
    private String description;

    @NotNull(message = "Product price cannot be null")
    @DecimalMin(value = "0.01", inclusive = true, message = "Product price must be greater than 0")
    @Column(name = "PRICE", nullable = false, precision = 10, scale = 2)
    @Schema(
        description = "Product price in USD - must be greater than $0.00 and rounded to 2 decimal places",
        example = "999.99",
        required = true,
        type = SchemaType.NUMBER,
        format = "double",
        minimum = "0.01",
        maximum = "999999.99",
        multipleOf = 0.01
    )
    private Double price;

    @Column(name = "SKU", unique = true, length = 50)
    @Pattern(regexp = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$", 
             message = "SKU must follow format: XXX-XXXXX-XXXX (e.g., APL-IPH15P-256)")
    @Schema(
        description = "Stock Keeping Unit - unique product identifier following format XXX-XXXXX-XXXX",
        example = "APL-IPH15P-256",
        pattern = "^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$",
        minLength = 5,
        maxLength = 50,
        nullable = true
    )
    private String sku;

    @Column(name = "CATEGORY", length = 50)
    @Schema(
        description = "Product category - must be one of the predefined categories",
        example = "ELECTRONICS",
        enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GARDEN", "SPORTS", "TOYS", "FOOD", "BEAUTY"},
        nullable = true
    )
    private String category;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(name = "STOCK_QUANTITY")
    @Schema(
        description = "Available quantity in stock",
        example = "50",
        type = SchemaType.INTEGER,
        format = "int32",
        minimum = "0",
        defaultValue = "0"
    )
    private Integer stockQuantity;

    @DecimalMin(value = "0.0", message = "Rating cannot be negative")
    @DecimalMax(value = "5.0", message = "Rating cannot exceed 5.0")
    @Column(name = "RATING", precision = 3, scale = 2)
    @Schema(
        description = "Product rating from 0.0 to 5.0 based on customer reviews",
        example = "4.8",
        type = SchemaType.NUMBER,
        format = "double",
        minimum = "0.0",
        maximum = "5.0",
        nullable = true
    )
    private Double rating;

    @DecimalMin(value = "0.001", message = "Weight must be greater than 0")
    @Column(name = "WEIGHT", precision = 10, scale = 3)
    @Schema(
        description = "Product weight in kilograms - must be greater than 0",
        example = "0.187",
        type = SchemaType.NUMBER,
        format = "double",
        minimum = "0.001",
        exclusiveMinimum = true,
        nullable = true
    )
    private Double weight;

    @Column(name = "ACTIVE")
    @Schema(
        description = "Indicates if the product is currently active and available for sale",
        example = "true",
        type = SchemaType.BOOLEAN,
        defaultValue = "true"
    )
    private Boolean active;

    @Size(max = 100)
    @Column(name = "MANUFACTURER", length = 100)
    @Schema(
        description = "Manufacturer or brand name",
        example = "Apple Inc.",
        maxLength = 100,
        nullable = true
    )
    private String manufacturer;

    @Min(value = 0, message = "Warranty months cannot be negative")
    @Max(value = 120, message = "Warranty months cannot exceed 120")
    @Column(name = "WARRANTY_MONTHS")
    @Schema(
        description = "Warranty period in months (0-120)",
        example = "12",
        type = SchemaType.INTEGER,
        format = "int32",
        minimum = "0",
        maximum = "120",
        nullable = true
    )
    private Integer warrantyMonths;

    @Column(name = "CREATED_AT")
    @Schema(
        description = "Timestamp when the product was created",
        example = "2026-01-15T10:30:00",
        type = SchemaType.STRING,
        format = "date-time",
        readOnly = true
    )
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    @Schema(
        description = "Timestamp when the product was last updated",
        example = "2026-02-01T14:20:00",
        type = SchemaType.STRING,
        format = "date-time",
        readOnly = true
    )
    private LocalDateTime updatedAt;

    // Default constructor
    public Product() {
        this.active = true;
        this.stockQuantity = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor with all fields
    public Product(Long id, String name, String description, Double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = true;
        this.stockQuantity = 0;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor without ID (for new entities)
    public Product(String name, String description, Double price) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.active = true;
        this.stockQuantity = 0;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (stockQuantity == null) {
            stockQuantity = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public Integer getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(Integer warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", sku='" + sku + '\'' +
                ", category='" + category + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", rating=" + rating +
                ", weight=" + weight +
                ", active=" + active +
                ", manufacturer='" + manufacturer + '\'' +
                ", warrantyMonths=" + warrantyMonths +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
