
package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.*;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.exception.InvalidCredentialsException;
import com.suryaar.socialmedia.repository.FollowRepository;
import com.suryaar.socialmedia.repository.PostRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import com.suryaar.socialmedia.security.TokenRevocationService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.suryaar.socialmedia.exception.DuplicateResourceException;

import java.util.Optional;
import com.suryaar.socialmedia.security.JwtService;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import com.suryaar.socialmedia.dto.PublicPostResponse;

import com.suryaar.socialmedia.dto.MediaResponse;
import com.suryaar.socialmedia.repository.CommentRepository;
import com.suryaar.socialmedia.repository.LikeRepository;
import com.suryaar.socialmedia.repository.PostMediaRepository;
import java.util.List;

import com.suryaar.socialmedia.entity.Post;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, TokenRevocationService tokenRevocationService, FollowRepository followRepository, PostRepository postRepository, PostMediaRepository postMediaRepository, LikeRepository likeRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRevocationService = tokenRevocationService;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.postMediaRepository = postMediaRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
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


    @Transactional(readOnly = true)
    public MeResponse getMyProfile(Long userId) {

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new InvalidCredentialsException("User not found"));

        return new MeResponse(
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getProfilePicture()
        );
    }


    @Transactional
    public MeResponse updateMyProfile(
            Long userId,
            UpdateMyProfileRequest request) {

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new InvalidCredentialsException("User not found"));

        if (request.getUsername() != null) {
            if (request.getUsername().isBlank()) {
                throw new IllegalArgumentException(
                        "Username cannot be blank");
            }

            if (!request.getUsername().equals(user.getUsername())
                    && userRepository.existsByUsernameAndStatus(
                    request.getUsername(), UserStatus.ACTIVE)) {
                throw new DuplicateResourceException(
                        "Username already exists");
            }

            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            if (request.getEmail().isBlank()) {
                throw new IllegalArgumentException(
                        "Email cannot be blank");
            }

            if (!request.getEmail().equalsIgnoreCase(user.getEmail())
                    && userRepository.existsByEmailAndStatus(
                    request.getEmail(), UserStatus.ACTIVE)) {
                throw new DuplicateResourceException(
                        "Email already exists");
            }

            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            if (request.getPhone().isBlank()) {
                throw new IllegalArgumentException(
                        "Phone cannot be blank");
            }

            if (!request.getPhone().equals(user.getPhone())
                    && userRepository.existsByPhoneAndStatus(
                    request.getPhone(), UserStatus.ACTIVE)) {
                throw new DuplicateResourceException(
                        "Phone already exists");
            }

            user.setPhone(request.getPhone());
        }

        User savedUser = userRepository.save(user);

        return new MeResponse(
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getPhone(),
                savedUser.getProfilePicture()
        );
    }


    @Transactional
    public void deleteMyAccount(Long userId) {

        User user = userRepository.findById(userId)
                .filter(existingUser ->
                        existingUser.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new InvalidCredentialsException("User not found"));

        followRepository.deleteAllRelationshipsForUser(userId);

        postRepository.deleteAllByUserId(userId);

        int updatedRows = userRepository.softDeleteUser(userId);

        if (updatedRows != 1) {
            throw new IllegalStateException(
                    "Account deletion could not be completed");
        }
    }


    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(
            String username,
            Long viewerUserId) {

        User profileUser = userRepository
                .findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "User not found"));

        long followersCount =
                followRepository.countByFollowing_UserId(
                        profileUser.getUserId());

        long followingCount =
                followRepository.countByFollower_UserId(
                        profileUser.getUserId());

        Boolean isFollowing = null;

        if (viewerUserId != null
                && !viewerUserId.equals(profileUser.getUserId())) {

            isFollowing =
                    followRepository
                            .existsByFollower_UserIdAndFollowing_UserId(
                                    viewerUserId,
                                    profileUser.getUserId());
        }

        return new PublicProfileResponse(
                profileUser.getUsername(),
                profileUser.getProfilePicture(),
                followersCount,
                followingCount,
                isFollowing);
    }

    @Transactional(readOnly = true)
    public Page<PublicPostResponse> getPublicPosts(
            String username, int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        User user = userRepository
                .findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending()
        );


        return postRepository
                .findByUserUsernameAndUserStatusOrderByCreatedAtDesc(
                        user.getUsername(), UserStatus.ACTIVE, pageable
                )
                .map(post -> {
                    List<MediaResponse> mediaResponses =
                            postMediaRepository
                                    .findByPostPostIdOrderByPositionAsc(
                                            post.getPostId()
                                    )
                                    .stream()
                                    .map(pm -> {
                                        var media = pm.getMedia();

                                        return new MediaResponse(
                                                media.getMediaId(),
                                                media.getType(),
                                                media.getUrl(),
                                                media.getName(),
                                                media.getSize()
                                        );
                                    })
                                    .toList();

                    long likeCount =
                            likeRepository.countByPostPostId(post.getPostId());

                    long commentCount =
                            commentRepository.countByPostPostId(post.getPostId());

                    return new PublicPostResponse(
                            post.getPostId(),
                            post.getUser().getUsername(),
                            post.getUser().getProfilePicture(),
                            post.getText(),
                            mediaResponses,
                            likeCount,
                            commentCount,
                            post.getCreatedAt(),
                            post.getUpdatedAt(),
                            null
                    );
                });
    }

    @Transactional(readOnly = true)
    public PublicPostResponse getPublicPost(Long postId, Long viewerUserId) {

        Post post = postRepository
                .findByPostIdAndUserStatus(postId, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Post not found"
                ));

        List<MediaResponse> mediaResponses =
                postMediaRepository
                        .findByPostPostIdOrderByPositionAsc(post.getPostId())
                        .stream()
                        .map(pm -> {
                            var media = pm.getMedia();

                            return new MediaResponse(
                                    media.getMediaId(),
                                    media.getType(),
                                    media.getUrl(),
                                    media.getName(),
                                    media.getSize()
                            );
                        })
                        .toList();

        long likeCount = likeRepository.countByPostPostId(postId);
        long commentCount = commentRepository.countByPostPostId(postId);

        Boolean likedByMe = viewerUserId == null
                ? null
                : likeRepository.existsByUserUserIdAndPostPostId(
                viewerUserId, postId
        );

        return new PublicPostResponse(
                post.getPostId(),
                post.getUser().getUsername(),
                post.getUser().getProfilePicture(),
                post.getText(),
                mediaResponses,
                likeCount,
                commentCount,
                post.getCreatedAt(),
                post.getUpdatedAt(),
                likedByMe
        );
    }
}
