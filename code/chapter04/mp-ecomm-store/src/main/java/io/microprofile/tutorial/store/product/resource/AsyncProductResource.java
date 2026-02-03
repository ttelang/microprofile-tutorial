package io.microprofile.tutorial.store.product.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.media.Content;
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

@Path("/products")
@ApplicationScoped
@Tag(name = "Products", description = "Async product operations")
public class AsyncProductResource {

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
                schema = @Schema(implementation = AsyncProductRequest.class)
            )
        ) AsyncProductRequest request
    ) {
        // Validate request
        if (request.getCallbackUrl() == null || request.getProduct() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                          .entity("Missing required fields")
                          .build();
        }
        
        // Initiate async processing (for demo, just return accepted)
        // In real implementation, you would trigger async processing here
        
        return Response.accepted()
                      .entity("{\"message\": \"Processing initiated\"}")
                      .build();
    }
}