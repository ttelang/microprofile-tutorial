package io.microprofile.tutorial.store.product.entity;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Status of asynchronous product processing operation.
 * 
 * <p>This enum represents the possible outcomes of an async processing request:
 * <ul>
 *   <li><b>SUCCESS</b> - Processing completed successfully</li>
 *   <li><b>FAILED</b> - Processing encountered an error and could not complete</li>
 *   <li><b>PARTIAL</b> - Processing completed with warnings or partial success</li>
 * </ul>
 */
@Schema(
    description = "Status of the processing operation",
    enumeration = {"SUCCESS", "FAILED", "PARTIAL"}
)
public enum ProcessingStatus {
    /** Processing completed successfully */
    SUCCESS,
    
    /** Processing failed due to an error */
    FAILED,
    
    /** Processing completed with warnings or partial success */
    PARTIAL
}
