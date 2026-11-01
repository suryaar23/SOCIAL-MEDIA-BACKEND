package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.FollowUserResponse;
import com.suryaar.socialmedia.entity.Follow;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.FollowRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FollowServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    private FollowService followService;
    private AutoCloseable mocks;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        followService = new FollowService(
                userRepository, followRepository);

        follower = new User();
        follower.setUserId(1L);
        follower.setUsername("surya");
        follower.setStatus(UserStatus.ACTIVE);

        following = new User();
        following.setUserId(2L);
        following.setUsername("arun");
        following.setStatus(UserStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldFollowUserSuccessfully() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(follower));

        when(userRepository.findByUsernameAndStatus(
                "arun", UserStatus.ACTIVE))
                .thenReturn(Optional.of(following));

        when(followRepository
                .existsByFollower_UserIdAndFollowing_UserId(1L, 2L))
                .thenReturn(false);

        followService.followUser(1L, "arun");

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void shouldNotSaveDuplicateFollow() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(follower));

        when(userRepository.findByUsernameAndStatus(
                "arun", UserStatus.ACTIVE))
                .thenReturn(Optional.of(following));

        when(followRepository
                .existsByFollower_UserIdAndFollowing_UserId(1L, 2L))
                .thenReturn(true);

        followService.followUser(1L, "arun");

        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void shouldRejectFollowingYourself() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(follower));

        when(userRepository.findByUsernameAndStatus(
                "surya", UserStatus.ACTIVE))
                .thenReturn(Optional.of(follower));

        assertThrows(ResponseStatusException.class,
                () -> followService.followUser(1L, "surya"));

        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    void shouldUnfollowUserSuccessfully() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(follower));
        when(userRepository.findByUsernameAndStatus(
                "arun", UserStatus.ACTIVE))
                .thenReturn(Optional.of(following));

        followService.unfollowUser(1L, "arun");

        verify(followRepository).deleteFollow(1L, 2L);
    }

    @Test
    void shouldGetFollowersSuccessfully() {
        Follow follow = new Follow(follower, following);

        when(userRepository.findByUsernameAndStatus(
                "arun", UserStatus.ACTIVE))
                .thenReturn(Optional.of(following));

        when(followRepository.findByFollowing_UserIdOrderByFollowerUsernameAsc(
                eq(2L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowUserResponse> result =
                followService.getFollowers("arun", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("surya", result.getContent().get(0).getUsername());
    }

    @Test
    void shouldGetFollowingSuccessfully() {
        Follow follow = new Follow(follower, following);

        when(userRepository.findByUsernameAndStatus(
                "surya", UserStatus.ACTIVE))
                .thenReturn(Optional.of(follower));

        when(followRepository.findByFollower_UserIdOrderByFollowingUsernameAsc(
                eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(follow)));

        Page<FollowUserResponse> result =
                followService.getFollowing("surya", 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("arun", result.getContent().get(0).getUsername());
    }
}