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
 * Application class demonstrating MicroProfile OpenAPI 4.1 features.
 * 
 * MicroProfile OpenAPI 4.1 aligns with OpenAPI v3.1 specification, which includes:
 * 1. Full JSON Schema 2020-12 compatibility
 * 2. Improved nullable handling (native JSON Schema support)
 * 3. Better numeric constraints (exclusiveMinimum as numeric value)
 * 4. Enhanced pattern validation with full regex support
 * 5. Support for webhooks (event-driven APIs)
 * 6. Richer metadata and documentation capabilities
 * 7. Better security scheme definitions
 * 8. Support for multiple examples per schema
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "MicroProfile Catalog Service API",
        version = "3.1.0",
        description = """
            ## Catalog Service API - OpenAPI v3.1 Demonstration
            
            This API demonstrates **MicroProfile OpenAPI 4.1** alignment with **OpenAPI v3.1** 
            and **JSON Schema 2020-12**.
            
            ### Key Features Demonstrated:
            
            #### 1. JSON Schema 2020-12 Validation
            - **Pattern Validation**: Regex-based string validation (SKU format: `^[A-Z]{3}-[A-Z0-9]+-[A-Z0-9]+$`)
            - **Numeric Constraints**: 
              - `minimum` and `maximum` (inclusive bounds)
              - `exclusiveMinimum` and `exclusiveMaximum` (exclusive bounds)
              - `multipleOf` for decimal precision (e.g., price rounded to $0.01)
            - **String Constraints**: `minLength`, `maxLength`, `pattern`
            - **Array Constraints**: `minItems`, `maxItems`, `uniqueItems`
            
            #### 2. Format Specifications
            Standardized formats for validation:
            - Numeric: `double`, `float`, `int32`, `int64`
            - Date/Time: `date-time`, `date`, `time`
            - Other: `email`, `uri`, `uuid`, `binary`, `byte`
            
            #### 3. Enhanced Schema Features
            - **Nullable**: Proper handling of null values (aligned with JSON Schema)
            - **Enumeration**: Type-safe enum values (e.g., product categories)
            - **Default Values**: Schema-level defaults (e.g., `active: true`, `stockQuantity: 0`)
            - **Read-only**: Properties that cannot be modified (e.g., `id`, timestamps)
            - **Examples**: Rich examples for better documentation
            
            #### 4. Advanced API Operations
            - Full CRUD operations with detailed response schemas
            - Search and filter with validated query parameters
            - Pagination with constraint validation
            - Bulk operations demonstrating array schemas
            
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
            url = "/catalog",
            description = "Catalog API server (works with localhost, Codespaces, and production)"
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
            name = "Categories",
            description = "Product category operations with enum-based type-safe category management"
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
        ),
        @Tag(
            name = "Async Operations",
            description = """
                Asynchronous product processing operations with callback-style webhooks.
                
                Submit products for async processing and receive results via HTTP callback to your URL.
                Useful for long-running operations that exceed HTTP timeout limits.
                """
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
    // JAX-RS application class with comprehensive OpenAPI definition
}