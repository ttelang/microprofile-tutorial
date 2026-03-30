package io.microprofile.tutorial.store.user.resource;

import java.util.Optional;
import java.util.Set;
import org.eclipse.microprofile.jwt.Claim;
import org.eclipse.microprofile.jwt.ClaimValue;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

/**
 * REST resource for user management operations.
 * Provides endpoints for creating, retrieving, updating, and deleting users.
 * Implements standard RESTful practices with proper status codes and hypermedia links.
 */
@Tag(name = "User Management", description = "Operations for managing users")
@Path("/users")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    @Claim("upn")
    ClaimValue<String> upn;

    @Inject
    @Claim("groups")
    ClaimValue<Set<String>> groups;

    @Inject
    @Claim("tenant_id")
    ClaimValue<Optional<String>> tenant;


    @Operation(
        summary = "Get user profile",
        description = "Returns the authenticated user's profile information extracted from the JWT token."
    )
    @SecurityRequirement(name = "jwt")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "User profile returned successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = String.class)
            )
        ),
        @APIResponse(
            responseCode = "401",
            description = "Unauthorized - JWT token is missing or invalid"
        )
    })
    @GET
    @Path("/user-profile")
    @RolesAllowed("user")
    public String getUserProfile() {
        String principalName = upn.getValue() != null ? upn.getValue() : jwt.getName();
        Set<String> userRoles = groups.getValue();
        String tenantId = tenant.getValue().orElse("N/A");

        return "User: " + principalName + ", Roles: " + userRoles + ", Tenant: " + tenantId;
    }  
}
