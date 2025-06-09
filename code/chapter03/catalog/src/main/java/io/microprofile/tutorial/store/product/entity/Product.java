package io.microprofile.tutorial.store.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "Product")
@NamedQuery(name = "Product.findAllProducts", query = "SELECT p FROM Product p")
@NamedQuery(name = "Product.findProductById", query = "SELECT p FROM Product p WHERE p.id = :id")
@Data
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;
    
    @NotNull
    private String description;
    
    @NotNull
    private Double price;
}