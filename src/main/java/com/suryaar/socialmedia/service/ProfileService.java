package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.MeResponse;
import com.suryaar.socialmedia.dto.PublicProfileResponse;
import com.suryaar.socialmedia.dto.UpdateMyProfileRequest;
import com.suryaar.socialmedia.entity.*;
import com.suryaar.socialmedia.exception.DuplicateResourceException;
import com.suryaar.socialmedia.exception.InvalidCredentialsException;
import com.suryaar.socialmedia.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    public ProfileService(UserRepository userRepository, FollowRepository followRepository, PostRepository postRepository, PostService postService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.postRepository = postRepository;
        this.postService = postService;
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

        List<Post> userPosts = postRepository.findByUserUserId(userId);

        for (Post post : userPosts) {
            postService.deletePost(userId, post.getPostId());
        }

        followRepository.deleteAllRelationshipsForUser(userId);

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
    public Page<PublicProfileResponse> searchUsers(
            String username,
            int page,
            int size,
            Long viewerUserId
    ) {
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Search username cannot be empty"
            );
        }

        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "username")
        );

        Page<User> users = userRepository
                .findByStatusAndUsernameContainingIgnoreCase(
                        UserStatus.ACTIVE,
                        username.trim(),
                        pageable
                );

        return users.map(user -> {
            Boolean isFollowing = null;

            if (viewerUserId != null
                    && !viewerUserId.equals(user.getUserId())) {

                isFollowing = followRepository
                        .existsByFollower_UserIdAndFollowing_UserId(
                                viewerUserId,
                                user.getUserId()
                        );
            }

            return new PublicProfileResponse(
                    user.getUsername(),
                    user.getProfilePicture(),
                    followRepository.countByFollowing_UserId(
                            user.getUserId()
                    ),
                    followRepository.countByFollower_UserId(
                            user.getUserId()
                    ),
                    isFollowing
            );
        });
    }
}
