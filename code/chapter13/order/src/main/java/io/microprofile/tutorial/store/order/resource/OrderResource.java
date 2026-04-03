package io.microprofile.tutorial.store.order.resource;

import io.microprofile.tutorial.store.order.entity.Order;
import io.microprofile.tutorial.store.order.entity.OrderStatus;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * Extends the existing store order endpoint by publishing an order event
 * for asynchronous downstream processing.
 */
@Path("/orders")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    
    private static final Logger LOGGER = Logger.getLogger(OrderResource.class.getName());
    
    @Inject
    @Channel("orders")
    Emitter<Order> orderEmitter;
    
    @POST
    public Response createOrder(Order order) {
        if (order.getStatus() == null) {
            order.setStatus(OrderStatus.CREATED);
        }

        LOGGER.info("Publishing order for async payment processing: " + order);
        orderEmitter.send(order);

        return Response.accepted()
                .entity("{\"message\": \"Order accepted and published for payment processing\"}")
                .build();
    }
}
