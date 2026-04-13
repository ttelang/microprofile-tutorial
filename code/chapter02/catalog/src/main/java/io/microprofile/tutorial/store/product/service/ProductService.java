package io.microprofile.tutorial.store.product.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.microprofile.tutorial.store.product.entity.Product;

@ApplicationScoped
public class ProductService {

    private List<Product> products = new ArrayList<>();

    public ProductService() {
        products.add(new Product(1L, "product 1", "", 0.0));
        products.add(new Product(2L, "product 2", "", 0.0));
    }

    public List<Product> getProducts() {
        return products;
    }

    public Optional<Product> getProductById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }
}