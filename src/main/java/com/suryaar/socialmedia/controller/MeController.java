
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.MeResponse;
import com.suryaar.socialmedia.dto.UpdateMyProfileRequest;
import com.suryaar.socialmedia.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "My Profile", description = "View, update and delete the authenticated user's profile")
@RestController
@RequestMapping("/api/users/me")
public class MeController {

    private final ProfileService profileService;

    public MeController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(
            summary = "Get my profile",
            description = "Retrieves the profile of the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<MeResponse> getMyProfile(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = (Long) authentication.getPrincipal();

        MeResponse response = profileService.getMyProfile(userId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update my profile",
            description = "Updates the profile details of the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid profile details"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping
    public ResponseEntity<MeResponse> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        MeResponse response =
                profileService.updateMyProfile(userId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete my account",
            description = "Deletes the account of the currently authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> deleteMyAccount(
            Authentication authentication) throws IOException {

        Long userId = (Long) authentication.getPrincipal();

        profileService.deleteMyAccount(userId);

        return ResponseEntity.noContent().build();
    }
}
