package com.example.simple;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import java.util.logging.Logger;

@ApplicationScoped
public class SimpleMessageBean {

    private static final Logger LOGGER = Logger.getLogger(SimpleMessageBean.class.getName());

    @Outgoing("greetings-in")
    public PublisherBuilder<String> produce() {
        String message = "Hello, MicroProfile Reactive Messaging";
        LOGGER.info("Sending message: " + message);
        return ReactiveStreams.of(message);
    }

    @Incoming("greetings-in")
    @Outgoing("greetings-out")
    public String process(String message) {
        LOGGER.info("Processing message: " + message);
        return message.toUpperCase();
    }

    @Incoming("greetings-out")
    public void consume(String message) {
        LOGGER.info("Received message: " + message);
    }
}
