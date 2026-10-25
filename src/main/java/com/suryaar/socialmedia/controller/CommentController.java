
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CommentResponse;
import com.suryaar.socialmedia.dto.CreateCommentRequest;
import com.suryaar.socialmedia.dto.UpdateCommentRequest;
import com.suryaar.socialmedia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final UserService userService;

    public CommentController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        CommentResponse response =
                userService.createComment(userId, postId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        Page<CommentResponse> comments =
                userService.getComments(userId, postId, page, size);

        return ResponseEntity.ok(comments);
    }


    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        CommentResponse response = userService.updateComment(
                userId, postId, commentId, request
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        userService.deleteComment(userId, postId, commentId);

        return ResponseEntity.noContent().build();
    }

}
