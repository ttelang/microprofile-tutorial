package io.microprofile.tutorial.store.product.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import io.microprofile.tutorial.store.product.entity.Product;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.extensions.Extension;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * ProductResource demonstrating MicroProfile OpenAPI 4.1 and OpenAPI v3.1 features:
 * - Rich parameter validation with patterns, formats, and constraints
 * - Detailed response schemas with examples
 * - Array schemas with minItems/maxItems
 * - Nullable and required properties
 * - Enumeration support
 * - Format specifications
 */
@Path("/products")
@ApplicationScoped
@Schema(description = "Product resource")
@Tag(
    name = "Products",
    description = "Product catalog operations"
)
public class ProductResource {
    private List<Product> products;

    public ProductResource() {
        products = new ArrayList<>();

        // Create sample products with all fields
        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("iPhone 15 Pro");
        p1.setDescription("Apple iPhone 15 Pro with 256GB storage");
        p1.setPrice(999.99);
        p1.setSku("APL-IPH15P-256");
        p1.setCategory("ELECTRONICS");
        p1.setStockQuantity(50);
        p1.setInStock(true);
        
        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("MacBook Air M3");
        p2.setDescription("Apple MacBook Air with M3 chip, 13-inch display");
        p2.setPrice(1299.99);
        p2.setSku("APL-MBA-M3-13");
        p2.setCategory("ELECTRONICS");
        p2.setStockQuantity(25);
        p2.setInStock(true);
        
        Product p3 = new Product();
        p3.setId(3L);
        p3.setName("Samsung Galaxy S24");
        p3.setDescription("Samsung Galaxy S24 Ultra with 512GB storage");
        p3.setPrice(1199.99);
        p3.setSku("SAM-GAL-S24-512");
        p3.setCategory("ELECTRONICS");
        p3.setStockQuantity(30);
        p3.setInStock(true);

        products.add(p1);
        products.add(p2);
        products.add(p3);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "List all products",
        description = "Retrieves a complete list of products in the catalog."
    )
    @APIResponses(value = {
        @APIResponse(
            responseCode = "200",
            description = "Successfully retrieved product list",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Product.class)
            )
        ),
        @APIResponse(
            responseCode = "500",
            description = "Internal server error"
        )
    })
    public List<Product> getProducts() {
        return products;
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Get product by ID",
        description = "Retrieves a single product by its unique identifier",
        extensions = {
            @Extension(name = "x-custom-timeout", value = "60"),
            @Extension(name = "x-rate-limit", value = "100"),
            @Extension(name = "x-cache-ttl", value = "300")
        }
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Product found",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Product.class)
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "Product not found"
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid ID format"
        )
    })
    public Response getProductById(
        @Parameter(
            description = "Product ID - must be a positive integer",
            required = true,
            schema = @Schema(
                type = SchemaType.INTEGER,
                format = "int64",
                minimum = "1",
                example = "1"
            )
        )
        @PathParam("id") Long id
    ) {
        return products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst()
            .map(p -> Response.ok(p).build())
            .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Create a new product",
        extensions = {
            @Extension(name = "x-requires-auth", value = "admin"),
            @Extension(name = "x-audit-log", value = "true")
        }
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Product.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid product data"
        )
    })
    public Response createProduct(
        @Valid
        @Parameter(
            description = "Product to create",
            required = true,
            schema = @Schema(implementation = Product.class)
        )
        Product product
    ) {
        // Generate new ID
        Long newId = products.stream()
            .mapToLong(Product::getId)
            .max()
            .orElse(0L) + 1;
        product.setId(newId);
        products.add(product);
        return Response.status(Response.Status.CREATED).entity(product).build();
    }

    @GET
    @Path("/search")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Search products",
        description = """
            Search products by various criteria demonstrating:
            - String pattern validation
            - Numeric range constraints with exclusiveMinimum
            - Nullable optional parameters
            - Category enumeration
            - Default values
            - Format specifications
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Search results (may be empty)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = Product.class,
                    minItems = 0
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid search parameters"
        )
    })
    public Response searchProducts(
        @Parameter(
            description = "Product name search term - alphanumeric with spaces and hyphens",
            schema = @Schema(
                type = SchemaType.STRING,
                minLength = 1,
                maxLength = 100,
                pattern = "^[a-zA-Z0-9\\\\s\\\\-]+$",
                nullable = true,
                example = "iPhone"
            )
        )
        @QueryParam("name") String name,
        
        @Parameter(
            description = "Minimum price (exclusive) - must be greater than $0.00",
            schema = @Schema(
                type = SchemaType.NUMBER,
                format = "double",
                minimum = "0.00",
                exclusiveMinimum = true,
                maximum = "999999.99",
                multipleOf = 0.01,
                nullable = true,
                example = "500.00"
            )
        )
        @QueryParam("minPrice") Double minPrice,
        
        @Parameter(
            description = "Maximum price (inclusive)",
            schema = @Schema(
                type = SchemaType.NUMBER,
                format = "double",
                minimum = "0.01",
                maximum = "999999.99",
                multipleOf = 0.01,
                nullable = true,
                example = "2000.00"
            )
        )
        @QueryParam("maxPrice") Double maxPrice,
        
        @Parameter(
            description = "Product category - must be one of predefined categories",
            schema = @Schema(
                type = SchemaType.STRING,
                enumeration = {"ELECTRONICS", "CLOTHING", "BOOKS", "HOME_GARDEN", "SPORTS", "TOYS", "FOOD", "BEAUTY"},
                nullable = true,
                example = "ELECTRONICS"
            )
        )
        @QueryParam("category") String category,
        
        @Parameter(
            description = "Page number for pagination (zero-based)",
            schema = @Schema(
                type = SchemaType.INTEGER,
                format = "int32",
                minimum = "0",
                defaultValue = "0",
                example = "0"
            )
        )
        @QueryParam("page") @DefaultValue("0") Integer page,
        
        @Parameter(
            description = "Page size - number of items per page",
            schema = @Schema(
                type = SchemaType.INTEGER,
                format = "int32",
                minimum = "1",
                maximum = "100",
                defaultValue = "20",
                example = "20"
            )
        )
        @QueryParam("size") @DefaultValue("20") Integer size
    ) {
        List<Product> results = products.stream()
            .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
            .filter(p -> minPrice == null || p.getPrice() > minPrice)
            .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
            .filter(p -> category == null || category.equals(p.getCategory()))
            .collect(Collectors.toList());
        
        // Apply pagination
        int start = page * size;
        int end = Math.min(start + size, results.size());
        if (start < results.size()) {
            results = results.subList(start, end);
        } else {
            results = List.of();
        }
        
        return Response.ok(results).build();
    }
}