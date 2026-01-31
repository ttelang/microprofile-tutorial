package io.microprofile.tutorial.graphql.product;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service layer for product operations
 */
@ApplicationScoped
public class ProductService {
    
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    @ConfigProperty(name = "product.max.results", defaultValue = "100")
    Integer maxResults;
    
    public ProductService() {
        // Initialize with sample data
        initializeSampleData();
    }
    
    private void initializeSampleData() {
        createProduct(new ProductInput("Laptop", "High-performance laptop", 999.99, "Electronics", 50));
        createProduct(new ProductInput("Mouse", "Wireless mouse", 29.99, "Electronics", 150));
        createProduct(new ProductInput("Keyboard", "Mechanical keyboard", 89.99, "Electronics", 75));
        createProduct(new ProductInput("Monitor", "27-inch 4K monitor", 399.99, "Electronics", 30));
        createProduct(new ProductInput("Headphones", "Noise-canceling headphones", 199.99, "Electronics", 100));
    }
    
    public List<Product> findAll() {
        return new ArrayList<>(products.values()).stream()
            .limit(maxResults)
            .collect(Collectors.toList());
    }
    
    public Product findById(Long id) {
        return products.get(id);
    }
    
    public List<Product> findByIds(List<Long> ids) {
        return ids.stream()
            .map(products::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public List<Product> search(String searchTerm, String category) {
        return products.values().stream()
            .filter(p -> {
                boolean matchesSearch = searchTerm == null || 
                    p.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchTerm.toLowerCase()));
                boolean matchesCategory = category == null || category.equals(p.getCategory());
                return matchesSearch && matchesCategory;
            })
            .collect(Collectors.toList());
    }
    
    public Product createProduct(ProductInput input) {
        Long id = idCounter.getAndIncrement();
        Product product = new Product(
            id,
            input.getName(),
            input.getDescription(),
            input.getPrice(),
            input.getCategory(),
            input.getStockQuantity()
        );
        products.put(id, product);
        return product;
    }
    
    public Product updateProduct(Long id, ProductInput input) {
        Product existing = products.get(id);
        if (existing == null) {
            throw new ProductNotFoundException(id);
        }
        
        existing.setName(input.getName());
        existing.setDescription(input.getDescription());
        existing.setPrice(input.getPrice());
        existing.setCategory(input.getCategory());
        existing.setStockQuantity(input.getStockQuantity());
        
        return existing;
    }
    
    public boolean deleteProduct(Long id) {
        return products.remove(id) != null;
    }
    
    public int getProductCount() {
        return products.size();
    }
    
    public Double getAveragePrice() {
        return products.values().stream()
            .mapToDouble(Product::getPrice)
            .average()
            .orElse(0.0);
    }
    
    public List<String> getAllCategories() {
        return products.values().stream()
            .map(Product::getCategory)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}
