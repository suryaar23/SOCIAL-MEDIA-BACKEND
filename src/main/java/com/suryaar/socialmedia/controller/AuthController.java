
package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.CreateUserRequest;
import com.suryaar.socialmedia.dto.UserResponse;
import com.suryaar.socialmedia.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Authentication", description = "User registration, login, logout and password management")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid registration details"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists"
            )
    })
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account using the provided registration details."
    )
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse response = authService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or validation failure"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "invalid credentials"
            )
    })
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates a user using their login credentials and returns the login response."
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.loginUser(request);

        return ResponseEntity.ok(response);
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Logout successful"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token"
            )
    })
    @Operation(
            summary = "Logout current user",
            description = "Logs out the authenticated user by processing the access token provided in the Authorization header."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authorizationHeader =
                request.getHeader("Authorization");

        authService.logoutUser(authorizationHeader);

        return ResponseEntity.noContent().build();
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Password changed successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or password validation failure"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication token"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Current password incorrect"
            )
    })
    @Operation(
            summary = "Change account password",
            description = "Changes the password of the currently authenticated user using the supplied password details."
    )
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        authService.changePassword(userId, request);

        return ResponseEntity.noContent().build();
    }
}
