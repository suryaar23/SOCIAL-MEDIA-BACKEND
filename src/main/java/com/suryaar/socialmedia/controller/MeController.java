
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.MeResponse;
import com.suryaar.socialmedia.dto.UpdateMyProfileRequest;
import com.suryaar.socialmedia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<MeResponse> getMyProfile(
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = (Long) authentication.getPrincipal();

        MeResponse response = userService.getMyProfile(userId);

        return ResponseEntity.ok(response);
    }


    @PatchMapping
    public ResponseEntity<MeResponse> updateMyProfile(
            @Valid @RequestBody UpdateMyProfileRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        MeResponse response =
                userService.updateMyProfile(userId, request);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteMyAccount(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        userService.deleteMyAccount(userId);

        return ResponseEntity.noContent().build();
    }

}
