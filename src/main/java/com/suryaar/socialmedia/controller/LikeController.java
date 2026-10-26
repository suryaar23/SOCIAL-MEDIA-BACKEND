
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.service.LikeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts/{postId}/like")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public ResponseEntity<Void> likePost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        likeService.likePost(userId, postId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long postId,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        likeService.unlikePost(userId, postId);

        return ResponseEntity.noContent().build();
    }
}
