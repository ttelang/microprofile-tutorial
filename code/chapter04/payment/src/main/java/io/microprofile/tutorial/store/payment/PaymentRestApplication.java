package io.microprofile.tutorial.store.payment;

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

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application class for Payment Service API.
 * 
 * This class configures the REST API root path and provides comprehensive
 * OpenAPI documentation using MicroProfile OpenAPI 4.1 annotations.
 * 
 * <h2>Payment Service Features:</h2>
 * <ul>
 *   <li><b>Payment Processing:</b> Process and validate payments with full audit trail</li>
 *   <li><b>Idempotency Support:</b> HTTP PUT-based idempotency to prevent duplicate charges</li>
 *   <li><b>Audit Trail:</b> Comprehensive audit logging for compliance and troubleshooting</li>
 *   <li><b>CDI Interceptors:</b> Automatic method logging with @Logged annotation</li>
 *   <li><b>Card Security:</b> Automatic card number masking in audit logs</li>
 * </ul>
 * 
 * @see <a href="https://github.com/eclipse/microprofile-open-api">MicroProfile OpenAPI</a>
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "Payment Service API",
        version = "1.0.0",
        description = """
            ## Payment Service API with Idempotency and Audit Trail
            
            **API Specification:** MicroProfile OpenAPI 4.0+ (aligned with OpenAPI 3.1 specification)
            
            This API demonstrates enterprise payment processing patterns with modern OpenAPI 3.1 features including:
            - **JSON Schema 2020-12**: Advanced validation with `exclusiveMinimum`, `multipleOf`, and rich pattern support
            - **Native Nullable Handling**: Proper null value semantics for optional fields
            - **Enhanced Numeric Constraints**: Precise validation for monetary amounts
            
            ### Core Features
            
            #### 1. Payment Processing
            - **Payment Validation**: Comprehensive validation of card details and amounts
            - **Payment Processing**: Secure payment transaction handling
            - **Refund Support**: Process refunds with full audit trail
            
            #### 2. Idempotency (HTTP PUT Pattern)
            Prevents duplicate charges using RESTful idempotency:
            - **Client-Provided ID**: Client supplies unique payment ID in URL path
            - **Automatic Deduplication**: Same ID returns cached result without reprocessing
            - **Conflict Detection**: Returns 409 if same ID used with different payment details
            - **24-Hour Cache**: Idempotency records expire after 24 hours
            - **HTTP PUT Semantics**: Follows RFC 7231 idempotency standard
            
            Example:
            ```
            PUT /api/payments/payment-12345
            { "cardNumber": "4111...", "amount": 99.99, ... }
            ```
            
            #### 3. Audit Trail
            Complete audit logging for compliance:
            - **Every Operation Logged**: All payment operations recorded with timestamp
            - **Card Number Masking**: Automatic PCI-DSS compliant masking (shows last 4 digits)
            - **Query by Payment ID**: Find all operations for a specific payment
            - **Query by Operation Type**: Filter by PAYMENT_PROCESS, IDEMPOTENT_PAYMENT, etc.
            - **Query by Status**: Find SUCCESS, FAILED, CONFLICT, etc.
            
            #### 4. Automatic Logging (CDI Interceptors)
            - **@Logged Annotation**: Automatic method entry/exit logging
            - **Execution Timing**: Measures method execution time automatically
            - **Parameter Tracking**: Logs method parameters (with sensitive data masking)
            - **Exception Handling**: Captures and logs exceptions with stack traces
            
            ### Security
            - Card numbers automatically masked in logs (e.g., `****1234`)
            - Bearer token authentication support (future enhancement)
            - Secure error messages (no sensitive data exposure)
            
            ### Testing the API
            Use the interactive documentation below to explore and test all endpoints.
            Start with `/payments/validate` to test payment validation.
            """,
        contact = @Contact(
            name = "MicroProfile Tutorial Team",
            url = "https://microprofile.io",
            email = "support@microprofile.io"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "https://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    tags = {
        @Tag(
            name = "Payments",
            description = """
                Payment processing operations including validation, processing, refunds, and idempotent payments.
                
                All payment operations are automatically logged using CDI interceptors (@Logged annotation).
                """
        ),
        @Tag(
            name = "Audit",
            description = """
                Audit trail operations for querying payment history and compliance reporting.
                
                Audit records include masked card numbers for PCI-DSS compliance.
                """
        )
    },
    servers = {
        @Server(
            url = "/payment",
            description = "Payment API server (works with localhost, Codespaces, and production)"
        )
    },
    externalDocs = @ExternalDocumentation(
        description = "MicroProfile Tutorial - Chapter 4: Payment Service",
        url = "https://github.com/microprofile/microprofile-tutorial"
    )
)
@SecuritySchemes({
    @SecurityScheme(
        securitySchemeName = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token authentication (future enhancement)"
    ),
    @SecurityScheme(
        securitySchemeName = "apiKey",
        type = SecuritySchemeType.APIKEY,
        apiKeyName = "X-API-Key",
        in = SecuritySchemeIn.HEADER,
        description = "API Key authentication (future enhancement)"
    )
})
public class PaymentRestApplication extends Application {
    // OpenAPI documentation is configured via annotations above
}
