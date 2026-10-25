
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.PublicPostResponse;
import com.suryaar.socialmedia.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PublicPostController {

    private final UserService userService;

    public PublicPostController(UserService userService) {
        this.userService = userService;
    }


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
                userService.getPublicPost(postId, viewerUserId);

        return ResponseEntity.ok(response);
    }

}
