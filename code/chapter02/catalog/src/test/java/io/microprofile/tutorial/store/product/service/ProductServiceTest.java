package io.microprofile.tutorial.store.product.service;

import io.microprofile.tutorial.store.product.entity.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
    }

    @AfterEach
    void tearDown() {
        productService = null;
    }

    @Test
    void testGetProducts() {
        List<Product> products = productService.getProducts();

        assertNotNull(products);
        assertEquals(2, products.size());

        // Verify first product details
        Product firstProduct = products.get(0);
        assertEquals(1L, firstProduct.getId());
        assertEquals("product 1", firstProduct.getName());
        assertEquals("", firstProduct.getDescription());
        assertEquals(0.0, firstProduct.getPrice());
    }

    @Test
    void testGetProductById_ExistingProduct() {
        Optional<Product> product = productService.getProductById(1L);

        assertTrue(product.isPresent());
        assertEquals(1L, product.get().getId());
        assertEquals("product 1", product.get().getName());
        assertEquals("", product.get().getDescription());
        assertEquals(0.0, product.get().getPrice());
    }

    @Test
    void testGetProductById_NonExistingProduct() {
        Optional<Product> product = productService.getProductById(999L);
        assertFalse(product.isPresent());
    }

    @Test
    void testGetProductById_SecondProduct() {
        Optional<Product> product = productService.getProductById(2L);

        assertTrue(product.isPresent());
        assertEquals(2L, product.get().getId());
        assertEquals("product 2", product.get().getName());
        assertEquals("", product.get().getDescription());
        assertEquals(0.0, product.get().getPrice());
    }
}