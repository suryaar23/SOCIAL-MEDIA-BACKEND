package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.Media;
import com.suryaar.socialmedia.entity.MediaType;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MediaRepositoryTest {

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private UserRepository userRepository;

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
    void shouldFindMediaByIds() {

        User user = createUser("user1", "user1@test.com");

        Media media1 = createMedia(user, "image1.jpg");
        Media media2 = createMedia(user, "image2.jpg");
        createMedia(user, "image3.jpg");

        List<Media> result = mediaRepository.findByMediaIdIn(
                List.of(media1.getMediaId(), media2.getMediaId())
        );

        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(
                media -> media.getMediaId().equals(media1.getMediaId())));

        assertTrue(result.stream().anyMatch(
                media -> media.getMediaId().equals(media2.getMediaId())));
    }

    @Test
    void shouldReturnEmptyListWhenNoMediaIdsMatch() {

        List<Media> result = mediaRepository.findByMediaIdIn(
                List.of(999999L, 999998L)
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenMediaBelongsToUser() {

        User user = createUser("user1", "user1@test.com");
        Media media = createMedia(user, "image1.jpg");

        assertTrue(mediaRepository.existsByMediaIdAndUploadedBy_UserId(
                media.getMediaId(),
                user.getUserId()
        ));
    }

    @Test
    void shouldReturnFalseWhenMediaBelongsToAnotherUser() {

        User owner = createUser("owner", "owner@test.com");
        User anotherUser = createUser("another", "another@test.com");

        Media media = createMedia(owner, "image1.jpg");

        assertFalse(mediaRepository.existsByMediaIdAndUploadedBy_UserId(
                media.getMediaId(),
                anotherUser.getUserId()
        ));
    }
}