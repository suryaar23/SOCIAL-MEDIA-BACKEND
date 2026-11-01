package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
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
class UserRepositoryTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User createUser(String s, String s1) {
        User user = new User();
        user.setUsername(s);
        user.setEmail(s1);
        user.setPhone("9876543210");
        user.setPassword("encodedPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setTokenVersion(0);
        return user;
    }

    @Test
    void shouldSaveAndFindUserByUsername() {

        User user = createUser("user1", "user1@test.com");

        userRepository.saveAndFlush(user);

        Optional<User> result =
                userRepository.findByUsernameAndStatus(
                        "user1", UserStatus.ACTIVE);

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
    }

    @Test
    void shouldSaveAndFindUserByEmail() {

        User user = createUser("user1", "user1@test.com");

        userRepository.saveAndFlush(user);

        Optional<User> result =
                userRepository.findByEmailAndStatus(
                        "user1@test.com", UserStatus.ACTIVE);

        assertTrue(result.isPresent());
        assertEquals("user1@test.com", result.get().getEmail());
    }

    @Test
    void shouldReturnTrueWhenActiveUserExists() {
        User user = new User();
        user.setUsername("surya");
        user.setEmail("surya@test.com");
        user.setPhone("9876543210");
        user.setPassword("encodedPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setTokenVersion(0);

        userRepository.saveAndFlush(user);

        boolean result = userRepository.existsByUsernameAndStatus(
                "surya", UserStatus.ACTIVE
        );

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenActiveUserDoesNotExist() {
        boolean result = userRepository.existsByUsernameAndStatus(
                "surya", UserStatus.ACTIVE
        );

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenActiveEmailExists() {

        User user = createUser("user1", "user1@test.com");

        userRepository.saveAndFlush(user);

        boolean result = userRepository.existsByEmailAndStatus(
                "user1@test.com", UserStatus.ACTIVE
        );

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenActiveEmailDoesNotExist() {
        boolean result = userRepository.existsByEmailAndStatus(
                "surya@test.com", UserStatus.ACTIVE
        );

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenActivePhoneExists() {

        User user = createUser("user1", "user1@test.com");

        userRepository.saveAndFlush(user);

        boolean result = userRepository.existsByPhoneAndStatus(
                "9876543210", UserStatus.ACTIVE
        );
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenActivePhoneDoesNotExist() {
        boolean result = userRepository.existsByPhoneAndStatus(
                "9876543210", UserStatus.ACTIVE
        );
        assertFalse(result);
    }

    @Test
    void shouldSoftDeleteActiveUser() {

        User user = createUser("user1", "user1@test.com");

        User savedUser = userRepository.saveAndFlush(user);

        int result = userRepository.softDeleteUser(savedUser.getUserId());

        assertEquals(1, result);

        // Clear persistence context to avoid stale entity data
        entityManager.clear();

        User deletedUser = userRepository.findById(savedUser.getUserId())
                .orElseThrow();

        assertEquals(UserStatus.DELETED, deletedUser.getStatus());
        assertNull(deletedUser.getUsername());
        assertNull(deletedUser.getEmail());
        assertNull(deletedUser.getPhone());
        assertNull(deletedUser.getPassword());
        assertNull(deletedUser.getProfilePicture());
        assertEquals(1, deletedUser.getTokenVersion());
    }

    @Test
    void shouldNotDeleteAlreadyDeletedUser() {

        User user = new User();
        user.setUsername("user1");
        user.setEmail("user1@gmail.com");
        user.setPhone("9876543210");
        user.setPassword("encodedPassword");
        user.setStatus(UserStatus.DELETED);
        user.setTokenVersion(0);

        User savedUser = userRepository.saveAndFlush(user);

        int result = userRepository.softDeleteUser(savedUser.getUserId());

        assertEquals(0, result);
    }

    @Test
    void shouldReturnZeroWhenUserDoesNotExist() {

        int result = userRepository.softDeleteUser(999999L);

        assertEquals(0, result);
    }

    @Test
    void shouldReturnUsersInDescendingCreatedAtOrder() throws InterruptedException {

        User user1 = createUser("user1", "user1@test.com");
        userRepository.saveAndFlush(user1);

        Thread.sleep(50);

        User user2 = createUser("user2", "user2@test.com");
        userRepository.saveAndFlush(user2);

        Thread.sleep(50);

        User user3 = createUser("user3", "user3@test.com");
        userRepository.saveAndFlush(user3);

        Page<User> result = userRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10));

        assertEquals(3, result.getTotalElements());

        assertEquals("user3", result.getContent().get(0).getUsername());
        assertEquals("user2", result.getContent().get(1).getUsername());
        assertEquals("user1", result.getContent().get(2).getUsername());
    }

    @Test
    void shouldReturnPaginatedUsers() {

        for (int i = 1; i <= 5; i++) {
            userRepository.save(createUser(
                    "user" + i,
                    "user" + i + "@test.com"
            ));
        }

        userRepository.flush();

        Page<User> result = userRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, 2));

        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }

    @Test
    void shouldSearchUsernameIgnoringCase() {

        userRepository.save(createUser("surya", "surya@test.com"));
        userRepository.save(createUser("Suraj", "suraj@test.com"));
        userRepository.save(createUser("Arun", "arun@test.com"));

        userRepository.flush();

        Page<User> result = userRepository
                .findByStatusAndUsernameContainingIgnoreCase(
                        UserStatus.ACTIVE,
                        "SUR",
                        PageRequest.of(0, 10)
                );

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void shouldReturnOnlyActiveUsers() {

        userRepository.save(createUser("surya", "surya@test.com"));

        User deletedUser = createUser("suraj", "suraj@test.com");
        deletedUser.setStatus(UserStatus.DELETED);

        userRepository.save(deletedUser);
        userRepository.flush();

        Page<User> result = userRepository
                .findByStatusAndUsernameContainingIgnoreCase(
                        UserStatus.ACTIVE,
                        "sur",
                        PageRequest.of(0, 10)
                );

        assertEquals(1, result.getTotalElements());
        assertEquals("surya", result.getContent().get(0).getUsername());
    }

    @Test
    void shouldPaginateUsernameSearchResults() {

        for (int i = 1; i <= 5; i++) {
            userRepository.save(
                    createUser("surya" + i, "surya" + i + "@test.com")
            );
        }

        userRepository.flush();

        Page<User> result = userRepository
                .findByStatusAndUsernameContainingIgnoreCase(
                        UserStatus.ACTIVE,
                        "surya",
                        PageRequest.of(0, 2)
                );

        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        assertEquals(3, result.getTotalPages());
    }
}