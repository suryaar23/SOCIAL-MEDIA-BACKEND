package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.entity.Like;
import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.LikeRepository;
import com.suryaar.socialmedia.repository.PostRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LikeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private LikeRepository likeRepository;

    private LikeService likeService;
    private AutoCloseable mocks;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        likeService = new LikeService(
                userRepository,
                postRepository,
                likeRepository
        );

        user = new User();
        user.setUserId(1L);
        user.setStatus(UserStatus.ACTIVE);

        post = new Post();
        post.setPostId(10L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldLikePostSuccessfully() {

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(postRepository.findByPostIdAndUserStatus(
                10L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(post));

        when(likeRepository.existsByUserUserIdAndPostPostId(1L, 10L))
                .thenReturn(false);

        likeService.likePost(1L, 10L);

        verify(likeRepository).save(any(Like.class));
    }

    @Test
    void shouldNotSaveDuplicateLike() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(postRepository.findByPostIdAndUserStatus(
                10L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(post));

        when(likeRepository.existsByUserUserIdAndPostPostId(1L, 10L))
                .thenReturn(true);

        likeService.likePost(1L, 10L);

        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void shouldRejectLikeWhenUserDoesNotExist() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> likeService.likePost(1L, 10L));

        verifyNoInteractions(postRepository, likeRepository);
    }

    @Test
    void shouldRejectLikeWhenPostDoesNotExist() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(postRepository.findByPostIdAndUserStatus(
                10L, UserStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> likeService.likePost(1L, 10L));

        verify(likeRepository, never()).save(any(Like.class));
    }

    @Test
    void shouldUnlikePostSuccessfully() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(postRepository.findByPostIdAndUserStatus(
                10L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(post));

        likeService.unlikePost(1L, 10L);

        verify(likeRepository).deleteLike(1L, 10L);
    }

    @Test
    void shouldRejectUnlikeWhenUserIsInactive() {
        user.setStatus(UserStatus.DELETED);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class,
                () -> likeService.unlikePost(1L, 10L));

        verifyNoInteractions(postRepository, likeRepository);
    }
}