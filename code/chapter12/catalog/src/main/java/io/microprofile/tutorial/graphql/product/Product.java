package io.microprofile.tutorial.graphql.product;

import org.eclipse.microprofile.graphql.Type;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product entity representing a product in the catalog
 */
@Type("Product")
@Description("A product in the catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    
    @Description("Unique product identifier")
    private Long id;
    
    @NonNull
    @Description("Product name")
    private String name;
    
    @Description("Product description")
    private String description;
    
    @NonNull
    @Description("Product price in USD")
    private Double price;
    
    @Description("Product category")
    private String category;
    
    @Description("Stock quantity available")
    private Integer stockQuantity;
    
    // Computed field - price with tax
    public Double getPriceWithTax() {
        return price != null ? price * 1.08 : null; // 8% tax
    }
    
    // Computed field - availability status
    public String getAvailabilityStatus() {
        if (stockQuantity == null || stockQuantity == 0) {
            return "OUT_OF_STOCK";
        } else if (stockQuantity < 10) {
            return "LOW_STOCK";
        } else {
            return "IN_STOCK";
        }
    }
}
