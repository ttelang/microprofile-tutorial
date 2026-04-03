package io.microprofile.tutorial.store.order;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import java.util.logging.Logger;

/**
 * Verify that MicroProfile Reactive Messaging is wired in the order service.
 */
@ApplicationScoped
public class HelloMessaging {

    private static final Logger LOG = Logger.getLogger(HelloMessaging.class.getName());

    @PostConstruct
    void onStartup() {
        emit("HelloMessaging initialized - Reactive Messaging is active in the order service.");
    }

    @Outgoing("hello-in")
    public PublisherBuilder<String> source() {
        emit("HelloMessaging source emitted: hello reactive messaging");
        return ReactiveStreams.of("hello reactive messaging");
    }

    @Incoming("hello-in")
    @Outgoing("hello-out")
    public String process(String message) {
        String processed = message.toUpperCase();
        emit("HelloMessaging processed message: " + processed);
        return processed;
    }

    @Incoming("hello-out")
    public void sink(String message) {
        emit("HelloMessaging final output: " + message);
    }

    private void emit(String message) {
        LOG.info(message);
        System.out.println(message);
    }
}