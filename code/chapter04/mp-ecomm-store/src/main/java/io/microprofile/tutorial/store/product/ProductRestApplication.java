package io.microprofile.tutorial.store.product;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.info.License;
import org.eclipse.microprofile.openapi.annotations.servers.Server;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlows;
import org.eclipse.microprofile.openapi.annotations.security.OAuthFlow;
import org.eclipse.microprofile.openapi.annotations.security.OAuthScope;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application class for Product Catalog API demonstrating MicroProfile OpenAPI 4.1 features.
 * 
 * This class serves as the root configuration for the REST API and provides comprehensive
 * OpenAPI documentation using annotations.
 * 
 * <h2>MicroProfile OpenAPI 4.1 Features Demonstrated:</h2>
 * <ul>
 *   <li><b>OpenAPI v3.1 Alignment:</b> Full JSON Schema 2020-12 compatibility</li>
 *   <li><b>Rich Documentation:</b> Detailed API descriptions with markdown support</li>
 *   <li><b>Multiple Security Schemes:</b> API Key, Bearer JWT, and OAuth2 examples</li>
 *   <li><b>Tag Organization:</b> Logical grouping of endpoints (Products, Webhooks)</li>
 *   <li><b>External Documentation:</b> Links to specifications and references</li>
 *   <li><b>Server Configuration:</b> Flexible URL configuration for different environments</li>
 * </ul>
 * 
 * <h2>Key Improvements in OpenAPI 3.1:</h2>
 * <ol>
 *   <li>JSON Schema 2020-12 support (better validation)</li>
 *   <li>Native nullable handling (no more allOf workarounds)</li>
 *   <li>Webhook support (new in OpenAPI 3.1)</li>
 *   <li>Improved numeric constraints (exclusiveMinimum as numeric)</li>
 *   <li>Better pattern validation with full regex support</li>
 * </ol>
 * 
 * @see <a href="https://spec.openapis.org/oas/v3.1.0">OpenAPI v3.1 Specification</a>
 * @see <a href="https://github.com/eclipse/microprofile-open-api">MicroProfile OpenAPI</a>
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "Product Catalog API",
        version = "1.0.0",
        description = """
            ## Product Catalog API with Async Callback Support
            This API demonstrates MicroProfile OpenAPI 4.1 features aligned with OpenAPI v3.1 specification.

            ### Key Features:
            
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
            url = "https://microprofile.io"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(
            url = "/mp-ecomm-store",
            description = "Product Catalog API server (works with localhost, Codespaces, and production)"
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
        ),
        @Tag(
            name = "Webhooks",
            description = """
                Webhook subscription management for receiving real-time product event notifications.
                
                **OpenAPI 3.1 Feature**: Webhooks are a new feature in OpenAPI 3.1 that allows documenting
                reverse APIs where the server calls the client.
                
                Subscribe to events like product.created, product.updated, etc. to receive notifications
                at your specified callback URL.
                """,
            externalDocs = @ExternalDocumentation(
                description = "Webhook Best Practices",
                url = "https://docs.github.com/webhooks"
            )
        )
    },
    externalDocs = @ExternalDocumentation(
        description = "MicroProfile OpenAPI Specification",
        url = "https://github.com/eclipse/microprofile-open-api"
    )
)
// Security schemes defined for OpenAPI documentation purposes
// These demonstrate how to document different authentication methods
// Note: Actual security implementation would require additional configuration
@SecuritySchemes({
    @SecurityScheme(
        securitySchemeName = "apiKey",
        type = SecuritySchemeType.APIKEY,
        description = "API Key authentication",
        in = SecuritySchemeIn.HEADER,
        apiKeyName = "X-API-Key"
    ),
    @SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        description = "JWT Bearer token authentication",
        scheme = "bearer",
        bearerFormat = "JWT"
    ),
    @SecurityScheme(
        securitySchemeName = "oauth2",
        type = SecuritySchemeType.OAUTH2,
        description = "OAuth2 authentication (example URLs - replace with actual OAuth provider)",
        flows = @OAuthFlows(
            authorizationCode = @OAuthFlow(
                authorizationUrl = "https://example.com/oauth/authorize",
                tokenUrl = "https://example.com/oauth/token",
                scopes = {
                    @OAuthScope(name = "read:products", description = "Read product information"),
                    @OAuthScope(name = "write:products", description = "Modify product information")
                }
            )
        )
    )
})
public class ProductRestApplication extends Application {
    // Rest application class with comprehensive OpenAPI definition
}