package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone("9876543210");
        user.setPassword("encodedPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setTokenVersion(0);

        return userRepository.saveAndFlush(user);
    }

    private Post createPost(User user, String content) {
        Post post = new Post();
        post.setUser(user);
        post.setText(content);

        return postRepository.saveAndFlush(post);
    }

    private Comment createComment(User user, Post post, String text) {
        return commentRepository.saveAndFlush(
                new Comment(user, post, text)
        );
    }

    @Test
    void shouldReturnCommentsInNewestFirstOrderWithPagination()
            throws InterruptedException {

        User user = createUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        createComment(user, post, "First comment");

        Thread.sleep(50);

        createComment(user, post, "Second comment");

        Thread.sleep(50);

        createComment(user, post, "Third comment");

        Page<Comment> result =
                commentRepository.findByPostPostIdOrderByCreatedAtDesc(
                        post.getPostId(),
                        PageRequest.of(0, 2)
                );

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        assertEquals("Third comment",
                result.getContent().get(0).getText());

        assertEquals("Second comment",
                result.getContent().get(1).getText());
    }

    @Test
    void shouldReturnCorrectCommentCount() {

        User user = createUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        createComment(user, post, "Comment 1");
        createComment(user, post, "Comment 2");
        createComment(user, post, "Comment 3");

        assertEquals(
                3,
                commentRepository.countByPostPostId(post.getPostId())
        );
    }

    @Test
    void shouldFindCommentByCommentIdAndPostId() {

        User user = createUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        Comment comment = createComment(user, post, "Hello");

        Optional<Comment> result =
                commentRepository.findByCommentIdAndPostPostId(
                        comment.getCommentId(),
                        post.getPostId()
                );

        assertTrue(result.isPresent());
        assertEquals("Hello", result.get().getText());
    }

    @Test
    void shouldReturnEmptyWhenCommentDoesNotBelongToPost() {

        User user = createUser("user1", "user1@test.com");

        Post post1 = createPost(user, "First post");
        Post post2 = createPost(user, "Second post");

        Comment comment = createComment(user, post1, "Hello");

        Optional<Comment> result =
                commentRepository.findByCommentIdAndPostPostId(
                        comment.getCommentId(),
                        post2.getPostId()
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteAllCommentsForPost() {

        User user = createUser("user1", "user1@test.com");

        Post post1 = createPost(user, "First post");
        Post post2 = createPost(user, "Second post");

        createComment(user, post1, "Comment 1");
        createComment(user, post1, "Comment 2");
        createComment(user, post2, "Comment 3");

        int deletedCount =
                commentRepository.deleteAllByPostId(post1.getPostId());

        entityManager.clear();

        assertEquals(2, deletedCount);

        assertEquals(0,
                commentRepository.countByPostPostId(post1.getPostId()));

        assertEquals(1,
                commentRepository.countByPostPostId(post2.getPostId()));
    }
}