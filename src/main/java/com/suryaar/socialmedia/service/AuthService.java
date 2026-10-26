package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.*;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.exception.DuplicateResourceException;
import com.suryaar.socialmedia.exception.InvalidCredentialsException;
import com.suryaar.socialmedia.repository.UserRepository;
import com.suryaar.socialmedia.security.JwtService;
import com.suryaar.socialmedia.security.TokenRevocationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, TokenRevocationService tokenRevocationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
    }


    @Transactional
    public UserResponse registerUser(CreateUserRequest request) {

        if (userRepository.existsByUsernameAndStatus(request.getUsername(), UserStatus.ACTIVE)) {
            throw new DuplicateResourceException("Username is already taken");
        }

        if (userRepository.existsByEmailAndStatus(request.getEmail(), UserStatus.ACTIVE)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        if (userRepository.existsByPhoneAndStatus(request.getPhone(), UserStatus.ACTIVE)) {
            throw new DuplicateResourceException("Phone number is already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getUserId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getProfilePicture()
        );
    }


    public LoginResponse loginUser(LoginRequest request) {

        String identifier = request.getIdentifier().trim();

        Optional<User> userOptional;

        if (identifier.contains("@")) {
            userOptional = userRepository.findByEmailAndStatus(
                    identifier, UserStatus.ACTIVE);
        } else {
            userOptional = userRepository.findByUsernameAndStatus(
                    identifier, UserStatus.ACTIVE);
        }

        User user = userOptional
                .filter(existingUser ->
                        passwordEncoder.matches(
                                request.getPassword(),
                                existingUser.getPassword()))
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid username/email or password"));

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                jwtExpirationMs / 1000
        );
    }


    public void logoutUser(String authorizationHeader) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            throw new InvalidCredentialsException(
                    "A valid Bearer token is required");
        }

        String token = authorizationHeader.substring(7);

        try {
            Long userId = jwtService.extractUserId(token);

            if (!jwtService.isTokenValid(token, userId)) {
                throw new InvalidCredentialsException("Invalid token");
            }

            String tokenId = jwtService.extractTokenId(token);
            var expiresAt = jwtService.extractExpiration(token);

            tokenRevocationService.revokeToken(
                    tokenId, userId, expiresAt);

        } catch (InvalidCredentialsException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new InvalidCredentialsException("Invalid token");
        }
    }



    @Transactional
    public void changePassword(
            Long userId,
            ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .filter(existingUser ->
                        existingUser.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new InvalidCredentialsException(
                                "Invalid authenticated user"));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(
                    "Current password is incorrect");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException(
                    "New password and confirmation do not match");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException(
                    "New password must differ from the current password");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword()));

        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
    }
}
