
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.PublicProfileResponse;
import com.suryaar.socialmedia.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Public Profiles", description = "View and search public user profiles")
@RestController
@RequestMapping("/api/users")
public class PublicProfileController {

    private final ProfileService profileService;

    public PublicProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(
            summary = "Get a public profile",
            description = "Retrieves the public profile of the specified user. Authentication is optional; when provided, the viewer's identity is used."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(
            @PathVariable
            @Parameter(description = "Username of the profile to retrieve", example = "surya_dev")
            String username,
            Authentication authentication) {

        Long viewerUserId = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Long id) {
            viewerUserId = id;
        }

        PublicProfileResponse response =
                profileService.getPublicProfile(username, viewerUserId);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Search users",
            description = "Searches public user profiles by username and returns paginated results. Authentication is optional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search or pagination parameters")
    })
    @GetMapping("/search")
    public Page<PublicProfileResponse> searchUsers(
            @RequestParam
            @Parameter(description = "Username or username fragment to search for", example = "surya")
            String username,

            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (zero-based)", example = "0")
            int page,

            @RequestParam(defaultValue = "20")
            @Parameter(description = "Number of profiles per page", example = "20")
            int size,
            
            @AuthenticationPrincipal(errorOnInvalidType = false)
            Long viewerUserId
    ) {
        return profileService.searchUsers(
                username,
                page,
                size,
                viewerUserId
        );
    }
}
    