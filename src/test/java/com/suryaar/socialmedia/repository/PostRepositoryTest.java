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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private User createUser(
            String username,
            String email,
            UserStatus status) {

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone("9876543210");
        user.setPassword("encodedPassword");
        user.setStatus(status);
        user.setTokenVersion(0);

        return userRepository.saveAndFlush(user);
    }

    private User createActiveUser(String username, String email) {
        return createUser(username, email, UserStatus.ACTIVE);
    }

    private Post createPost(User user, String content) {
        Post post = new Post();
        post.setUser(user);
        post.setText(content);

        return postRepository.saveAndFlush(post);
    }

    @Test
    void shouldDeleteAllPostsForUser() {

        User user1 = createActiveUser("user1", "user1@test.com");
        User user2 = createActiveUser("user2", "user2@test.com");

        createPost(user1, "Post 1");
        createPost(user1, "Post 2");
        createPost(user2, "Post 3");

        int deletedCount =
                postRepository.deleteAllByUserId(user1.getUserId());

        entityManager.clear();

        assertEquals(2, deletedCount);

        assertTrue(postRepository.findByUserUserId(
                user1.getUserId()).isEmpty());

        assertEquals(1, postRepository.findByUserUserId(
                user2.getUserId()).size());
    }

    @Test
    void shouldFindUserPostsInNewestFirstOrderWithPagination()
            throws InterruptedException {

        User user = createActiveUser("user1", "user1@test.com");

        createPost(user, "First post");
        Thread.sleep(50);

        createPost(user, "Second post");
        Thread.sleep(50);

        createPost(user, "Third post");

        Page<Post> result =
                postRepository.findByUserUsernameAndUserStatusOrderByCreatedAtDesc(
                        "user1",
                        UserStatus.ACTIVE,
                        PageRequest.of(0, 2)
                );

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        assertEquals("Third post",
                result.getContent().get(0).getText());

        assertEquals("Second post",
                result.getContent().get(1).getText());
    }

    @Test
    void shouldFindPostWhenUserStatusMatches() {

        User user = createActiveUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        Optional<Post> result =
                postRepository.findByPostIdAndUserStatus(
                        post.getPostId(),
                        UserStatus.ACTIVE
                );

        assertTrue(result.isPresent());
        assertEquals(post.getPostId(),
                result.get().getPostId());
    }

    @Test
    void shouldNotFindPostWhenUserStatusDoesNotMatch() {

        User user = createActiveUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        Optional<Post> result =
                postRepository.findByPostIdAndUserStatus(
                        post.getPostId(),
                        UserStatus.DELETED
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFindPostsFromSpecifiedUsersExcludingCurrentUser()
            throws InterruptedException {

        User user1 = createActiveUser("user1", "user1@test.com");
        User user2 = createActiveUser("user2", "user2@test.com");
        User user3 = createActiveUser("user3", "user3@test.com");
        User excludedUser = createActiveUser(
                "excluded", "excluded@test.com");

        createPost(user1, "User 1 post");
        Thread.sleep(50);

        createPost(user2, "User 2 post");
        Thread.sleep(50);

        createPost(user3, "User 3 post");
        createPost(excludedUser, "Excluded user post");

        Page<Post> result =
                postRepository
                        .findByUserUserIdInAndUserStatusAndUserUserIdNotOrderByCreatedAtDesc(
                                List.of(
                                        user1.getUserId(),
                                        user2.getUserId(),
                                        user3.getUserId(),
                                        excludedUser.getUserId()
                                ),
                                UserStatus.ACTIVE,
                                excludedUser.getUserId(),
                                PageRequest.of(0, 2)
                        );

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        assertEquals("User 3 post",
                result.getContent().get(0).getText());

        assertEquals("User 2 post",
                result.getContent().get(1).getText());

        assertTrue(result.getContent().stream()
                .noneMatch(post -> post.getUser().getUserId()
                        .equals(excludedUser.getUserId())));
    }

    @Test
    void shouldFindPostsByUserStatusWithPagination()
            throws InterruptedException {

        User active1 = createActiveUser("active1", "active1@test.com");
        User active2 = createActiveUser("active2", "active2@test.com");
        User deleted = createUser(
                "deleted", "deleted@test.com", UserStatus.DELETED);

        createPost(active1, "First active post");
        Thread.sleep(50);

        createPost(active2, "Second active post");

        createPost(deleted, "Deleted user post");

        Page<Post> result =
                postRepository.findByUserStatusOrderByCreatedAtDesc(
                        UserStatus.ACTIVE,
                        PageRequest.of(0, 1)
                );

        assertEquals(1, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        assertEquals("Second active post",
                result.getContent().get(0).getText());
    }

    @Test
    void shouldReturnAllPostsForUser() {

        User user1 = createActiveUser("user1", "user1@test.com");
        User user2 = createActiveUser("user2", "user2@test.com");

        createPost(user1, "Post 1");
        createPost(user1, "Post 2");
        createPost(user2, "Post 3");

        List<Post> result =
                postRepository.findByUserUserId(user1.getUserId());

        assertEquals(2, result.size());

        assertTrue(result.stream()
                .allMatch(post -> post.getUser().getUserId()
                        .equals(user1.getUserId())));
    }
}