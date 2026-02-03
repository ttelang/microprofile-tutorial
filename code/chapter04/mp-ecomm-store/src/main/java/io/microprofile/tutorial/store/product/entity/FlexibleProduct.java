package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.media.SchemaProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import java.util.Map;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    description = "Product with flexible custom attributes",
    properties = {
        @SchemaProperty(name = "tags")
    }
)
public class FlexibleProduct {
    @Schema(description = "Product ID", example = "1")
    private Long id;
    
    @Schema(description = "Product name", example = "Wireless Mouse")
    private String name;
    
    @Schema(description = "Product price", example = "29.99")
    private Double price;
    
    @Schema(
        description = "Product specifications (e.g., color, material, size)",
        type = SchemaType.OBJECT,
        example = "{\"color\": \"black\", \"material\": \"plastic\", \"weight\": \"100g\"}"
    )
    private Map<String, Object> specifications;
    
    @Schema(
        description = "Product tags for categorization and search",
        type = SchemaType.ARRAY,
        implementation = String.class,
        example = "[\"wireless\", \"electronics\", \"accessories\"]"
    )
    private List<String> tags;
}