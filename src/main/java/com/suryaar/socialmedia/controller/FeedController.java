
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.PublicPostResponse;
import com.suryaar.socialmedia.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Feed", description = "Retrieve the authenticated user's feed")
@RestController
@RequestMapping("/api/users/me/feed")
public class FeedController {

    private final PostService postService;

    public FeedController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "Get personalized feed",
            description = "Retrieves a paginated feed of posts for the authenticated user, ordered by newest first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Feed retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<Page<PublicPostResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        Page<PublicPostResponse> feed =
                postService.getFeed(userId, page, size);

        return ResponseEntity.ok(feed);
    }
}
