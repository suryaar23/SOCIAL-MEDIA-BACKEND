package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreatePostRequest;
import com.suryaar.socialmedia.dto.PostResponse;
import com.suryaar.socialmedia.dto.UpdatePostRequest;
import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return postService.createPost(userId, request);
    }

    @PatchMapping("/{postId}")
    public PostResponse updatePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        return postService.updatePost(userId, postId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(userId, postId);
    }
}