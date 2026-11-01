package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.CommentResponse;
import com.suryaar.socialmedia.dto.CreateCommentRequest;
import com.suryaar.socialmedia.dto.UpdateCommentRequest;
import com.suryaar.socialmedia.entity.Comment;
import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.CommentRepository;
import com.suryaar.socialmedia.repository.PostRepository;
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

class CommentServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    private CommentService commentService;
    private AutoCloseable mocks;

    private User user;
    private Post post;
    private Comment comment;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        commentService = new CommentService(
                userRepository, postRepository, commentRepository);

        user = new User();
        user.setUserId(1L);
        user.setUsername("surya");
        user.setStatus(UserStatus.ACTIVE);

        post = new Post();
        post.setPostId(10L);

        comment = new Comment(user, post, "Original comment");
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldCreateCommentSuccessfully() {
        CreateCommentRequest request = new CreateCommentRequest("  Nice post!  ");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(postRepository.findByPostIdAndUserStatus(
                10L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommentResponse response =
                commentService.createComment(1L, 10L, request);

        assertEquals("Nice post!", response.getText());
        assertEquals("surya", response.getUsername());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldRejectCommentForMissingPost() {
        CreateCommentRequest request = new CreateCommentRequest("Nice post!");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(postRepository.findByPostIdAndUserStatus(
                10L, UserStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> commentService.createComment(1L, 10L, request));

        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldGetCommentsSuccessfully() {
        comment.setCommentId(100L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(postRepository.findByPostIdAndUserStatus(
                10L, UserStatus.ACTIVE))
                .thenReturn(Optional.of(post));
        when(commentRepository.findByPostPostIdOrderByCreatedAtDesc(
                eq(10L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(comment)));

        Page<CommentResponse> result =
                commentService.getComments(1L, 10L, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Original comment", result.getContent().get(0).getText());
    }

    @Test
    void shouldUpdateOwnCommentSuccessfully() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(commentRepository.findByCommentIdAndPostPostId(100L, 10L))
                .thenReturn(Optional.of(comment));

        UpdateCommentRequest request =
                new UpdateCommentRequest("  Updated comment  ");

        CommentResponse response =
                commentService.updateComment(1L, 10L, 100L, request);

        assertEquals("Updated comment", response.getText());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void shouldRejectUpdatingAnotherUsersComment() {
        User anotherUser = new User();
        anotherUser.setUserId(2L);
        anotherUser.setUsername("anotherUser");
        anotherUser.setStatus(UserStatus.ACTIVE);

        Comment anotherComment =
                new Comment(anotherUser, post, "Their comment");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(commentRepository.findByCommentIdAndPostPostId(100L, 10L))
                .thenReturn(Optional.of(anotherComment));

        UpdateCommentRequest request =
                new UpdateCommentRequest("Attempted update");

        assertThrows(ResponseStatusException.class,
                () -> commentService.updateComment(1L, 10L, 100L, request));

        assertEquals("Their comment", anotherComment.getText());
    }

    @Test
    void shouldDeleteOwnCommentSuccessfully() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(commentRepository.findByCommentIdAndPostPostId(100L, 10L))
                .thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, 10L, 100L);

        verify(commentRepository).delete(comment);
    }
}