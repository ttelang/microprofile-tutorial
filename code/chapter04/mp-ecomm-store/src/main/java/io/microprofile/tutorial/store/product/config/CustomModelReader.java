package io.microprofile.tutorial.store.product.config;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;

/**
 * CustomModelReader to set OpenAPI version and JSON Schema dialect.
 * 
 * Sets OpenAPI 3.1.0 with JSON Schema 2020-12 dialect for improved
 * schema validation features.
 */
public class CustomModelReader implements OASModelReader {
    
    @Override
    public OpenAPI buildModel() {
        return OASFactory.createOpenAPI()
            .openapi("3.1.0")
            .jsonSchemaDialect("https://spec.openapis.org/oas/3.1/dialect/base");
    }
}
