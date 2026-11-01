
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CommentResponse;
import com.suryaar.socialmedia.dto.CreateCommentRequest;
import com.suryaar.socialmedia.dto.UpdateCommentRequest;
import com.suryaar.socialmedia.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;

@Tag(name = "Comments", description = "View, create, update and delete comments")
@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(
            summary = "Create a comment",
            description = "Creates a new comment on the specified post for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment details"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        CommentResponse response =
                commentService.createComment(userId, postId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get post comments",
            description = "Retrieves a paginated list of comments belonging to the specified post."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        Page<CommentResponse> comments =
                commentService.getComments(userId, postId, page, size);

        return ResponseEntity.ok(comments);
    }

    @Operation(
            summary = "Update a comment",
            description = "Updates an existing comment belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid comment details"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not authorized to update this comment"),
            @ApiResponse(responseCode = "404", description = "Post or comment not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        CommentResponse response = commentService.updateComment(
                userId, postId, commentId, request
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a comment",
            description = "Deletes an existing comment belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "403", description = "User is not authorized to delete this comment"),
            @ApiResponse(responseCode = "404", description = "Post or comment not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal(); 

        commentService.deleteComment(userId, postId, commentId);

        return ResponseEntity.noContent().build();
    }

}
