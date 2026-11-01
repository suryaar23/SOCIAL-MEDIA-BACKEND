package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostMediaRepositoryTest {

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MediaRepository mediaRepository;

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

    private Media createMedia(User user, String name) {
        Media media = new Media(
                MediaType.IMAGE,
                "https://example.com/" + name,
                name,
                1024L,
                user
        );

        return mediaRepository.saveAndFlush(media);
    }

    @Test
    void shouldReturnPostMediaOrderedByPosition() {

        User user = createUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        Media media1 = createMedia(user, "image1.jpg");
        Media media2 = createMedia(user, "image2.jpg");
        Media media3 = createMedia(user, "image3.jpg");

        // Save deliberately out of position order
        postMediaRepository.save(new PostMedia(post, media3, 3));
        postMediaRepository.save(new PostMedia(post, media1, 1));
        postMediaRepository.save(new PostMedia(post, media2, 2));

        postMediaRepository.flush();

        List<PostMedia> result =
                postMediaRepository.findByPostPostIdOrderByPositionAsc(
                        post.getPostId());

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getPosition());
        assertEquals(2, result.get(1).getPosition());
        assertEquals(3, result.get(2).getPosition());
    }

    @Test
    void shouldReturnTrueWhenMediaIsLinkedToPost() {

        User user = createUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");
        Media media = createMedia(user, "image1.jpg");

        postMediaRepository.saveAndFlush(
                new PostMedia(post, media, 1));

        assertTrue(postMediaRepository.existsByMediaMediaId(
                media.getMediaId()));
    }

    @Test
    void shouldReturnFalseWhenMediaIsNotLinkedToAnyPost() {

        User user = createUser("user1", "user1@test.com");
        Media media = createMedia(user, "image1.jpg");

        assertFalse(postMediaRepository.existsByMediaMediaId(
                media.getMediaId()));
    }

    @Test
    void shouldReturnAllMediaForPost() {

        User user = createUser("user1", "user1@test.com");
        Post post = createPost(user, "First post");

        Media media1 = createMedia(user, "image1.jpg");
        Media media2 = createMedia(user, "image2.jpg");

        postMediaRepository.save(new PostMedia(post, media1, 1));
        postMediaRepository.save(new PostMedia(post, media2, 2));
        postMediaRepository.flush();

        List<PostMedia> result =
                postMediaRepository.findByPostPostId(post.getPostId());

        assertEquals(2, result.size());
    }
}