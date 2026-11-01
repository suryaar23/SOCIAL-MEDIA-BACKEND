package com.suryaar.socialmedia.repository;
import com.suryaar.socialmedia.entity.Like;
import com.suryaar.socialmedia.entity.Post;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

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

    @Test
    void shouldReturnTrueWhenLikeExists() {

        User user = createUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        likeRepository.saveAndFlush(new Like(user, post));

        assertTrue(likeRepository.existsByUserUserIdAndPostPostId(
                user.getUserId(), post.getPostId()));
    }

    @Test
    void shouldReturnFalseWhenLikeDoesNotExist() {

        User user = createUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        assertFalse(likeRepository.existsByUserUserIdAndPostPostId(
                user.getUserId(), post.getPostId()));
    }

    @Test
    void shouldReturnCorrectLikeCountForPost() {

        User user1 = createUser("user1", "user1@test.com");
        User user2 = createUser("user2", "user2@test.com");
        User user3 = createUser("user3", "user3@test.com");

        Post post = createPost(user1, "First post");

        likeRepository.save(new Like(user1, post));
        likeRepository.save(new Like(user2, post));
        likeRepository.save(new Like(user3, post));
        likeRepository.flush();

        assertEquals(3, likeRepository.countByPostPostId(post.getPostId()));
    }

    @Test
    void shouldDeleteSpecificLike() {

        User user1 = createUser("user1", "user1@test.com");
        User user2 = createUser("user2", "user2@test.com");

        Post post = createPost(user1, "First post");

        likeRepository.save(new Like(user1, post));
        likeRepository.save(new Like(user2, post));
        likeRepository.flush();

        int deletedCount = likeRepository.deleteLike(
                user1.getUserId(), post.getPostId());

        entityManager.clear();

        assertEquals(1, deletedCount);

        assertFalse(likeRepository.existsByUserUserIdAndPostPostId(
                user1.getUserId(), post.getPostId()));

        assertTrue(likeRepository.existsByUserUserIdAndPostPostId(
                user2.getUserId(), post.getPostId()));
    }

    @Test
    void shouldDeleteAllLikesForPost() {

        User user1 = createUser("user1", "user1@test.com");
        User user2 = createUser("user2", "user2@test.com");
        User user3 = createUser("user3", "user3@test.com");

        Post post1 = createPost(user1, "First post");
        Post post2 = createPost(user2, "Second post");

        likeRepository.save(new Like(user1, post1));
        likeRepository.save(new Like(user2, post1));
        likeRepository.save(new Like(user3, post2));
        likeRepository.flush();

        int deletedCount = likeRepository.deleteAllByPostId(
                post1.getPostId());

        entityManager.clear();

        assertEquals(2, deletedCount);

        assertEquals(0, likeRepository.countByPostPostId(post1.getPostId()));
        assertEquals(1, likeRepository.countByPostPostId(post2.getPostId()));
    }
}