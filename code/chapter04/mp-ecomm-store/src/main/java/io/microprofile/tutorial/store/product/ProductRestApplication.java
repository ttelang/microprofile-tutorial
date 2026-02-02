package io.microprofile.tutorial.store.product;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Application class demonstrating MicroProfile OpenAPI 4.1 features.
 * 
 * MicroProfile OpenAPI 4.1 aligns with OpenAPI v3.1 specification, which includes:
 * 1. Full JSON Schema 2020-12 compatibility
 * 2. Improved nullable handling (native JSON Schema support)
 * 3. Better numeric constraints (exclusiveMinimum as numeric value)
 * 4. Enhanced pattern validation with full regex support
 * 5. Richer metadata and documentation capabilities
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "MicroProfile E-Commerce Store API",
        version = "3.1.0",
        description = """
            ## E-Commerce Store API - OpenAPI v3.1 Demonstration
            
            This API demonstrates **MicroProfile OpenAPI 4.1** alignment with **OpenAPI v3.1** 
            and **JSON Schema 2020-12**.
            
            ### Key Features Demonstrated:
            
            #### 1. JSON Schema 2020-12 Validation
            - **Pattern Validation**: Regex-based string validation (SKU format: `^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$`)
            - **Numeric Constraints**: 
              - `minimum` and `maximum` (inclusive bounds)
              - `exclusiveMinimum` (exclusive bounds - price > $0.01)
              - `multipleOf` for decimal precision (prices rounded to $0.01)
            - **String Constraints**: `minLength`, `maxLength`, `pattern`
            - **Array Constraints**: `minItems`, `maxItems`
            
            #### 2. Format Specifications
            Standardized formats for validation:
            - Numeric: `double`, `int32`, `int64`
            - Strings with format validation
            
            #### 3. Enhanced Schema Features
            - **Nullable**: Proper handling of null values (aligned with JSON Schema)
            - **Enumeration**: Type-safe enum values (e.g., product categories)
            - **Default Values**: Schema-level defaults (e.g., `inStock: true`, `stockQuantity: 0`)
            - **Read-only**: Properties that cannot be modified (e.g., `id`)
            - **Examples**: Rich examples for better documentation
            
            #### 4. API Operations
            - Get all products with array schema constraints
            - Get product by ID with validated path parameter
            - Create new product with full schema validation
            - Search with multiple filters and pagination
            
            ### Benefits of OpenAPI v3.1
            - **Single Source of Truth**: Same schema for OpenAPI docs and JSON validation
            - **Better Tooling**: Leverage existing JSON Schema ecosystem
            - **More Precise Validation**: Richer constraint expressions
            - **Standard Compliance**: Full JSON Schema 2020-12 support
            - **Improved Interoperability**: Works with any JSON Schema tool
            - **Code Generation**: Better type safety in generated clients
            
            ### Testing the API
            Use the interactive documentation below to explore and test all endpoints.
            """,
        contact = @Contact(
            name = "MicroProfile Tutorial Team",
            url = "https://microprofile.io",
            email = "tutorial@microprofile.io"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            url = "/mp-ecomm-store",
            description = "E-Commerce Store API server (works with localhost, Codespaces, and production)"
        )
    },
    tags = {
        @Tag(
            name = "Products",
            description = "Product catalog operations with full JSON Schema 2020-12 validation",
            externalDocs = @ExternalDocumentation(
                description = "OpenAPI v3.1 Specification",
                url = "https://spec.openapis.org/oas/v3.1.0"
            )
        )
    },
    externalDocs = @ExternalDocumentation(
        description = "MicroProfile OpenAPI Specification",
        url = "https://github.com/eclipse/microprofile-open-api"
    )
)
public class ProductRestApplication extends Application {
    // Rest application class with comprehensive OpenAPI definition
}
