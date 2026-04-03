package io.microprofile.tutorial.store.payment.service;

import io.microprofile.tutorial.store.payment.entity.PaymentDetails;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@ApplicationScoped
@Path("/authorize")
public class PaymentService {

    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    @Inject
    @ConfigProperty(name = "payment.gateway.endpoint")
    private String endpoint;

    @Inject
    @Channel("payment-requests")
    Emitter<PaymentDetails> paymentEmitter;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Submit payment for asynchronous processing", description = "Publishes a payment request to an internal Reactive Messaging channel and lets the payment logic run asynchronously")
    @APIResponses(value = {
        @APIResponse(responseCode = "202", description = "Payment request accepted for asynchronous processing"),
        @APIResponse(responseCode = "400", description = "Invalid input data"),
        @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response processPayment(PaymentDetails paymentDetails) {
        if (paymentDetails == null || paymentDetails.getAmount() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"message\":\"Payment details and amount are required\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        LOGGER.info("Accepting payment request and publishing to channel payment-requests");
        paymentEmitter.send(paymentDetails);

        String result = "{\"status\":\"accepted\", \"message\":\"Payment request accepted for asynchronous processing.\"}";
        return Response.accepted(result).type(MediaType.APPLICATION_JSON).build();
    }

    @Incoming("payment-requests")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> authorizePayment(Message<PaymentDetails> message) {
        PaymentDetails paymentDetails = message.getPayload();

        return CompletableFuture.runAsync(() -> {
            BigDecimal amount = paymentDetails.getAmount();
            if (amount == null || amount.signum() <= 0) {
                throw new IllegalArgumentException("Payment amount must be greater than zero");
            }

            LOGGER.info("Calling payment gateway API at: " + endpoint + " for amount " + amount);
            LOGGER.info("Payment processed successfully for card holder: " + paymentDetails.getCardHolderName());
        }).thenCompose(v -> message.ack())
          .exceptionallyCompose(throwable -> message.nack(throwable));
    }
}
