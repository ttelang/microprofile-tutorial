package io.microprofile.tutorial.store.order.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Terminal consumer for the price conversion example.
 */
@ApplicationScoped
public class PriceLogger {

    private static final Logger LOGGER = Logger.getLogger(PriceLogger.class.getName());

    @Incoming("prices-out")
    public void logConvertedPrice(BigDecimal price) {
        LOGGER.info("Converted price available for display: $" + price);
    }
}
