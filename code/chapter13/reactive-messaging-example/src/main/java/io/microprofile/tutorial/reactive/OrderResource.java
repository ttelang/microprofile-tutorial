package io.microprofile.tutorial.reactive;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;

/**
 * REST endpoint demonstrating the use of @Emitter to programmatically
 * send messages to a reactive messaging channel.
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
    
    /**
     * Creates a new order and sends it to the "orders" channel for async processing.
     * 
     * @param order the order to create
     * @return HTTP 202 Accepted response
     */
    @POST
    public Response createOrder(Order order) {
        LOGGER.info("Received order: " + order);
        orderEmitter.send(order);
        return Response.accepted()
                .entity("{\"message\": \"Order accepted for processing\"}")
                .build();
    }
}
