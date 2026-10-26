package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.FollowUserResponse;
import com.suryaar.socialmedia.entity.Follow;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.FollowRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FollowService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public FollowService(UserRepository userRepository, FollowRepository followRepository) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
    }

    @Transactional
    public void followUser(Long followerId, String username) {

        User follower = userRepository.findById(followerId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        User following = userRepository
                .findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Target user not found"
                ));

        if (followerId.equals(following.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot follow yourself"
            );
        }

        boolean alreadyFollowing =
                followRepository
                        .existsByFollower_UserIdAndFollowing_UserId(
                                followerId, following.getUserId()
                        );

        if (alreadyFollowing) {
            return;
        }

        Follow follow = new Follow(follower, following);
        followRepository.save(follow);
    }


    @Transactional
    public void unfollowUser(Long followerId, String username) {

        userRepository.findById(followerId)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        User following = userRepository
                .findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Target user not found"
                ));

        if (followerId.equals(following.getUserId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You cannot unfollow yourself"
            );
        }

        followRepository.deleteFollow(
                followerId, following.getUserId()
        );
    }


    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getFollowers(
            String username, int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        User targetUser = userRepository
                .findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("follower.username").ascending()
        );

        return followRepository
                .findByFollowing_UserIdOrderByFollowerUsernameAsc(
                        targetUser.getUserId(), pageable
                )
                .map(follow -> new FollowUserResponse(
                        follow.getFollower().getUsername(),
                        follow.getFollower().getProfilePicture()
                ));
    }


    @Transactional(readOnly = true)
    public Page<FollowUserResponse> getFollowing(
            String username, int page, int size) {

        if (page < 0 || size < 1 || size > 50) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid pagination parameters"
            );
        }

        User targetUser = userRepository
                .findByUsernameAndStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"
                ));

        Pageable pageable = PageRequest.of(
                page, size, Sort.by("following.username").ascending()
        );

        return followRepository
                .findByFollower_UserIdOrderByFollowingUsernameAsc(
                        targetUser.getUserId(), pageable
                )
                .map(follow -> new FollowUserResponse(
                        follow.getFollowing().getUsername(),
                        follow.getFollowing().getProfilePicture()
                ));
    }
}
