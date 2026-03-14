package io.microprofile.tutorial.store.product.health;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Readiness health check for the Product Catalog service.
 * Provides database connection health checks to monitor service health and availability.
 */
@Readiness
@ApplicationScoped
public class ProductServiceHealthCheck implements HealthCheck {

    private static final Logger LOGGER = Logger.getLogger(ProductServiceHealthCheck.class.getName());

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        if (isDatabaseConnectionHealthy()) {
            return HealthCheckResponse.named("ProductServiceReadinessCheck")
                    .up()
                    .build();
        } else {
            return HealthCheckResponse.named("ProductServiceReadinessCheck")
                    .down()
                    .build();
        }
    }

    private boolean isDatabaseConnectionHealthy(){
        try {
            // Lightweight query that doesn't depend on specific data existing
            entityManager.createNamedQuery("Product.findAll", Product.class)
                        .setMaxResults(1)
                        .getResultList();
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Database readiness check failed", e);
            return false;
        }
    }
}
