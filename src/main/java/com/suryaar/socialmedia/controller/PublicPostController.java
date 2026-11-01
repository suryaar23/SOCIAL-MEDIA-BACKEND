
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.PublicPostResponse;
import com.suryaar.socialmedia.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Public Post", description = "Retrieve details of a specific publicly accessible post")
@RestController
@RequestMapping("/api/posts")
public class PublicPostController {

    private final PostService postService;

    public PublicPostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "Get a public post",
            description = "Retrieves a publicly accessible post. Authentication is optional; when provided, the viewer's identity is used."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{postId}")
    public ResponseEntity<PublicPostResponse> getPublicPost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long viewerUserId = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Long userId) {
            viewerUserId = userId;
        }

        PublicPostResponse response =
                postService.getPublicPost(postId, viewerUserId);

        return ResponseEntity.ok(response);
    }
}
