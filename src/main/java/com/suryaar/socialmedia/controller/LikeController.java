
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.service.LikeService;
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

@Tag(name = "Likes", description = "Like and unlike posts")
@RestController
@RequestMapping("/api/posts/{postId}/like")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(
            summary = "Like a post",
            description = "Adds a like to the specified post on behalf of the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post liked successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<Void> likePost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        likeService.likePost(userId, postId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Unlike a post",
            description = "Removes the authenticated user's like from the specified post."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post unliked successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        likeService.unlikePost(userId, postId);

        return ResponseEntity.noContent().build();
    }
}
