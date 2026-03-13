package io.microprofile.tutorial.store.payment.resource;

import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditOperationType;
import io.microprofile.tutorial.store.payment.entity.PaymentAuditRecord.AuditStatus;
import io.microprofile.tutorial.store.payment.service.PaymentAuditService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

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
@Tag(name = "Audit", description = "Payment audit trail operations")
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
    @Operation(
        summary = "Get all audit records",
        description = """
            Retrieves all payment audit records in the system.
            
            **Note:** Card numbers are automatically masked in audit records (shows last 4 digits only)
            for PCI-DSS compliance.
            
            Use this endpoint for:
            - Compliance reporting
            - System-wide audit review
            - Troubleshooting payment issues
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Successfully retrieved audit records",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                schema = @Schema(implementation = Object.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "totalRecords": 5,
                          "records": [
                            {
                              "auditId": "audit-001",
                              "paymentId": "payment-123",
                              "timestamp": "2026-03-09T10:30:00Z",
                              "operationType": "PAYMENT_PROCESS",
                              "status": "SUCCESS",
                              "cardNumberMasked": "****1111",
                              "amount": 99.99,
                              "responseMessage": "Payment processed successfully",
                              "cachedResponse": false
                            }
                          ]
                        }
                        """
                )
            )
        )
    })
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
    @Operation(
        summary = "Get audit records by payment ID",
        description = """
            Retrieves all audit records for a specific payment transaction.
            
            This shows the complete history of a payment, including:
            - Initial payment attempt
            - Validation steps
            - Idempotent cache hits (if payment ID was reused)
            - Any refunds or modifications
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Audit records found for payment ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "paymentId": "payment-123",
                          "totalRecords": 2,
                          "records": [
                            {
                              "auditId": "audit-001",
                              "paymentId": "payment-123",
                              "timestamp": "2026-03-09T10:30:00Z",
                              "operationType": "IDEMPOTENT_PAYMENT",
                              "status": "SUCCESS",
                              "cachedResponse": false
                            },
                            {
                              "auditId": "audit-002",
                              "paymentId": "payment-123",
                              "timestamp": "2026-03-09T10:31:00Z",
                              "operationType": "IDEMPOTENT_CACHE_HIT",
                              "status": "SUCCESS",
                              "cachedResponse": true
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "404",
            description = "No audit records found for payment ID",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "message": "No audit records found for payment ID: payment-999",
                          "paymentId": "payment-999",
                          "totalRecords": 0
                        }
                        """
                )
            )
        )
    })
    public Response getAuditRecordsByPaymentId(
            @Parameter(
                description = "Payment transaction ID to query",
                required = true,
                schema = @Schema(
                    type = SchemaType.STRING,
                    example = "payment-123"
                )
            )
            @PathParam("paymentId") String paymentId) {
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
    @Operation(
        summary = "Get audit records by operation type",
        description = """
            Retrieves audit records filtered by operation type.
            
            **Valid Operation Types:**
            - `PAYMENT_PROCESS` - Regular payment processing
            - `PAYMENT_VALIDATE` - Payment validation only
            - `PAYMENT_REFUND` - Refund operations
            - `IDEMPOTENT_PAYMENT` - First-time idempotent payment processing
            - `IDEMPOTENT_CACHE_HIT` - Idempotent payment returned from cache
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Audit records found for operation type",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "operationType": "IDEMPOTENT_CACHE_HIT",
                          "totalRecords": 3,
                          "records": [
                            {
                              "auditId": "audit-002",
                              "paymentId": "payment-123",
                              "operationType": "IDEMPOTENT_CACHE_HIT",
                              "status": "SUCCESS",
                              "cachedResponse": true
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid operation type",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Invalid operation type: INVALID_TYPE",
                          "validTypes": [
                            "PAYMENT_PROCESS",
                            "PAYMENT_VALIDATE",
                            "PAYMENT_REFUND",
                            "IDEMPOTENT_PAYMENT",
                            "IDEMPOTENT_CACHE_HIT"
                          ]
                        }
                        """
                )
            )
        )
    })
    public Response getAuditRecordsByOperationType(
            @Parameter(
                description = "Operation type to filter by (case-insensitive)",
                required = true,
                schema = @Schema(
                    type = SchemaType.STRING,
                    enumeration = {
                        "PAYMENT_PROCESS",
                        "PAYMENT_VALIDATE",
                        "PAYMENT_REFUND",
                        "IDEMPOTENT_PAYMENT",
                        "IDEMPOTENT_CACHE_HIT"
                    },
                    example = "IDEMPOTENT_PAYMENT"
                )
            )
            @PathParam("operationType") String operationType) {
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
    @Operation(
        summary = "Get audit records by status",
        description = """
            Retrieves audit records filtered by status.
            
            **Valid Status Values:**
            - `SUCCESS` - Operation completed successfully
            - `FAILED` - Operation failed (business logic failure)
            - `VALIDATION_ERROR` - Invalid input data
            - `CONFLICT` - Idempotency conflict (same ID, different data)
            - `SYSTEM_ERROR` - Internal system error
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Audit records found for status",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "status": "SUCCESS",
                          "totalRecords": 10,
                          "records": [
                            {
                              "auditId": "audit-001",
                              "paymentId": "payment-123",
                              "status": "SUCCESS",
                              "responseMessage": "Payment processed successfully"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "Invalid status value",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "error": "Invalid status: INVALID_STATUS",
                          "validStatuses": [
                            "SUCCESS",
                            "FAILED",
                            "VALIDATION_ERROR",
                            "CONFLICT",
                            "SYSTEM_ERROR"
                          ]
                        }
                        """
                )
            )
        )
    })
    public Response getAuditRecordsByStatus(
            @Parameter(
                description = "Status to filter by (case-insensitive)",
                required = true,
                schema = @Schema(
                    type = SchemaType.STRING,
                    enumeration = {
                        "SUCCESS",
                        "FAILED",
                        "VALIDATION_ERROR",
                        "CONFLICT",
                        "SYSTEM_ERROR"
                    },
                    example = "SUCCESS"
                )
            )
            @PathParam("status") String status) {
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
    @Operation(
        summary = "Get audit statistics",
        description = """
            Retrieves aggregated statistics for all audit records.
            
            Provides breakdown by:
            - Total record count
            - Count by operation type
            - Count by status
            - Number of cached responses (idempotent cache hits)
            
            Use this for:
            - Dashboard metrics
            - System health monitoring
            - Compliance reporting
            """
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "Successfully retrieved audit statistics",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON,
                examples = @ExampleObject(
                    value = """
                        {
                          "totalRecords": 25,
                          "byOperationType": {
                            "PAYMENT_PROCESS": 5,
                            "PAYMENT_VALIDATE": 3,
                            "PAYMENT_REFUND": 2,
                            "IDEMPOTENT_PAYMENT": 10,
                            "IDEMPOTENT_CACHE_HIT": 5
                          },
                          "byStatus": {
                            "SUCCESS": 20,
                            "FAILED": 3,
                            "VALIDATION_ERROR": 1,
                            "CONFLICT": 1,
                            "SYSTEM_ERROR": 0
                          },
                          "cachedResponses": 5
                        }
                        """
                )
            )
        )
    })
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
