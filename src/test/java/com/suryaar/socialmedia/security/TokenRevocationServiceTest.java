package com.suryaar.socialmedia.security;

import com.suryaar.socialmedia.entity.RevokedToken;
import com.suryaar.socialmedia.repository.RevokedTokenRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TokenRevocationServiceTest {

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    private TokenRevocationService tokenRevocationService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        tokenRevocationService =
                new TokenRevocationService(revokedTokenRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldRevokeTokenSuccessfully() {

        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC)
                .plusHours(1);

        when(revokedTokenRepository.existsByTokenId("token-123"))
                .thenReturn(false);

        tokenRevocationService.revokeToken(
                "token-123", 1L, expiresAt);

        verify(revokedTokenRepository).save(any(RevokedToken.class));
    }

    @Test
    void shouldNotSaveDuplicateRevokedToken() {

        when(revokedTokenRepository.existsByTokenId("token-123"))
                .thenReturn(true);

        tokenRevocationService.revokeToken(
                "token-123",
                1L,
                LocalDateTime.now(ZoneOffset.UTC).plusHours(1));

        verify(revokedTokenRepository, never())
                .save(any(RevokedToken.class));
    }

    @Test
    void shouldCheckWhetherTokenIsRevoked() {

        when(revokedTokenRepository.existsByTokenId("revoked-token"))
                .thenReturn(true);

        assertTrue(tokenRevocationService.isTokenRevoked("revoked-token"));

        when(revokedTokenRepository.existsByTokenId("active-token"))
                .thenReturn(false);

        assertFalse(tokenRevocationService.isTokenRevoked("active-token"));
    }
}