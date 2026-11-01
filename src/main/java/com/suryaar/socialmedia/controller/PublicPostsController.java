
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.PublicPostResponse;
import com.suryaar.socialmedia.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Posts", description = "View posts created by a specific user")
@RestController
@RequestMapping("/api/users")
public class PublicPostsController {

    private final PostService postService;

    public PublicPostsController(PostService postService) {
        this.postService = postService;
    }

    @Operation(
            summary = "Get a user's public posts",
            description = "Retrieves a paginated list of publicly accessible posts created by the specified user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{username}/posts")
    public ResponseEntity<Page<PublicPostResponse>> getPublicPosts(
            @PathVariable
            @Parameter(description = "Username of the post author", example = "surya_dev")
            String username,

            @RequestParam(defaultValue = "0")
            @Parameter(description = "Page number (zero-based)", example = "0")
            int page,

            @RequestParam(defaultValue = "10")
            @Parameter(description = "Number of posts per page", example = "10")
            int size) {

        Page<PublicPostResponse> posts =
                postService.getPublicPosts(username, page, size);

        return ResponseEntity.ok(posts);
    }
}
