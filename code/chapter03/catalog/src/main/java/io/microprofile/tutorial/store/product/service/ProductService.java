package io.microprofile.tutorial.store.product.service;

import java.util.List;
import java.util.logging.Logger;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.repository.ProductRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@RequestScoped
public class ProductService {
    
    private static final Logger LOGGER = Logger.getLogger(ProductService.class.getName());

    @Inject
    private ProductRepository repository;

    public List<Product> findAllProducts() {
        LOGGER.fine("Service: Finding all products");
        return repository.findAllProducts();
    }
    
    public Product findProductById(Long id) {
        LOGGER.fine("Service: Finding product with ID: " + id);
        return repository.findProductById(id);
    }
    
    public void createProduct(Product product) {
        LOGGER.fine("Service: Creating new product: " + product);
        repository.createProduct(product);
    }
    
    public Product updateProduct(Long id, Product updatedProduct) {
        LOGGER.fine("Service: Updating product with ID: " + id);
        Product existingProduct = repository.findProductById(id);
        if (existingProduct != null) {
            existingProduct.setName(updatedProduct.getName());
            existingProduct.setDescription(updatedProduct.getDescription());
            existingProduct.setPrice(updatedProduct.getPrice());
            return repository.updateProduct(existingProduct);
        }
        return null;
    }
    
    public boolean deleteProduct(Long id) {
        LOGGER.fine("Service: Deleting product with ID: " + id);
        Product product = repository.findProductById(id);
        if (product != null) {
            repository.deleteProduct(product);
            return true;
        }
        return false;
    }
}