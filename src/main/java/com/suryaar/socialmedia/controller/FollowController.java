
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{username}/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping
    public ResponseEntity<Void> followUser(
            @PathVariable String username,
            Authentication authentication) {

        Long followerId = (Long) authentication.getPrincipal();

        followService.followUser(followerId, username);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unfollowUser(
            @PathVariable String username,
            Authentication authentication) {

        Long followerId = (Long) authentication.getPrincipal();

        followService.unfollowUser(followerId, username);

        return ResponseEntity.noContent().build();
    }
}
