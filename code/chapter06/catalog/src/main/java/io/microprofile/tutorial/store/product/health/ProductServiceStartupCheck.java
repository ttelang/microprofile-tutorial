package io.microprofile.tutorial.store.product.health;

import io.microprofile.tutorial.store.product.entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Startup;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Startup health check for the Product Catalog service.
 * Verifies that the database schema is accessible during application startup.
 */
@Startup
@ApplicationScoped
public class ProductServiceStartupCheck implements HealthCheck{

    private static final Logger LOGGER = Logger.getLogger(ProductServiceStartupCheck.class.getName());

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public HealthCheckResponse call() {
        try {
            // Verify database schema exists by checking table accessibility
            entityManager.createNamedQuery("Product.findAll", Product.class)
                        .setMaxResults(1)
                        .getResultList();
            
            LOGGER.info("Startup check: Database schema verified successfully");
            return HealthCheckResponse.named("ProductServiceStartupCheck")
                    .up()
                    .withData("message", "Database schema initialized")
                    .build();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Startup check failed: Database schema not accessible", e);
            return HealthCheckResponse.named("ProductServiceStartupCheck")
                    .down()
                    .withData("error", e.getMessage())
                    .build();
        }
    }
}
