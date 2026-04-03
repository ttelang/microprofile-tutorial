package io.microprofile.tutorial.store.order;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS application for the order service extension.
 */
@ApplicationPath("/api")
public class OrderApplication extends Application {
}
