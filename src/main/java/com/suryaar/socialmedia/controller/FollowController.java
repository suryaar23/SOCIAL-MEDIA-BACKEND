
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Follow Management", description = "Follow and unfollow users")
@RestController
@RequestMapping("/api/users/{username}/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @Operation(
            summary = "Follow a user",
            description = "Allows the authenticated user to follow another user identified by username."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User followed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid follow request"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Target user not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<Void> followUser(
            @PathVariable String username,
            Authentication authentication) {

        Long followerId = (Long) authentication.getPrincipal();

        followService.followUser(followerId, username);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Unfollow a user",
            description = "Removes the authenticated user's follow relationship with the specified user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User unfollowed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Target user not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> unfollowUser(
            @PathVariable String username,
            Authentication authentication) {

        Long followerId = (Long) authentication.getPrincipal();

        followService.unfollowUser(followerId, username);

        return ResponseEntity.noContent().build();
    }
}
