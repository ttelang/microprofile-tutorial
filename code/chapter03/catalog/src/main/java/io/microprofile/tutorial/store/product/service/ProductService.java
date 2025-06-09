package io.microprofile.tutorial.store.product.service;

import java.util.List;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class ProductService {

    @Inject
    private ProductRepository repository;

    public List<Product> findAllProducts() {
        return repository.findAllProducts();
    }

    public Product findProductById(Long id) {
        return repository.findProductById(id);
    }
    public Product createProduct(Product product) {
        repository.createProduct(product);
        return product;
    }
    public Product updateProduct(Product product) {
        return repository.updateProduct(product);
    }
    public void deleteProduct(Product product) {
        repository.deleteProduct(product);
    }
}