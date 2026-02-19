package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

@Schema(name = "CategoryRecord", description = "Product category information")
public record CategoryRecord(
    @NotNull
    @Schema(description = "Category ID", example = "1")
    Long id,
    
    @NotNull
    @Schema(description = "Category name", example = "Electronics")
    String name,
    
    @Schema(description = "Parent category ID for hierarchical structure", nullable = true)
    Long parentId, // Null for root categories
    
    @NotNull
    @Schema(description = "Display order", example = "1")
    Integer displayOrder
) {}