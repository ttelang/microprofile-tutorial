package io.microprofile.tutorial.reactive;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.logging.Logger;

/**
 * Basic example demonstrating message transformation with @Incoming and @Outgoing.
 * This processor receives prices in cents and converts them to dollars.
 */
@ApplicationScoped
public class PriceConverter {
    
    private static final Logger LOGGER = Logger.getLogger(PriceConverter.class.getName());
    
    /**
     * Receives price in cents from "prices-in" channel and publishes
     * the converted price in dollars to "prices-out" channel.
     * 
     * @param priceInCents the price in cents
     * @return the price in dollars
     */
    @Incoming("prices-in")
    @Outgoing("prices-out")
    public double convertPrice(int priceInCents) {
        double priceInDollars = priceInCents / 100.0;
        LOGGER.info("Converting price: " + priceInCents + " cents = $" + priceInDollars);
        return priceInDollars;
    }
}
