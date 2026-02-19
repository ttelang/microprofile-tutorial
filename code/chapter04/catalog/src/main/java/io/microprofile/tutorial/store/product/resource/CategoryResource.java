package io.microprofile.tutorial.store.product.resource;

import io.microprofile.tutorial.store.product.entity.ProductCategory;
import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.entity.ErrorResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Arrays;
import java.util.List;

/**
 * REST Resource demonstrating type-safe enum usage with OpenAPI.
 * 
 * This resource shows how Java enums integrate with OpenAPI to provide:
 * - Automatic enum value documentation
 * - Type-safe parameters and responses
 * - Better validation and error messages
 * - IDE auto-completion for developers
 */
@Path("/categories")
@ApplicationScoped
@Tag(
    name = "Product Categories",
    description = """
        API endpoints demonstrating type-safe enum usage with OpenAPI.
        
        All category-related operations use the ProductCategory enum which provides:
        - Compile-time type safety
        - Automatic OpenAPI schema generation
        - Built-in validation
        - Self-documenting API
        """
)
public class CategoryResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get all product categories",
        description = """
            Returns all available product categories as an enum array.
            
            **Type Safety**: Response uses Java enum values
            **OpenAPI**: Automatically documents enum values in schema
            
            Each category includes:
            - Enum name (e.g., "ELECTRONICS")
            - Display name (e.g., "Electronics")
            - Description
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "List of all product categories",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = ProductCategory.class
                ),
                examples = @ExampleObject(
                    name = "all-categories",
                    summary = "All available categories",
                    value = """
                        [
                          "ELECTRONICS",
                          "CLOTHING",
                          "BOOKS",
                          "HOME_GARDEN",
                          "SPORTS",
                          "TOYS",
                          "FOOD",
                          "BEAUTY"
                        ]
                        """
                )
            )
        )
    })
    public Response getAllCategories() {
        List<ProductCategory> categories = Arrays.asList(ProductCategory.values());
        return Response.ok(categories).build();
    }
    
    @GET
    @Path("/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get category details",
        description = """
            Get detailed information about a specific product category.
            
            **Validation**: Path parameter is validated against enum values
            **Error Handling**: Returns 400 if invalid category provided
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Category details",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    name = "category-info",
                    summary = "Electronics category details",
                    value = """
                        {
                          "name": "ELECTRONICS",
                          "displayName": "Electronics",
                          "description": "Electronic devices and accessories"
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid category name",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public Response getCategoryInfo(
        @Parameter(
            description = """
                Product category name.
                
                **Type**: Enum (ProductCategory)
                **Validation**: Must be one of the allowed enum values
                **Case Insensitive**: Automatically converted to uppercase
                
                OpenAPI automatically documents all valid enum values in the dropdown.
                """,
            schema = @Schema(
                implementation = ProductCategory.class
            ),
            examples = {
                @ExampleObject(name = "electronics", value = "ELECTRONICS"),
                @ExampleObject(name = "clothing", value = "CLOTHING"),
                @ExampleObject(name = "books", value = "BOOKS")
            }
        )
        @PathParam("category") String categoryName
    ) {
        try {
            ProductCategory category = ProductCategory.fromString(categoryName);
            var info = new CategoryInfo(
                category.name(),
                category.getDisplayName(),
                category.getDescription()
            );
            return Response.ok(info).build();
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                "Invalid category: " + categoryName,
                "Valid categories: " + ProductCategory.getAllCategoryNames()
            );
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }
    }
    
    @GET
    @Path("/{category}/products")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get products by category",
        description = """
            Retrieve all products in a specific category.
            
            **Type Safety**: Category parameter uses enum for validation
            **OpenAPI**: Enum values appear in Swagger UI dropdown
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Products in the specified category",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = Product.class
                ),
                examples = {
                    @ExampleObject(
                        name = "electronics-products",
                        summary = "Electronics category products",
                        value = """
                            [
                              {
                                "id": 1,
                                "name": "Wireless Mouse",
                                "description": "Ergonomic wireless mouse",
                                "price": 29.99,
                                "category": "ELECTRONICS",
                                "stockQuantity": 100,
                                "inStock": true
                              },
                              {
                                "id": 2,
                                "name": "USB-C Hub",
                                "description": "7-in-1 USB-C hub",
                                "price": 49.99,
                                "category": "ELECTRONICS",
                                "stockQuantity": 50,
                                "inStock": true
                              }
                            ]
                            """
                    ),
                    @ExampleObject(
                        name = "books-products",
                        summary = "Books category products",
                        value = """
                            [
                              {
                                "id": 10,
                                "name": "Clean Code",
                                "description": "A Handbook of Agile Software Craftsmanship",
                                "price": 39.99,
                                "category": "BOOKS",
                                "stockQuantity": 25,
                                "inStock": true
                              }
                            ]
                            """
                    )
                }
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid category",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public Response getProductsByCategory(
        @Parameter(
            description = "Product category (enum value)",
            schema = @Schema(implementation = ProductCategory.class)
        )
        @PathParam("category") String categoryName
    ) {
        try {
            // Validate and convert category name to enum
            // In a real application, use this to query database: productService.findByCategory(category)
            ProductCategory.fromString(categoryName);
            return Response.ok(List.of()).build();
        } catch (IllegalArgumentException e) {
            ErrorResponse error = new ErrorResponse(
                "Invalid category: " + categoryName,
                "Valid categories: " + ProductCategory.getAllCategoryNames()
            );
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error)
                    .build();
        }
    }
    
    /**
     * Simple record for category information response.
     * 
     * <p>This record provides a structured representation of ProductCategory enum
     * with all its details for API responses.
     */
    @Schema(description = "Category details including name, display name, and description")
    public record CategoryInfo(
        @Schema(
            description = "Category enum name (e.g., ELECTRONICS, BOOKS)",
            enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GARDEN", "SPORTS", "TOYS", "FOOD", "BEAUTY"}
        )
        String name,
        
        @Schema(description = "Human-readable display name (e.g., Electronics, Books)")
        String displayName,
        
        @Schema(description = "Detailed category description")
        String description
    ) {}
}
