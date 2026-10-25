
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.PublicProfileResponse;
import com.suryaar.socialmedia.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class PublicProfileController {

    private final UserService userService;

    public PublicProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(
            @PathVariable String username,
            Authentication authentication) {

        Long viewerUserId = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof Long id) {
            viewerUserId = id;
        }

        PublicProfileResponse response =
                userService.getPublicProfile(username, viewerUserId);

        return ResponseEntity.ok(response);
    }
}
    