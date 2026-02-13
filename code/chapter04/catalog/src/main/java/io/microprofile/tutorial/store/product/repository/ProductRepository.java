package io.microprofile.tutorial.store.product.repository;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple in-memory product repository for demonstration purposes.
 * This implementation keeps the code focused on OpenAPI documentation concepts.
 */
@ApplicationScoped
public class ProductRepository {

    private List<Product> products = new ArrayList<>();
    private Long nextId = 1L;

    @PostConstruct
    public void init() {
        // Initialize with sample data
        products.add(new Product(nextId++, "Laptop", "High-performance laptop for professionals", 1299.99));
        products.add(new Product(nextId++, "Wireless Mouse", "Ergonomic wireless mouse with USB receiver", 29.99));
        products.add(new Product(nextId++, "Mechanical Keyboard", "RGB mechanical gaming keyboard", 89.99));
        products.add(new Product(nextId++, "Monitor", "27-inch 4K UHD monitor", 449.99));
        products.add(new Product(nextId++, "Webcam", "1080p HD webcam with built-in microphone", 79.99));
    }

    /**
     * Find all products.
     * @return list of all products (defensive copy)
     */
    public List<Product> findAllProducts() {
        return new ArrayList<>(products);
    }

    /**
     * Find a product by its ID.
     * @param id the product ID
     * @return the product if found, null otherwise
     */
    public Product findProductById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Create a new product.
     * @param product the product to create
     * @return the created product with assigned ID
     */
    public Product createProduct(Product product) {
        product.setId(nextId++);
        products.add(product);
        return product;
    }

    /**
     * Update an existing product.
     * @param id the ID of the product to update
     * @param updatedProduct the updated product data
     * @return the updated product if found, null otherwise
     */
    public Product updateProduct(Long id, Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            if (products.get(i).getId().equals(id)) {
                updatedProduct.setId(id);
                products.set(i, updatedProduct);
                return updatedProduct;
            }
        }
        return null;
    }

    /**
     * Delete a product by its ID.
     * @param id the product ID
     * @return true if deleted, false if not found
     */
    public boolean deleteProduct(Long id) {
        return products.removeIf(p -> p.getId().equals(id));
    }

    /**
     * Search products by criteria.
     * @param name product name filter (partial match, case-insensitive)
     * @param description description filter (partial match, case-insensitive)
     * @param minPrice minimum price filter
     * @param maxPrice maximum price filter
     * @return list of matching products
     */
    public List<Product> searchProducts(String name, String description, 
                                        Double minPrice, Double maxPrice) {
        return products.stream()
                .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> description == null || 
                        (p.getDescription() != null && p.getDescription().toLowerCase().contains(description.toLowerCase())))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }
}
