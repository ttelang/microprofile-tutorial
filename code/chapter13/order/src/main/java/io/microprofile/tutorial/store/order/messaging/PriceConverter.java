package io.microprofile.tutorial.store.order.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Basic transformation example used alongside the store order flow.
 */
@ApplicationScoped
public class PriceConverter {
    
    private static final Logger LOGGER = Logger.getLogger(PriceConverter.class.getName());
    
    @Incoming("prices-in")
    @Outgoing("prices-out")
    public BigDecimal convertPrice(long priceInCents) {
        BigDecimal priceInDollars = BigDecimal.valueOf(priceInCents, 2);
        LOGGER.info("Converting price: " + priceInCents + " cents = $" + priceInDollars);
        return priceInDollars;
    }
}
