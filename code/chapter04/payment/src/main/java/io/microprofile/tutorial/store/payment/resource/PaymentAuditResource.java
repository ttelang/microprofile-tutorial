package io.microprofile.tutorial.store.payment.resource;

import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditOperationType;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditStatus;
import io.microprofile.tutorial.store.payment.service.PaymentAuditService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Resource for accessing payment audit trails.
 * Provides endpoints to view and query audit records for compliance and troubleshooting.
 */
@ApplicationScoped
@Path("/audit")
public class PaymentAuditResource {

    @Inject
    private PaymentAuditService auditService;

    /**
     * Get all audit records.
     *
     * @return Response with all audit records
     */
    @GET
    @Path("/records")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllAuditRecords() {
        List<PaymentAuditRecord> records = auditService.getAllAuditRecords();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalRecords", records.size());
        response.put("records", records);
        
        return Response
                .status(Response.Status.OK)
                .entity(response)
                .build();
    }

    /**
     * Get audit records for a specific payment ID.
     *
     * @param paymentId Payment transaction ID
     * @return Response with audit records for the payment
     */
    @GET
    @Path("/payment/{paymentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuditRecordsByPaymentId(@PathParam("paymentId") String paymentId) {
        List<PaymentAuditRecord> records = auditService.getAuditRecordsByPaymentId(paymentId);
        
        if (records.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "No audit records found for payment ID: " + paymentId);
            response.put("paymentId", paymentId);
            response.put("totalRecords", 0);
            
            return Response
                    .status(Response.Status.NOT_FOUND)
                    .entity(response)
                    .build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("paymentId", paymentId);
        response.put("totalRecords", records.size());
        response.put("records", records);
        
        return Response
                .status(Response.Status.OK)
                .entity(response)
                .build();
    }

    /**
     * Get audit records by operation type.
     *
     * @param operationType Type of operation (PAYMENT_PROCESS, PAYMENT_VALIDATE, PAYMENT_REFUND, IDEMPOTENT_PAYMENT, IDEMPOTENT_CACHE_HIT)
     * @return Response with audit records for the operation type
     */
    @GET
    @Path("/operation/{operationType}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuditRecordsByOperationType(@PathParam("operationType") String operationType) {
        try {
            AuditOperationType type = AuditOperationType.valueOf(operationType.toUpperCase());
            List<PaymentAuditRecord> records = auditService.getAuditRecordsByOperationType(type);
            
            Map<String, Object> response = new HashMap<>();
            response.put("operationType", type);
            response.put("totalRecords", records.size());
            response.put("records", records);
            
            return Response
                    .status(Response.Status.OK)
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid operation type: " + operationType);
            errorResponse.put("validTypes", AuditOperationType.values());
            
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    /**
     * Get audit records by status.
     *
     * @param status Audit status (SUCCESS, FAILED, VALIDATION_ERROR, CONFLICT, SYSTEM_ERROR)
     * @return Response with audit records for the status
     */
    @GET
    @Path("/status/{status}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuditRecordsByStatus(@PathParam("status") String status) {
        try {
            AuditStatus auditStatus = AuditStatus.valueOf(status.toUpperCase());
            List<PaymentAuditRecord> records = auditService.getAuditRecordsByStatus(auditStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", auditStatus);
            response.put("totalRecords", records.size());
            response.put("records", records);
            
            return Response
                    .status(Response.Status.OK)
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid status: " + status);
            errorResponse.put("validStatuses", AuditStatus.values());
            
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    /**
     * Get audit statistics summary.
     *
     * @return Response with audit statistics
     */
    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAuditStats() {
        List<PaymentAuditRecord> allRecords = auditService.getAllAuditRecords();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRecords", allRecords.size());
        
        // Count by operation type
        Map<String, Long> byOperationType = new HashMap<>();
        for (AuditOperationType type : AuditOperationType.values()) {
            long count = auditService.getAuditRecordsByOperationType(type).size();
            byOperationType.put(type.name(), count);
        }
        stats.put("byOperationType", byOperationType);
        
        // Count by status
        Map<String, Long> byStatus = new HashMap<>();
        for (AuditStatus status : AuditStatus.values()) {
            long count = auditService.getAuditRecordsByStatus(status).size();
            byStatus.put(status.name(), count);
        }
        stats.put("byStatus", byStatus);
        
        // Count cached responses
        long cachedCount = allRecords.stream()
                .filter(record -> record.getCachedResponse() != null && record.getCachedResponse())
                .count();
        stats.put("cachedResponses", cachedCount);
        
        return Response
                .status(Response.Status.OK)
                .entity(stats)
                .build();
    }
}
