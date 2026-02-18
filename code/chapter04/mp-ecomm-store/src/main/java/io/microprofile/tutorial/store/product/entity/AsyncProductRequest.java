package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to process a product asynchronously with callback notification.
 * 
 * <p>When submitted to the async processing endpoint, the product will be
 * validated and processed in the background. Upon completion, the processing
 * result will be POSTed to the specified callback URL.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Async product processing request with callback URL")
public class AsyncProductRequest {
    
    @Valid
    @NotNull(message = "Product is required")
    @Schema(description = "Product to process", implementation = Product.class)
    private Product product;
    
    @NotBlank(message = "Callback URL is required")
    @Pattern(
        regexp = "^https?://.*",
        message = "Callback URL must be a valid HTTP or HTTPS URL"
    )
    @Schema(
        description = "URL to call when processing completes (must be HTTP or HTTPS)",
        format = "uri"
    )
    private String callbackUrl;
}