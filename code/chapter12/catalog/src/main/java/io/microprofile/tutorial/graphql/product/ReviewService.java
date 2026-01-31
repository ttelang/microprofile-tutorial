package io.microprofile.tutorial.graphql.product;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Service layer for review operations
 */
@ApplicationScoped
public class ReviewService {
    
    private final Map<Long, Review> reviews = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    
    public ReviewService() {
        // Initialize with sample reviews
        initializeSampleData();
    }
    
    private void initializeSampleData() {
        createReview(1L, "John Doe", 5, "Excellent laptop, very fast!");
        createReview(1L, "Jane Smith", 4, "Good performance, a bit pricey.");
        createReview(2L, "Bob Johnson", 5, "Perfect mouse, comfortable grip.");
        createReview(3L, "Alice Brown", 4, "Great keyboard, keys are responsive.");
        createReview(4L, "Charlie Davis", 5, "Amazing display quality!");
    }
    
    private void createReview(Long productId, String reviewerName, Integer rating, String comment) {
        Long id = idCounter.getAndIncrement();
        Review review = new Review(id, productId, reviewerName, rating, comment, LocalDateTime.now());
        reviews.put(id, review);
    }
    
    public List<Review> findByProductId(Long productId) {
        return reviews.values().stream()
            .filter(r -> r.getProductId().equals(productId))
            .collect(Collectors.toList());
    }
    
    public List<Review> findByProductIds(List<Long> productIds) {
        Set<Long> productIdSet = new HashSet<>(productIds);
        return reviews.values().stream()
            .filter(r -> productIdSet.contains(r.getProductId()))
            .collect(Collectors.toList());
    }
    
    public List<Review> findTopReviewsByProductId(Long productId, int limit) {
        return reviews.values().stream()
            .filter(r -> r.getProductId().equals(productId))
            .sorted(Comparator.comparing(Review::getRating).reversed())
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public Double getAverageRating(Long productId) {
        return reviews.values().stream()
            .filter(r -> r.getProductId().equals(productId))
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
    }
}
