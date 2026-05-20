package io.microprofile.tutorial.store.product.resource;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.service.ProductService;
import io.microprofile.tutorial.store.interceptor.Logged;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.callbacks.Callback;
import org.eclipse.microprofile.openapi.annotations.callbacks.CallbackOperation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.Optional;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/products")
@Logged
public class ProductResource {

    private static final Logger LOGGER = Logger.getLogger(ProductResource.class.getName());
    
    @Inject
    private ProductService productService;

    public ProductResource(ProductService productService) {
        this.productService = productService;
    }
    
    // No-args constructor for tests
    public ProductResource() {
        this.productService = new ProductService();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProducts() {
        LOGGER.info("Fetching all products");
        return Response.ok(productService.getAllProducts()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductById(@PathParam("id") Long id) {
        LOGGER.info("Fetching product with id: " + id);
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            return Response.ok(product.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createProduct(Product product) {
        LOGGER.info("Creating product: " + product);
        Product createdProduct = productService.createProduct(product);
        return Response.status(Response.Status.CREATED).entity(createdProduct).build();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(@PathParam("id") Long id, Product updatedProduct) {
        LOGGER.info("Updating product with id: " + id);
        Optional<Product> product = productService.updateProduct(id, updatedProduct);
        if (product.isPresent()) {
            return Response.ok(product.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProduct(@PathParam("id") Long id) {
        LOGGER.info("Deleting product with id: " + id);
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/async-process")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Process product asynchronously",
        description = "Initiates async product processing and calls back when complete (MicroProfile OpenAPI 4.1 async feature)"
    )
    @Callback(
        name = "productProcessed",
        callbackUrlExpression = "{$request.body#/callbackUrl}",
        operations = {
            @CallbackOperation(
                method = "post",
                summary = "Product processing completed",
                requestBody = @RequestBody(
                    description = "Processing result",
                    content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = io.microprofile.tutorial.store.product.entity.ProcessResult.class)
                    )
                )
            )
        }
    )
    @APIResponse(
        responseCode = "202",
        description = "Processing initiated"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid request"
    )
    public Response processProductAsync(
        @RequestBody(
            description = "Product and callback URL",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = io.microprofile.tutorial.store.product.entity.AsyncRequest.class)
            )
        ) io.microprofile.tutorial.store.product.entity.AsyncRequest request
    ) {
        LOGGER.info("REST: Initiating async product processing");
        // In a real application, this would trigger async processing
        // For demo purposes, we'll just return accepted
        return Response.accepted()
            .entity("{\"message\": \"Processing initiated\", \"requestId\": \"" + 
                   java.util.UUID.randomUUID() + "\"}")
            .build();
    }
}