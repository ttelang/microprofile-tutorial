package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Async product processing request")
public class AsyncProductRequest {
    
    @Valid
    @NotNull
    @Schema(description = "Product to process", required = true)
    private Product product;
    
    @NotNull
    @Schema(
        description = "URL to call when processing completes", 
        required = true,
        example = "https://client.example.com/webhooks/product-processed"
    )
    private String callbackUrl;
}