package io.microprofile.tutorial.store.product.config;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;

/* 
 * Custom OASModelReader to set OpenAPI version and JSON Schema dialect.
 */
public class CustomModelReader implements OASModelReader {
    
    @Override
    public OpenAPI buildModel() {
        return OASFactory.createOpenAPI()
            .openapi("3.1.0")
            .jsonSchemaDialect("https://spec.openapis.org/oas/3.1/dialect/base")
            .info(OASFactory.createInfo()
                .title("Product Catalog API")
                .version("1.0.0")
                .description("API for managing products in the e-commerce store"));
    }
}
