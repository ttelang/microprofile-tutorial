package io.microprofile.tutorial.store.product.resource;

import io.microprofile.tutorial.store.product.entity.ProductEvent;
import io.microprofile.tutorial.store.product.entity.WebhookSubscription;
import io.microprofile.tutorial.store.product.service.WebhookService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Webhook Resource demonstrating MicroProfile OpenAPI 4.1 callback support.
 * 
 * Uses @Callback annotations to document webhook events that this API will send
 * to client-provided URLs when product events occur.
 * 
 * The @Callback annotation documents the HTTP requests that this API will make to
 * client-provided webhook URLs when product events occur.
 */
@Path("/webhooks")
@ApplicationScoped
@Tag(
    name = "Webhooks",
    description = """
        Manage webhook subscriptions for product event notifications.
        
        Subscribe to receive HTTP POST callbacks when products are created, updated, deleted,
        or when stock levels change. Each subscription includes a secret for verifying webhook signatures.
        
        **Events Available**:
        - product.created - New product added to catalog
        - product.updated - Product details modified
        - product.deleted - Product removed from catalog
        - product.stock.low - Inventory below threshold
        - product.stock.out - Product sold out
        """
)
public class WebhookResource {
    
    @Inject
    WebhookService webhookService;
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Subscribe to product webhook events",
        description = """
            Create a webhook subscription to receive real-time product event notifications.
            
            ## How It Works
            1. POST your subscription with a valid HTTPS callback URL
            2. Receive a unique secret for verifying webhook signatures
            3. Your callback URL will receive POST requests when subscribed events occur
            
            ## Security
            - All webhook deliveries include `X-Webhook-Signature` header
            - Signature format: `HMAC-SHA256(secret, request_body)` in hex
            - Always verify signatures to ensure request authenticity
            
            ## Retry Policy
            - Failed deliveries (5xx responses) trigger automatic retry
            - Exponential backoff: 1s, 2s, 4s, 8s, 16s
            - After 5 failures, subscription is marked inactive
            """
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Subscription created - Save the secret to verify incoming webhooks",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = WebhookSubscription.class),
                examples = @ExampleObject(
                    name = "created",
                    summary = "Successful subscription",
                    value = """
                        {
                          "id": "sub_7x9k2m4n6p",
                          "callbackUrl": "https://myapp.example.com/webhooks/products",
                          "events": ["product.created", "product.updated"],
                          "secret": "whs_a1b2c3d4e5f6g7h8i9j0",
                          "active": true,
                          "createdAt": "2026-02-08T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid subscription - URL must use HTTPS and events must be valid"
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized - API key required")
    })
    // This is the key feature: @Callback documents all webhook events using annotations only
    @Callback(
        name = "productEvents",
        callbackUrlExpression = "{$request.body#/callbackUrl}",
        operations = {
            // Event 1: Product Created
            @CallbackOperation(
                method = "post",
                summary = "Product Created Webhook",
                description = """
                    Triggered when: POST /products creates a new product
                    
                    **Headers:**
                    - `X-Webhook-Signature`: HMAC-SHA256 signature for verification
                    - `X-Event-Type`: product.created
                    - `X-Event-ID`: Unique event identifier
                    
                    **Expected Response:** 200 OK to acknowledge receipt
                    """,
                requestBody = @RequestBody(
                    description = "Product creation event payload",
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(implementation = ProductEvent.class),
                        examples = @ExampleObject(
                            name = "created-event",
                            summary = "New product created",
                            value = """
                                {
                                  "eventId": "evt_abc123xyz",
                                  "eventType": "product.created",
                                  "timestamp": "2026-02-08T10:35:12Z",
                                  "product": {
                                    "id": 42,
                                    "name": "Wireless Keyboard",
                                    "description": "Mechanical RGB keyboard",
                                    "price": 89.99,
                                    "category": "ELECTRONICS",
                                    "tags": ["wireless", "RGB", "mechanical"]
                                  },
                                  "metadata": {
                                    "source": "api",
                                    "userId": "user@example.com",
                                    "ipAddress": "203.0.113.42"
                                  }
                                }
                                """
                        )
                    )
                ),
                responses = {
                    @APIResponse(
                        responseCode = "200",
                        description = "Webhook acknowledged - Event processed successfully",
                        headers = @Header(
                            name = "X-Webhook-Signature",
                            description = "HMAC-SHA256 signature for verification",
                            schema = @Schema(type = SchemaType.STRING, example = "sha256=a1b2c3d4e5...")
                        )
                    ),
                    @APIResponse(
                        responseCode = "5xx",
                        description = "Processing failed - Will retry with exponential backoff"
                    )
                }
            ),
            
            // Event 2: Product Updated
            @CallbackOperation(
                method = "post",
                summary = "Product Updated Webhook",
                description = """
                    Triggered when: PUT /products/{id} modifies an existing product
                    
                    **Includes:** Only changed fields in the product object
                    **Metadata:** Lists which fields were modified
                    """,
                requestBody = @RequestBody(
                    description = "Product update event payload",
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(implementation = ProductEvent.class),
                        examples = @ExampleObject(
                            name = "updated-event",
                            summary = "Product price updated",
                            value = """
                                {
                                  "eventId": "evt_def456ghi",
                                  "eventType": "product.updated",
                                  "timestamp": "2026-02-08T11:20:33Z",
                                  "product": {
                                    "id": 42,
                                    "name": "Wireless Keyboard",
                                    "price": 79.99
                                  },
                                  "metadata": {
                                    "changedFields": ["price"],
                                    "previousPrice": 89.99,
                                    "userId": "admin@example.com"
                                  }
                                }
                                """
                        )
                    )
                ),
                responses = {
                    @APIResponse(responseCode = "200", description = "Event acknowledged"),
                    @APIResponse(responseCode = "5xx", description = "Will retry")
                }
            ),
            
            // Event 3: Product Deleted
            @CallbackOperation(
                method = "post",
                summary = "Product Deleted Webhook",
                description = """
                    Triggered when: DELETE /products/{id} removes a product
                    
                    **Note:** Product object contains minimal data (only ID)
                    """,
                requestBody = @RequestBody(
                    description = "Product deletion event payload",
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(implementation = ProductEvent.class),
                        examples = @ExampleObject(
                            name = "deleted-event",
                            summary = "Product removed from catalog",
                            value = """
                                {
                                  "eventId": "evt_jkl789mno",
                                  "eventType": "product.deleted",
                                  "timestamp": "2026-02-08T12:05:18Z",
                                  "product": {
                                    "id": 42
                                  },
                                  "metadata": {
                                    "reason": "discontinued",
                                    "deletedBy": "admin@example.com"
                                  }
                                }
                                """
                        )
                    )
                ),
                responses = {
                    @APIResponse(responseCode = "200", description = "Event acknowledged"),
                    @APIResponse(responseCode = "5xx", description = "Will retry")
                }
            ),
            
            // Event 4: Stock Low
            @CallbackOperation(
                method = "post",
                summary = "Low Stock Alert Webhook",
                description = """
                    Triggered when: Product inventory falls below threshold (default: 10 units)
                    
                    **Use Case:** Trigger reorder workflows, display low stock warnings
                    **Frequency:** Only sent once when threshold is crossed
                    """,
                requestBody = @RequestBody(
                    description = "Low stock alert payload",
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(implementation = ProductEvent.class),
                        examples = @ExampleObject(
                            name = "stock-low-event",
                            summary = "Inventory running low",
                            value = """
                                {
                                  "eventId": "evt_pqr012stu",
                                  "eventType": "product.stock.low",
                                  "timestamp": "2026-02-08T13:45:22Z",
                                  "product": {
                                    "id": 42,
                                    "name": "Wireless Keyboard"
                                  },
                                  "metadata": {
                                    "currentStock": 7,
                                    "threshold": 10,
                                    "recommendedReorder": 50
                                  }
                                }
                                """
                        )
                    )
                ),
                responses = {
                    @APIResponse(responseCode = "200", description = "Alert acknowledged"),
                    @APIResponse(responseCode = "5xx", description = "Will retry")
                }
            ),
            
            // Event 5: Out of Stock
            @CallbackOperation(
                method = "post",
                summary = "Out of Stock Webhook",
                description = """
                    Triggered when: Product inventory reaches zero
                    
                    **Use Case:** Update product availability, trigger urgent restock
                    **Priority:** HIGH - requires immediate attention
                    """,
                requestBody = @RequestBody(
                    description = "Out of stock alert payload",
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(implementation = ProductEvent.class),
                        examples = @ExampleObject(
                            name = "stock-out-event",
                            summary = "Product sold out",
                            value = """
                                {
                                  "eventId": "evt_vwx345yz",
                                  "eventType": "product.stock.out",
                                  "timestamp": "2026-02-08T14:12:09Z",
                                  "product": {
                                    "id": 42,
                                    "name": "Wireless Keyboard"
                                  },
                                  "metadata": {
                                    "lastSoldAt": "2026-02-08T14:12:05Z",
                                    "totalSoldToday": 25,
                                    "backorderAvailable": true
                                  }
                                }
                                """
                        )
                    )
                ),
                responses = {
                    @APIResponse(responseCode = "200", description = "Alert acknowledged"),
                    @APIResponse(responseCode = "5xx", description = "Will retry")
                }
            )
        }
    )
    public Response subscribe(
        @Valid
        @RequestBody(
            description = "Webhook subscription configuration",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = WebhookSubscription.class),
                examples = @ExampleObject(
                    name = "subscribe-request",
                    summary = "Subscribe to multiple events",
                    value = """
                        {
                          "callbackUrl": "https://myapp.example.com/webhooks/products",
                          "events": [
                            "product.created",
                            "product.updated",
                            "product.deleted",
                            "product.stock.low",
                            "product.stock.out"
                          ],
                          "active": true
                        }
                        """
                )
            )
        )
        WebhookSubscription subscription
    ) {
        WebhookSubscription created = webhookService.subscribe(subscription);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "List webhook subscriptions",
        description = "Retrieve all active and inactive webhook subscriptions"
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Subscription list retrieved",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = WebhookSubscription.class
                )
            )
        ),
        @APIResponse(responseCode = "401", description = "Unauthorized")
    })
    public Response listSubscriptions() {
        return Response.ok(webhookService.getSubscriptions()).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get webhook subscription",
        description = "Retrieve a specific webhook subscription by ID"
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Subscription found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = WebhookSubscription.class)
            )
        ),
        @APIResponse(responseCode = "404", description = "Subscription not found"),
        @APIResponse(responseCode = "401", description = "Unauthorized")
    })
    public Response getSubscription(
        @Parameter(
            description = "Subscription ID",
            required = true,
            example = "sub_7x9k2m4n6p"
        )
        @PathParam("id") String id
    ) {
        WebhookSubscription subscription = webhookService.getSubscription(id);
        if (subscription == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(subscription).build();
    }
    
    @DELETE
    @Path("/{id}")
    @Operation(
        summary = "Delete webhook subscription",
        description = "Unsubscribe from webhook events by deleting the subscription"
    )
    @SecurityRequirement(name = "bearerAuth")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "Subscription deleted"),
        @APIResponse(responseCode = "404", description = "Subscription not found"),
        @APIResponse(responseCode = "401", description = "Unauthorized")
    })
    public Response deleteSubscription(
        @Parameter(
            description = "Subscription ID",
            required = true,
            example = "sub_7x9k2m4n6p"
        )
        @PathParam("id") String id
    ) {
        boolean deleted = webhookService.deleteSubscription(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.noContent().build();
    }
}
