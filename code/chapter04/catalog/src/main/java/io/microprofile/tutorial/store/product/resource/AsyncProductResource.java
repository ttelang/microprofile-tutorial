package io.microprofile.tutorial.store.product.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.enterprise.context.ApplicationScoped;
import io.microprofile.tutorial.store.product.entity.AsyncProductRequest;
import io.microprofile.tutorial.store.product.entity.ProcessResult;
import io.microprofile.tutorial.store.product.entity.ProcessingStatus;
import jakarta.validation.Valid;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

@Path("/products")
@ApplicationScoped
@Tag(name = "Products", description = "Async product operations")
public class AsyncProductResource {

    private static final Logger LOGGER = Logger.getLogger(AsyncProductResource.class.getName());

    @POST
    @Path("/process-async")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Process product asynchronously",
        description = "Initiates asynchronous product processing and calls back when complete"
    )
    @APIResponse(
        responseCode = "202",
        description = "Processing initiated"
    )
    @Callback(
        name = "productProcessed",
        callbackUrlExpression = "{$request.body#/callbackUrl}",
        operations = {
            @CallbackOperation(
                method = "post",
                summary = "Product processing completed",
                description = "Called when async product processing is complete",
                requestBody = @RequestBody(
                    description = "Processing result",
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON,
                        schema = @Schema(implementation = ProcessResult.class)
                    )
                )
            )
        }
    )
    public Response processProductAsync(
        @RequestBody(
            description = "Product data and callback URL",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = AsyncProductRequest.class),
                examples = {
                    @ExampleObject(
                        name = "electronics-product",
                        summary = "Electronics product with callback",
                        description = "Example request with an ELECTRONICS category product",
                        value = """
                            {
                              "product": {
                                "name": "Wireless Mouse",
                                "description": "Ergonomic wireless mouse with USB receiver",
                                "price": 29.99,
                                "sku": "ELC-MS001-BLK",
                                "category": "ELECTRONICS",
                                "stockQuantity": 100,
                                "inStock": true
                              },
                              "callbackUrl": "https://webhook.site/your-unique-id"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "book-product",
                        summary = "Book product with callback",
                        description = "Example request with a BOOKS category product",
                        value = """
                            {
                              "product": {
                                "name": "Design Patterns",
                                "description": "Elements of Reusable Object-Oriented Software",
                                "price": 54.99,
                                "sku": "BOK-DP001-HBK",
                                "category": "BOOKS",
                                "stockQuantity": 50,
                                "inStock": true
                              },
                              "callbackUrl": "https://your-server.com/webhook/product-processed"
                            }
                            """
                    ),
                    @ExampleObject(
                        name = "clothing-product",
                        summary = "Clothing product with callback",
                        description = "Example request with a CLOTHING category product",
                        value = """
                            {
                              "product": {
                                "name": "Cotton T-Shirt",
                                "description": "100% organic cotton t-shirt",
                                "price": 19.99,
                                "sku": "CLO-TS001-MED",
                                "category": "CLOTHING",
                                "stockQuantity": 200,
                                "inStock": true
                              },
                              "callbackUrl": "https://api.example.com/callbacks/products"
                            }
                            """
                    )
                }  
            )
        ) @Valid AsyncProductRequest request
    ) {
        // Validate request
        if (request.getCallbackUrl() == null || request.getProduct() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("{\"error\": \"Missing required fields\"}")
                          .build();
        }
        
        LOGGER.info("Received async product processing request for: " + request.getProduct().getName());
        LOGGER.info("Callback URL: " + request.getCallbackUrl());
        
        // Process asynchronously
        CompletableFuture.runAsync(() -> processAndCallback(request));
        
        return Response.accepted()
                      .entity("{\"message\": \"Processing initiated\", \"callbackUrl\": \"" + request.getCallbackUrl() + "\"}")
                      .build();
    }
    
    /**
     * Process the product asynchronously and send callback when complete.
     * 
     * @param request The async product request with callback URL
     */
    private void processAndCallback(AsyncProductRequest request) {
        ProcessResult result;
        
        try {
            // Simulate processing time (2-5 seconds)
            int processingTime = ThreadLocalRandom.current().nextInt(2000, 5000);
            LOGGER.info("Processing product '" + request.getProduct().getName() + "' - will take " + processingTime + "ms");
            Thread.sleep(processingTime);
            
            // Simulate 90% success rate
            boolean success = ThreadLocalRandom.current().nextInt(100) < 90;
            
            // Generate random product ID
            Long productId = ThreadLocalRandom.current().nextLong(10000, 99999);
            
            if (success) {
                result = new ProcessResult(
                    productId,
                    ProcessingStatus.SUCCESS,
                    "Product '" + request.getProduct().getName() + "' processed successfully",
                    Instant.now()
                );
                LOGGER.info("Product processed successfully: " + productId);
            } else {
                result = new ProcessResult(
                    productId,
                    ProcessingStatus.FAILED,
                    "Product processing failed due to validation error",
                    Instant.now()
                );
                LOGGER.warning("Product processing failed: " + productId);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result = new ProcessResult(
                null,
                ProcessingStatus.FAILED,
                "Processing interrupted: " + e.getMessage(),
                Instant.now()
            );
            LOGGER.severe("Processing interrupted: " + e.getMessage());
        } catch (Exception e) {
            result = new ProcessResult(
                null,
                ProcessingStatus.FAILED,
                "Unexpected error: " + e.getMessage(),
                Instant.now()
            );
            LOGGER.severe("Unexpected error during processing: " + e.getMessage());
        }
        
        // Send callback
        sendCallback(request.getCallbackUrl(), result);
    }
    
    /**
     * Manually serialize ProcessResult to JSON to avoid classloader issues in async context.
     * 
     * <p><b>Implementation Note:</b> We use manual JSON serialization instead of JSON-B
     * because CompletableFuture worker threads don't have access to the CDI container's
     * classloader where JSON-B providers are registered.
     * 
     * @param result The processing result
     * @return JSON string representation
     */
    private String toJson(ProcessResult result) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"productId\":").append(result.getProductId() != null ? result.getProductId() : "null");
        json.append(",\"status\":\"").append(result.getStatus()).append("\"");
        json.append(",\"message\":\"").append(escapeJson(result.getMessage())).append("\"");
        json.append(",\"timestamp\":\"").append(result.getTimestamp().toString()).append("\"");
        json.append("}");
        return json.toString();
    }
    
    /**
     * Escape special characters in JSON strings.
     * 
     * @param value The string to escape
     * @return Escaped string
     */
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    /**
     * Send the processing result to the callback URL using HttpURLConnection.
     * 
     * <p><b>Implementation Note:</b> We use HttpURLConnection instead of MicroProfile Rest Client
     * because RestClientBuilder hangs when invoked from CompletableFuture worker threads.
     * HttpURLConnection works reliably in any thread context without classloader dependencies.
     * 
     * @param callbackUrl The URL to send the callback to
     * @param result The processing result
     */
    private void sendCallback(String callbackUrl, ProcessResult result) {
        HttpURLConnection connection = null;
        
        try {
            LOGGER.info("========================================");
            LOGGER.info("CALLBACK ATTEMPT STARTED");
            LOGGER.info("Callback URL: " + callbackUrl);
            LOGGER.info("Result Status: " + result.getStatus());
            LOGGER.info("Result Message: " + result.getMessage());
            LOGGER.info("Product ID: " + result.getProductId());
            LOGGER.info("Timestamp: " + result.getTimestamp());
            
            // Convert result to JSON manually (avoiding classloader issues)
            LOGGER.info("Converting result to JSON...");
            String jsonPayload = toJson(result);
            LOGGER.info("JSON payload created: " + jsonPayload);
            
            // Create HTTP connection
            LOGGER.info("Creating HTTP connection to: " + callbackUrl);
            connection = (HttpURLConnection) URI.create(callbackUrl).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000);
            LOGGER.info("HTTP connection configured");
            
            // Send JSON payload
            LOGGER.info("Sending JSON payload...");
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }
            LOGGER.info("Payload sent, reading response...");
            
            // Get response code
            int responseCode = connection.getResponseCode();
            LOGGER.info("Response status code: " + responseCode);
            
            if (responseCode >= 200 && responseCode < 300) {
                LOGGER.info("✅ CALLBACK SENT SUCCESSFULLY!");
            } else {
                LOGGER.warning("⚠️ Callback returned non-success status: " + responseCode);
            }
            
            LOGGER.info("========================================");
            
        } catch (Exception e) {
            LOGGER.severe("========================================");
            LOGGER.severe("❌ CALLBACK FAILED!");
            LOGGER.severe("URL: " + callbackUrl);
            LOGGER.severe("Error: " + e.getClass().getName());
            LOGGER.severe("Message: " + e.getMessage());
            LOGGER.severe("Stack trace:");
            e.printStackTrace();
            LOGGER.severe("========================================");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}