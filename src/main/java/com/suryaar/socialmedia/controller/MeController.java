
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.MeResponse;
import com.suryaar.socialmedia.dto.UpdateMyProfileRequest;
import com.suryaar.socialmedia.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/users/me")
public class MeController {

    private final ProfileService profileService;

    public MeController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<MeResponse> getMyProfile(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = (Long) authentication.getPrincipal();

        MeResponse response = profileService.getMyProfile(userId);

        return ResponseEntity.ok(response);
    }


    @PatchMapping
    public ResponseEntity<MeResponse> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        MeResponse response =
                profileService.updateMyProfile(userId, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteMyAccount(
            Authentication authentication) throws IOException {

        Long userId = (Long) authentication.getPrincipal();

        profileService.deleteMyAccount(userId);

        return ResponseEntity.noContent().build();
    }

}
