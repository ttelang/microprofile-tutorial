package io.microprofile.tutorial.store.order.messaging;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

/**
 * Provides a small internal price stream so the transformation example can run
 * without any external broker configuration.
 */
@ApplicationScoped
public class PricePublisher {

    @Outgoing("prices-in")
    public PublisherBuilder<Long> publishSamplePrices() {
        return ReactiveStreams.of(1299L, 2599L, 4999L);
    }
}
