package io.microprofile.tutorial.store.product.service;

import io.microprofile.tutorial.store.product.entity.Product;
import io.microprofile.tutorial.store.product.entity.ProductEvent;
import io.microprofile.tutorial.store.product.entity.WebhookSubscription;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.JsonbBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Service for managing webhook subscriptions and sending event notifications.
 * Demonstrates webhook implementation for OpenAPI 3.1.
 */
@ApplicationScoped
public class WebhookService {
    
    private static final Logger LOGGER = Logger.getLogger(WebhookService.class.getName());
    private final List<WebhookSubscription> subscriptions = new ArrayList<>();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    /**
     * Subscribe to webhook events
     */
    public WebhookSubscription subscribe(WebhookSubscription subscription) {
        // Generate ID and secret
        subscription.setId("sub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        subscription.setSecret("whsec_" + UUID.randomUUID().toString().replace("-", ""));
        subscription.setActive(subscription.getActive() != null ? subscription.getActive() : true);
        
        subscriptions.add(subscription);
        LOGGER.info("Created webhook subscription: " + subscription.getId() + " for URL: " + subscription.getUrl());
        return subscription;
    }
    
    /**
     * Get all subscriptions
     */
    public List<WebhookSubscription> getSubscriptions() {
        return new ArrayList<>(subscriptions);
    }
    
    /**
     * Get subscription by ID
     */
    public WebhookSubscription getSubscription(String id) {
        return subscriptions.stream()
            .filter(s -> s.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Delete a subscription
     */
    public boolean deleteSubscription(String id) {
        return subscriptions.removeIf(s -> s.getId().equals(id));
    }
    
    /**
     * Send webhook event to all active subscribers
     */
    public void sendEvent(String eventType, Product product) {
        // Create event
        ProductEvent event = new ProductEvent();
        event.setEventId("evt_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        event.setEventType(eventType);
        event.setTimestamp(LocalDateTime.now());
        event.setProduct(product);
        
        // Find matching subscriptions
        List<WebhookSubscription> targets = subscriptions.stream()
            .filter(WebhookSubscription::getActive)
            .filter(s -> s.getEvents().contains(eventType))
            .collect(Collectors.toList());
        
        LOGGER.info("Sending " + eventType + " event to " + targets.size() + " subscribers");
        
        // Send to each subscriber asynchronously
        targets.forEach(subscription -> 
            CompletableFuture.runAsync(() -> sendToSubscriber(event, subscription))
        );
    }
    
    /**
     * Send event to a specific subscriber
     */
    private void sendToSubscriber(ProductEvent event, WebhookSubscription subscription) {
        try {
            String payload = JsonbBuilder.create().toJson(event);
            String signature = generateSignature(payload, subscription.getSecret());
            
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(subscription.getUrl()))
                .header("Content-Type", "application/json")
                .header("X-Webhook-Signature", signature)
                .header("X-Event-Type", event.getEventType())
                .header("X-Event-Id", event.getEventId())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                LOGGER.info("Webhook delivered to " + subscription.getUrl() + " (status: " + response.statusCode() + ")");
            } else {
                LOGGER.warning("Webhook delivery failed to " + subscription.getUrl() + " (status: " + response.statusCode() + ")");
            }
        } catch (Exception e) {
            LOGGER.severe("Error sending webhook to " + subscription.getUrl() + ": " + e.getMessage());
        }
    }
    
    /**
     * Generate HMAC-SHA256 signature for webhook verification
     */
    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }
}
