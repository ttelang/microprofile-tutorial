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
 * JAX-RS Application class for Payment Service API - Chapter 5 Enhanced.
 * 
 * This class configures the REST API root path and provides comprehensive
 * OpenAPI documentation using MicroProfile OpenAPI 4.1 annotations.
 * 
 * <h2>Chapter 5 Enhancements:</h2>
 * <ul>
 *   <li><b>Externalized Configuration:</b> Payment gateway, timeouts, and limits via MicroProfile Config</li>
 *   <li><b>Environment Adaptability:</b> Switch between sandbox and production without code changes</li>
 *   <li><b>Configuration Sources:</b> Properties files, environment variables, and custom sources</li>
 *   <li><b>Dynamic Updates:</b> Configuration can be updated without redeploying</li>
 * </ul>
 * 
 * <h2>Payment Service Features (from previous chapters):</h2>
 * <ul>
 *   <li><b>Payment Processing:</b> Process and validate payments with full audit trail</li>
 *   <li><b>Idempotency Support:</b> HTTP PUT-based idempotency to prevent duplicate charges</li>
 *   <li><b>Audit Trail:</b> Comprehensive audit logging for compliance and troubleshooting</li>
 *   <li><b>CDI Interceptors:</b> Automatic method logging with @Logged annotation</li>
 *   <li><b>Card Security:</b> Automatic card number masking in audit logs</li>
 * </ul>
 * 
 * @see <a href="https://github.com/eclipse/microprofile-config">MicroProfile Config</a>
 * @see <a href="https://github.com/eclipse/microprofile-open-api">MicroProfile OpenAPI</a>
 */
@ApplicationPath("/api")
@OpenAPIDefinition(
    info = @Info(
        title = "Payment Service API - Chapter 5: Configuration",
        version = "2.0.0",
        description = """
            ## Payment Service API with MicroProfile Config
            
            **API Specification:** MicroProfile OpenAPI 4.0+ (aligned with OpenAPI 3.1 specification)
            **Configuration:** MicroProfile Config 3.1
            
            This chapter enhances the Payment Service with externalized configuration capabilities,
            allowing the service to adapt to different environments (dev, test, prod) without code changes.
            
            ### Chapter 5: MicroProfile Config Features
            
            #### 1. Configuration Injection
            Configuration properties are injected using `@ConfigProperty`:
            ```java
            @Inject
            @ConfigProperty(name = "payment.gateway.endpoint")
            private String gatewayEndpoint;
            ```
            
            #### 2. Configuration Grouping
            Related properties grouped with `@ConfigProperties`:
            ```java
            @Inject
            private PaymentGatewayConfig gatewayConfig;
            ```
            
            #### 3. Configuration Sources (in priority order)
            - Custom ConfigSource (ordinal: 600)
            - System Properties (ordinal: 400)
            - Environment Variables (ordinal: 300)
            - microprofile-config.properties (ordinal: 100)
            
            #### 4. Environment-Specific Configuration
            - **Development:** Uses sandbox payment gateway
            - **Production:** Uses live payment gateway with strict limits
            - **Testing:** Uses mock gateway with flexible configuration
            
            #### 5. Dynamic Configuration Updates
            Configuration changes can be applied at runtime through the custom ConfigSource,
            allowing operational adjustments without service restarts.
            
            ### Core Payment Features (Chapters 3-4)
            
            #### Payment Processing
            - **Payment Validation**: Comprehensive validation with configured amount limits
            - **Payment Processing**: Secure transaction handling via configured gateway
            - **Refund Support**: Process refunds with full audit trail
            - **Fraud Detection**: Configurable fraud detection rules
            
            #### Idempotency (HTTP PUT Pattern)
            Prevents duplicate charges using RESTful idempotency:
            - **Client-Provided ID**: Client supplies unique payment ID in URL path
            - **Automatic Deduplication**: Same ID returns cached result without reprocessing
            - **Conflict Detection**: Returns 409 if same ID used with different payment details
            - **24-Hour Cache**: Idempotency records expire after 24 hours
            - **HTTP PUT Semantics**: Follows RFC 7231 idempotency standard
            
            #### Audit Trail
            Complete audit logging for compliance:
            - **Every Operation Logged**: All payment operations recorded with timestamp
            - **Card Number Masking**: Automatic PCI-DSS compliant masking (shows last 4 digits)
            - **Query by Payment ID**: Find all operations for a specific payment
            - **Query by Operation Type**: Filter by PAYMENT_PROCESS, IDEMPOTENT_PAYMENT, etc.
            - **Query by Status**: Find SUCCESS, FAILED, CONFLICT, etc.
            
            #### Automatic Logging (CDI Interceptors)
            - **@Logged Annotation**: Automatic method entry/exit logging
            - **Execution Timing**: Measures method execution time automatically
            - **Parameter Tracking**: Logs method parameters (with sensitive data masking)
            - **Exception Handling**: Captures and logs exceptions with stack traces
            
            ### Configuration Examples
            
            Set via environment variables (highest priority):
            ```bash
            export PAYMENT_GATEWAY_ENDPOINT=https://api.paymentgateway.com
            export PAYMENT_FRAUD_DETECTION_ENABLED=true
            export PAYMENT_MAXIMUM_AMOUNT=10000.00
            ```
            
            Or via microprofile-config.properties:
            ```properties
            payment.gateway.endpoint=https://sandbox.paymentgateway.com
            payment.gateway.timeout=30000
            payment.fraud.detection.enabled=false
            payment.minimum.amount=0.01
            payment.maximum.amount=10000.00
            ```
            
            ### Testing the API
            Use the interactive documentation below to explore and test all endpoints.
            Start with `/payments/validate` to test payment validation with configured limits.
            Try the `/payment-config` endpoint to view current configuration values.
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
                Payment processing operations with externalized configuration.
                
                All payment operations use configured gateway settings and limits.
                Configuration can be updated without code changes through environment
                variables or configuration files.
                """
        ),
        @Tag(
            name = "Audit",
            description = """
                Audit trail operations for querying payment history and compliance reporting.
                
                Audit records include masked card numbers for PCI-DSS compliance.
                """
        ),
        @Tag(
            name = "Configuration",
            description = """
                Configuration management endpoints to view and update payment service configuration.
                
                Demonstrates MicroProfile Config capabilities including ConfigValue,
                ConfigSource priority, and dynamic updates.
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
        description = "MicroProfile Tutorial - Chapter 5: Configuration",
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
