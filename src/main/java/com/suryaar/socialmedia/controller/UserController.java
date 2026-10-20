
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.UserResponse;
import com.suryaar.socialmedia.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.suryaar.socialmedia.dto.LoginRequest;
import com.suryaar.socialmedia.dto.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import com.suryaar.socialmedia.dto.ChangePasswordRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = userService.loginUser(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authorizationHeader =
                request.getHeader("Authorization");

        userService.logoutUser(authorizationHeader);

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        userService.changePassword(userId, request);

        return ResponseEntity.noContent().build();
    }

}
