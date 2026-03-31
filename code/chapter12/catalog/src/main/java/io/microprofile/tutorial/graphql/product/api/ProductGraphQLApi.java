package io.microprofile.tutorial.graphql.product.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.Source;
import io.microprofile.tutorial.graphql.product.dto.ProductInput;
import io.microprofile.tutorial.graphql.product.entity.Product;
import io.microprofile.tutorial.graphql.product.entity.ProductReview;
import io.microprofile.tutorial.graphql.product.exception.ProductNotFoundException;
import io.microprofile.tutorial.graphql.product.service.ProductService;
import io.microprofile.tutorial.graphql.product.service.ReviewService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

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
    public List<ProductReview> reviews(@Source Product product) {
        return reviewService.findByProductId(product.getId());
    }
    
    /**
     * Field resolver with parameters
     */
    @Description("Top reviews for this product")
    public List<ProductReview> topReviews(
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
