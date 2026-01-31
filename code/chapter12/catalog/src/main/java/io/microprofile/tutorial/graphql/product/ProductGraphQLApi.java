package io.microprofile.tutorial.graphql.product;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Source;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * GraphQL API for product operations
 */
@GraphQLApi
@ApplicationScoped
@Description("Product management GraphQL API")
public class ProductGraphQLApi {
    
    @Inject
    ProductService productService;
    
    @Inject
    ReviewService reviewService;
    
    // ===== Queries =====
    
    @Query("products")
    @Description("Retrieves all products from the catalog")
    public List<Product> getAllProducts() {
        return productService.findAll();
    }
    
    @Query("product")
    @Description("Retrieves a single product by its unique identifier")
    public Product getProduct(
            @Name("id") 
            @Description("The unique identifier of the product") 
            Long id) {
        Product product = productService.findById(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        return product;
    }
    
    @Query("searchProducts")
    @Description("Search for products by name or category")
    public List<Product> searchProducts(
            @Name("searchTerm") 
            @Description("Search term to match against product name or description")
            String searchTerm,
            @Name("category") 
            @Description("Filter by category")
            String category) {
        return productService.search(searchTerm, category);
    }
    
    @Query("productCount")
    @Description("Returns the total number of products in the catalog")
    public int getProductCount() {
        return productService.getProductCount();
    }
    
    @Query("averagePrice")
    @Description("Returns the average price of all products")
    public Double getAveragePrice() {
        return productService.getAveragePrice();
    }
    
    @Query("categories")
    @Description("Returns all available product categories")
    public List<String> getCategories() {
        return productService.getAllCategories();
    }
    
    // ===== Mutations =====
    
    @Mutation("createProduct")
    @Description("Creates a new product in the catalog")
    public Product createProduct(
            @Name("input") 
            @Description("Product input data") 
            ProductInput input) {
        return productService.createProduct(input);
    }
    
    @Mutation("updateProduct")
    @Description("Updates an existing product")
    public Product updateProduct(
            @Name("id") 
            @Description("Product ID to update") 
            Long id,
            @Name("input") 
            @Description("Updated product data") 
            ProductInput input) {
        return productService.updateProduct(id, input);
    }
    
    @Mutation("deleteProduct")
    @Description("Deletes a product from the catalog")
    public boolean deleteProduct(
            @Name("id") 
            @Description("Product ID to delete") 
            Long id) {
        return productService.deleteProduct(id);
    }
    
    // ===== Field Resolvers =====
    
    /**
     * Field resolver to add reviews to a product
     * Non-batched version - causes N+1 queries
     */
    @Description("Reviews for this product")
    public List<Review> reviews(@Source Product product) {
        return reviewService.findByProductId(product.getId());
    }
    
    /**
     * Batched field resolver for reviews
     * Solves N+1 query problem by fetching all reviews in one query
     */
    @Description("Reviews for products (batched)")
    public CompletionStage<List<Review>> reviews(@Source List<Product> products) {
        List<Long> productIds = products.stream()
            .map(Product::getId)
            .collect(Collectors.toList());
            
        return CompletableFuture.supplyAsync(() -> 
            reviewService.findByProductIds(productIds));
    }
    
    /**
     * Field resolver with parameters
     */
    @Description("Top reviews for this product")
    public List<Review> topReviews(
            @Source Product product,
            @Name("limit") 
            @DefaultValue("5") 
            @Description("Maximum number of reviews to return")
            int limit) {
        return reviewService.findTopReviewsByProductId(product.getId(), limit);
    }
    
    /**
     * Computed field - average rating
     */
    @Description("Average rating for this product")
    public Double averageRating(@Source Product product) {
        return reviewService.getAverageRating(product.getId());
    }
    
    /**
     * Computed field - price category
     */
    @Description("Price category based on product price")
    public String priceCategory(@Source Product product) {
        Double price = product.getPrice();
        if (price < 50) return "BUDGET";
        if (price < 200) return "STANDARD";
        if (price < 500) return "PREMIUM";
        return "LUXURY";
    }
}
