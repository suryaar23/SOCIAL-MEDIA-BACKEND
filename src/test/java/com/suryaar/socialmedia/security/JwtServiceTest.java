package com.suryaar.socialmedia.security;

import com.suryaar.socialmedia.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Mock
    private TokenRevocationService tokenRevocationService;

    private JwtService jwtService;
    private AutoCloseable mocks;

    private static final String SECRET =
            java.util.Base64.getEncoder().encodeToString(
                    "01234567890123456789012345678901"
                            .getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        jwtService = new JwtService(SECRET, 60000L, tokenRevocationService);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    private User createUser() {
        User user = new User();
        user.setUserId(1L);
        user.setTokenVersion(0);
        return user;
    }

    @Test
    void shouldGenerateTokenSuccessfully() {
        String token = jwtService.generateToken(createUser());

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void shouldExtractUserIdFromToken() {
        String token = jwtService.generateToken(createUser());

        assertEquals(1L, jwtService.extractUserId(token));
    }

    @Test
    void shouldValidateTokenForCorrectUser() {
        String token = jwtService.generateToken(createUser());

        assertTrue(jwtService.isTokenValid(token, 1L));
        assertFalse(jwtService.isTokenValid(token, 2L));
    }

    @Test
    void shouldExtractTokenClaims() {
        String token = jwtService.generateToken(createUser());

        assertNotNull(jwtService.extractTokenId(token));
        assertEquals(0, jwtService.extractTokenVersion(token));
        assertNotNull(jwtService.extractExpiration(token));
    }

    @Test
    void shouldRejectNullUser() {
        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generateToken(null)
        );
    }

    @Test
    void shouldRejectUserWithoutId() {
        User user = new User();
        user.setTokenVersion(0);

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generateToken(user)
        );
    }

    @Test
    void shouldRejectUserWithoutTokenVersion() {
        User user = new User();
        user.setUserId(1L);
        user.setTokenVersion(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> jwtService.generateToken(user)
        );
    }
}