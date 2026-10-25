
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.FollowUserResponse;
import com.suryaar.socialmedia.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{username}")
public class FollowListController {

    private final UserService userService;

    public FollowListController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/followers")
    public ResponseEntity<Page<FollowUserResponse>> getFollowers(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Page<FollowUserResponse> followers =
                userService.getFollowers(username, page, size);

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/following")
    public ResponseEntity<Page<FollowUserResponse>> getFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Page<FollowUserResponse> following =
                userService.getFollowing(username, page, size);

        return ResponseEntity.ok(following);
    }
}
