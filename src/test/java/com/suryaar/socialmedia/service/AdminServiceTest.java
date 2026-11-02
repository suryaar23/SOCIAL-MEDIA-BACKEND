package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.AdminPostResponse;
import com.suryaar.socialmedia.dto.AdminUserResponse;
import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.*;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private CommentRepository commentRepository;

    private AdminService adminService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

//        adminService = new AdminService(
//                userRepository,
//                postRepository,
//                postMediaRepository,
//                likeRepository,
//                commentRepository
//        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldGetAdminUsersSuccessfully() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("surya");
        user.setEmail("surya@example.com");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findAllByOrderByCreatedAtDesc(
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        Page<AdminUserResponse> result =
                adminService.getAdminUsers(0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("surya", result.getContent().get(0).getUsername());
    }

    @Test
    void shouldGetAdminPostsSuccessfully() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("surya");

        Post post = new Post();
        post.setPostId(10L);
        post.setUser(user);
        post.setText("Test post");

        when(postRepository.findByUserStatusOrderByCreatedAtDesc(
                eq(UserStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(post)));

        when(postMediaRepository.findByPostPostIdOrderByPositionAsc(10L))
                .thenReturn(List.of());

        when(likeRepository.countByPostPostId(10L))
                .thenReturn(5L);

        when(commentRepository.countByPostPostId(10L))
                .thenReturn(3L);

        Page<AdminPostResponse> result =
                adminService.getAdminPosts(0, 10);

        assertEquals(1, result.getTotalElements());

        AdminPostResponse response = result.getContent().get(0);
        assertEquals(10L, response.getPostId());
        assertEquals("surya", response.getUsername());
        assertEquals(5L, response.getLikeCount());
        assertEquals(3L, response.getCommentCount());
    }

    @Test
    void shouldRejectInvalidPagination() {
        assertThrows(ResponseStatusException.class,
                () -> adminService.getAdminUsers(-1, 10));

        assertThrows(ResponseStatusException.class,
                () -> adminService.getAdminPosts(0, 101));

        verifyNoInteractions(
                userRepository,
                postRepository,
                postMediaRepository,
                likeRepository,
                commentRepository
        );
    }


}