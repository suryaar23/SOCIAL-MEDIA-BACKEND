package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.RevokedToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RevokedTokenRepositoryTest {

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    void shouldReturnTrueWhenTokenExists() {

        RevokedToken token = new RevokedToken(
                "token-001",
                1L,
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now()
        );

        revokedTokenRepository.saveAndFlush(token);

        assertTrue(revokedTokenRepository.existsByTokenId("token-001"));
    }

    @Test
    void shouldReturnFalseWhenTokenDoesNotExist() {

        assertFalse(revokedTokenRepository.existsByTokenId("unknown-token"));
    }

    @Test
    void shouldDeleteExpiredTokens() {

        LocalDateTime now = LocalDateTime.now();

        RevokedToken expired = new RevokedToken(
                "expired-token",
                1L,
                now.minusHours(2),
                now.minusHours(3)
        );

        RevokedToken valid = new RevokedToken(
                "valid-token",
                2L,
                now.plusHours(2),
                now.minusHours(1)
        );

        revokedTokenRepository.save(expired);
        revokedTokenRepository.save(valid);
        revokedTokenRepository.flush();

        long deletedCount =
                revokedTokenRepository.deleteByExpiresAtBefore(now);

        assertEquals(1, deletedCount);
        assertFalse(revokedTokenRepository.existsById("expired-token"));
        assertTrue(revokedTokenRepository.existsById("valid-token"));
    }

    @Test
    void shouldReturnZeroWhenNoExpiredTokensExist() {

        RevokedToken valid = new RevokedToken(
                "valid-token",
                1L,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now()
        );

        revokedTokenRepository.saveAndFlush(valid);

        long deletedCount =
                revokedTokenRepository.deleteByExpiresAtBefore(
                        LocalDateTime.now()
                );

        assertEquals(0, deletedCount);
    }
}