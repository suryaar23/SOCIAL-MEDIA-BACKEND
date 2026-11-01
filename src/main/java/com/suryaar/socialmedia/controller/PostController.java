package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreatePostRequest;
import com.suryaar.socialmedia.dto.PostResponse;
import com.suryaar.socialmedia.dto.UpdatePostRequest;
import com.suryaar.socialmedia.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Posts", description = "Create, update, view and delete posts")
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "Create a post",
            description = "Creates a new post for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid post details"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return postService.createPost(userId, request);
    }

    @Operation(
            summary = "Update a post",
            description = "Updates the specified post belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid post details"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to update this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{postId}")
    public PostResponse updatePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return postService.updatePost(userId, postId, request);
    }

    @Operation(
            summary = "Delete a post",
            description = "Deletes the specified post belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to delete this post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(userId, postId);
    }
}