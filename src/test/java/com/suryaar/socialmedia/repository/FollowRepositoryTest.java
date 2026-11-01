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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FollowRepositoryTest {

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

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

    private void createFollow(User follower, User following) {
        followRepository.saveAndFlush(
                new Follow(follower, following)
        );
    }

    @Test
    void shouldCountFollowers() {

        User target = createUser("target", "target@test.com");
        User user1 = createUser("user1", "user1@test.com");
        User user2 = createUser("user2", "user2@test.com");

        createFollow(user1, target);
        createFollow(user2, target);

        assertEquals(
                2,
                followRepository.countByFollowing_UserId(
                        target.getUserId())
        );
    }

    @Test
    void shouldCountFollowing() {

        User source = createUser("source", "source@test.com");
        User user1 = createUser("user1", "user1@test.com");
        User user2 = createUser("user2", "user2@test.com");

        createFollow(source, user1);
        createFollow(source, user2);

        assertEquals(
                2,
                followRepository.countByFollower_UserId(
                        source.getUserId())
        );
    }

    @Test
    void shouldReturnTrueWhenFollowExists() {

        User follower = createUser("follower", "follower@test.com");
        User following = createUser("following", "following@test.com");

        createFollow(follower, following);

        assertTrue(
                followRepository.existsByFollower_UserIdAndFollowing_UserId(
                        follower.getUserId(),
                        following.getUserId()
                )
        );
    }

    @Test
    void shouldReturnFalseWhenFollowDoesNotExist() {

        User follower = createUser("follower", "follower@test.com");
        User following = createUser("following", "following@test.com");

        assertFalse(
                followRepository.existsByFollower_UserIdAndFollowing_UserId(
                        follower.getUserId(),
                        following.getUserId()
                )
        );
    }

    @Test
    void shouldDeleteSpecificFollow() {

        User follower = createUser("follower", "follower@test.com");
        User following1 = createUser("following1", "following1@test.com");
        User following2 = createUser("following2", "following2@test.com");

        createFollow(follower, following1);
        createFollow(follower, following2);

        int deletedCount = followRepository.deleteFollow(
                follower.getUserId(),
                following1.getUserId()
        );

        entityManager.clear();

        assertEquals(1, deletedCount);

        assertFalse(
                followRepository.existsByFollower_UserIdAndFollowing_UserId(
                        follower.getUserId(),
                        following1.getUserId()
                )
        );

        assertTrue(
                followRepository.existsByFollower_UserIdAndFollowing_UserId(
                        follower.getUserId(),
                        following2.getUserId()
                )
        );
    }

    @Test
    void shouldDeleteAllRelationshipsForUser() {

        User userA = createUser("userA", "a@test.com");
        User userB = createUser("userB", "b@test.com");
        User userC = createUser("userC", "c@test.com");
        User userD = createUser("userD", "d@test.com");

        createFollow(userA, userB);
        createFollow(userB, userC);

        // Unrelated relationship
        createFollow(userC, userD);

        int deletedCount =
                followRepository.deleteAllRelationshipsForUser(
                        userB.getUserId()
                );

        entityManager.clear();

        assertEquals(2, deletedCount);

        assertFalse(
                followRepository.existsByFollower_UserIdAndFollowing_UserId(
                        userA.getUserId(), userB.getUserId())
        );

        assertFalse(
                followRepository.existsByFollower_UserIdAndFollowing_UserId(
                        userB.getUserId(), userC.getUserId())
        );

        assertTrue(
                followRepository.existsByFollower_UserIdAndFollowing_UserId(
                        userC.getUserId(), userD.getUserId())
        );
    }

    @Test
    void shouldReturnFollowersInUsernameOrderWithPagination() {

        User target = createUser("target", "target@test.com");

        User zara = createUser("zara", "zara@test.com");
        User amy = createUser("amy", "amy@test.com");
        User bob = createUser("bob", "bob@test.com");

        createFollow(zara, target);
        createFollow(amy, target);
        createFollow(bob, target);

        Page<Follow> result =
                followRepository
                        .findByFollowing_UserIdOrderByFollowerUsernameAsc(
                                target.getUserId(),
                                PageRequest.of(0, 2)
                        );

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        assertEquals("amy",
                result.getContent().get(0).getFollower().getUsername());

        assertEquals("bob",
                result.getContent().get(1).getFollower().getUsername());
    }

    @Test
    void shouldReturnFollowingInUsernameOrderWithPagination() {

        User source = createUser("source", "source@test.com");

        User zebra = createUser("zebra", "zebra@test.com");
        User alpha = createUser("alpha", "alpha@test.com");
        User mike = createUser("mike", "mike@test.com");

        createFollow(source, zebra);
        createFollow(source, alpha);
        createFollow(source, mike);

        Page<Follow> result =
                followRepository
                        .findByFollower_UserIdOrderByFollowingUsernameAsc(
                                source.getUserId(),
                                PageRequest.of(0, 2)
                        );

        assertEquals(2, result.getContent().size());
        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getTotalPages());

        assertEquals("alpha",
                result.getContent().get(0).getFollowing().getUsername());

        assertEquals("mike",
                result.getContent().get(1).getFollowing().getUsername());
    }
}