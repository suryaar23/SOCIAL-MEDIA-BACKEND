
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.PublicProfileResponse;
import com.suryaar.socialmedia.service.ProfileService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class PublicProfileController {

    private final ProfileService profileService;

    public PublicProfileController(ProfileService profileService) {
        this.profileService = profileService;
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
                profileService.getPublicProfile(username, viewerUserId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public Page<PublicProfileResponse> searchUsers(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal(errorOnInvalidType = false)
            Long viewerUserId
    ) {
        return profileService.searchUsers(
                username,
                page,
                size,
                viewerUserId
        );
    }
}
    