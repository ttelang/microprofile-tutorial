package io.microprofile.tutorial.store.product.service;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.repository.JPA;
import io.microprofile.tutorial.store.product.repository.ProductRepositoryInterface;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.RegistryScope;

import java.util.List;
import java.util.logging.Logger;

/**
 * Service class for Product operations.
 * Contains business logic for product management.
 */
@ApplicationScoped
public class ProductService {
    
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());

    @Inject
    @JPA
    private ProductRepositoryInterface repository;
    
    // Programmatic metrics - injecting MetricRegistry
    @Inject
    @RegistryScope(scope = MetricRegistry.APPLICATION_SCOPE)
    private MetricRegistry registry;
    
    private Counter productCreatedCounter;
    private Counter productDeletedCounter;
    private Timer searchTimer;
    
    @PostConstruct
    public void initMetrics() {
        // Programmatically register metrics with rich metadata
        productCreatedCounter = registry.counter(
            Metadata.builder()
                .withName("products.created.total")
                .withDescription("Total number of products created")
                .build()
        );
        
        productDeletedCounter = registry.counter(
            Metadata.builder()
                .withName("products.deleted.total")
                .withDescription("Total number of products deleted")
                .build()
        );
        
        searchTimer = registry.timer(
            Metadata.builder()
                .withName("product.search.duration")
                .withDescription("Time taken to execute product search")
                .withUnit(MetricUnits.MILLISECONDS)
                .build()
        );
    }

    /**
     * Retrieves all products.
     *
     * @return List of all products
     */
    public List<Product> findAllProducts() {
        LOGGER.info("Service: Finding all products");
        return repository.findAllProducts();
    }
    
    /**
     * Retrieves a product by ID.
     *
     * @param id Product ID
     * @return The product or null if not found
     */
    public Product findProductById(Long id) {
        LOGGER.info("Service: Finding product with ID: " + id);
        return repository.findProductById(id);
    }
    
    /**
     * Creates a new product.
     *
     * @param product Product data to create
     * @return The created product with ID
     */
    public Product createProduct(Product product) {
        LOGGER.info("Service: Creating new product: " + product);
        Product created = repository.createProduct(product);
        productCreatedCounter.inc(); // Track product creation with programmatic counter
        return created;
    }
    
    /**
     * Updates an existing product.
     *
     * @param id ID of the product to update
     * @param updatedProduct Updated product data
     * @return The updated product or null if not found
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        LOGGER.info("Service: Updating product with ID: " + id);
        
        Product existingProduct = repository.findProductById(id);
        if (existingProduct != null) {
            // Set the ID to ensure correct update
            updatedProduct.setId(id);
            return repository.updateProduct(updatedProduct);
        }
        return null;
    }
    
    /**
     * Deletes a product by ID.
     *
     * @param id ID of the product to delete
     * @return true if deleted, false if not found
     */
    public boolean deleteProduct(Long id) {
        LOGGER.info("Service: Deleting product with ID: " + id);
        boolean deleted = repository.deleteProduct(id);
        if (deleted) {
            productDeletedCounter.inc(); // Track product deletion with programmatic counter
        }
        return deleted;
    }
    
    /**
     * Searches for products by criteria.
     *
     * @param name Product name (optional)
     * @param description Product description (optional)
     * @param minPrice Minimum price (optional)
     * @param maxPrice Maximum price (optional)
     * @return List of matching products
     */
    public List<Product> searchProducts(String name, String description, Double minPrice, Double maxPrice) {
        LOGGER.info("Service: Searching for products with criteria");
        
        // Use timer context to measure search performance programmatically
        Timer.Context context = searchTimer.time();
        try {
            return repository.searchProducts(name, description, minPrice, maxPrice);
        } finally {
            context.stop();
        }
    }
}
