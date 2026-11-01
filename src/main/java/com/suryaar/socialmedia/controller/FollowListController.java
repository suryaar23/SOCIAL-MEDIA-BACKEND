
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.FollowUserResponse;
import com.suryaar.socialmedia.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Followers & Following", description = "View a user's followers and following lists")
@RestController
@RequestMapping("/api/users/{username}")
public class FollowListController {

    private final FollowService followService;

    public FollowListController(FollowService followService) {
        this.followService = followService;
    }

    @Operation(
            summary = "Get user's followers",
            description = "Retrieves a paginated list of users who follow the specified user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Followers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/followers")
    public ResponseEntity<Page<FollowUserResponse>> getFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Page<FollowUserResponse> followers =
                followService.getFollowers(username, page, size);

        return ResponseEntity.ok(followers);
    }

    @Operation(
            summary = "Get users being followed",
            description = "Retrieves a paginated list of users followed by the specified user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Following list retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/following")
    public ResponseEntity<Page<FollowUserResponse>> getFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Page<FollowUserResponse> following =
                followService.getFollowing(username, page, size);

        return ResponseEntity.ok(following);
    }
}
