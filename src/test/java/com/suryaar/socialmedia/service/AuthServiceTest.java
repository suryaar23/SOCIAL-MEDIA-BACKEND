package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.*;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.exception.DuplicateResourceException;
import com.suryaar.socialmedia.exception.InvalidCredentialsException;
import com.suryaar.socialmedia.security.JwtService;
import com.suryaar.socialmedia.security.TokenRevocationService;
import org.junit.jupiter.api.Test;
import com.suryaar.socialmedia.repository.UserRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

class AuthServiceTest {

    private AutoCloseable mocks;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        authService = new AuthService(
                userRepository,
                passwordEncoder,
                jwtService,
                tokenRevocationService
        );

        ReflectionTestUtils.setField(authService, "jwtExpirationMs", 60000L);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldRejectDuplicateUsername() {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("surya_dev");
        request.setEmail("surya@example.com");
        request.setPhone("9876543210");
        request.setPassword("Pass1234");

        when(userRepository.existsByUsernameAndStatus(
                request.getUsername(),
                UserStatus.ACTIVE
        )).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> authService.registerUser(request)
        );

        verify(userRepository, never()).save(
                any(User.class)
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("surya_dev");
        request.setEmail("surya@example.com");
        request.setPhone("9876543210");
        request.setPassword("Pass1234");

        when(userRepository.existsByUsernameAndStatus(
                request.getUsername(), UserStatus.ACTIVE
        )).thenReturn(false);

        when(userRepository.existsByEmailAndStatus(
                request.getEmail(), UserStatus.ACTIVE
        )).thenReturn(false);

        when(userRepository.existsByPhoneAndStatus(
                request.getPhone(), UserStatus.ACTIVE
        )).thenReturn(false);

        when(passwordEncoder.encode("Pass1234"))
                .thenReturn("encodedPassword123");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setUserId(1L);
                    return user;
                });

        UserResponse response = authService.registerUser(request);

        assertNotNull(response);

        assertEquals(1L, response.getUserId());
        assertEquals("surya_dev", response.getUsername());
        assertEquals("surya@example.com", response.getEmail());

        verify(passwordEncoder).encode("Pass1234");
        verify(userRepository, times(1)).save(any(User.class));

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("encodedPassword123", savedUser.getPassword());

        org.junit.jupiter.api.Assertions.assertNotEquals(
                "Pass1234",
                savedUser.getPassword()
        );
    }

    @Test
    void shouldRejectDuplicateEmail() {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("surya_dev");
        request.setEmail("surya@example.com");
        request.setPhone("9876543210");
        request.setPassword("Pass1234");

        when(userRepository.existsByEmailAndStatus(request.getEmail(),
                UserStatus.ACTIVE))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            authService.registerUser(request);
        });

        assertEquals(
                "Email is already registered",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicatePhone() {

        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("surya_dev");
        request.setEmail("surya.example.com");
        request.setPhone("9876543210");
        request.setPassword("Pass1234");

        when (userRepository.existsByPhoneAndStatus(request.getPhone(), UserStatus.ACTIVE))
                .thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class, () -> {
            authService.registerUser(request);
        });

        assertEquals(
                "Phone number is already registered",
                exception.getMessage()
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfullyWithUsername() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setIdentifier("surya_dev");
        request.setPassword("Pass1234");

        User user = new User();
        user.setUserId(1L);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPassword("encodedPassword123");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsernameAndStatus(
                "surya_dev", UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Pass1234", "encodedPassword123"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        // Act
        LoginResponse response = authService.loginUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());

        assertEquals(60L, response.getExpiresIn());
        assertEquals("Bearer", response.getTokenType());


        verify(jwtService, times(1)).generateToken(user);

        verify(userRepository, never()).findByEmailAndStatus(anyString(), any(UserStatus.class));
    }

    @Test
    void shouldRejectLoginWithIncorrectPassword() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setIdentifier("surya_dev");
        request.setPassword("WrongPass123");

        User user = new User();
        user.setUserId(1L);
        user.setUsername("surya_dev");
        user.setPassword("encodedPassword123");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsernameAndStatus(
                "surya_dev", UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPass123", "encodedPassword123"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.loginUser(request)
        );

        // Verify
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void shouldLoginSuccessfullyWithEmail() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setIdentifier("surya@example.com");
        request.setPassword("Pass1234");

        User user = new User();
        user.setUserId(1L);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPassword("encodedPassword123");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmailAndStatus(
                "surya@example.com", UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Pass1234", "encodedPassword123"))
                .thenReturn(true);

        when(jwtService.generateToken(user))
                .thenReturn("mock-jwt-token");

        // Act
        LoginResponse response = authService.loginUser(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getAccessToken());

        assertEquals(60L, response.getExpiresIn());
        assertEquals("Bearer", response.getTokenType());

        verify(userRepository, times(1))
                .findByEmailAndStatus(
                        "surya@example.com", UserStatus.ACTIVE);

        verify(userRepository, never())
                .findByUsernameAndStatus(anyString(), any(UserStatus.class));
    }

    @Test
    void shouldRejectLoginWithNonexistentUsername() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setIdentifier("unknown_user");
        request.setPassword("Pass1234");

        when(userRepository.findByUsernameAndStatus(
                "unknown_user", UserStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.loginUser(request)
        );

        // Verify
        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(any(User.class));
    }

    @Test
    void shouldRejectLoginWithNonexistentEmail() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setIdentifier("unknown@example.com");
        request.setPassword("Pass1234");

        when(userRepository.findByEmailAndStatus(
                "unknown@example.com", UserStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.loginUser(request)
        );

        // Verify
        verify(passwordEncoder, never())
                .matches(anyString(), anyString());

        verify(jwtService, never())
                .generateToken(any(User.class));
    }

    @Test
    void shouldRejectPasswordChangeWithIncorrectCurrentPassword() {
        // Arrange
        Long userId = 1L;

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("WrongPass123");
        request.setNewPassword("NewPass123");
        request.setConfirmPassword("NewPass123");

        User user = new User();
        user.setUserId(userId);
        user.setPassword("encodedPassword123");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("WrongPass123", "encodedPassword123"))
                .thenReturn(false);

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.changePassword(userId, request)
        );

        // Verify
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectPasswordChangeWithMismatchedNewPasswords() {
        //Arrange
        Long userId = 1L;

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("Pass1234");
        request.setNewPassword("NewPass123");
        request.setConfirmPassword("NewPass456"); // Mismatched new password

        User user = new User();
        user.setUserId(userId);
        user.setPassword("encodedPassword123");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("Pass1234", "encodedPassword123"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(userId, request)
        );

        // Verify
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectPasswordChangeWithSameNewPassword() {
        //Arrange
        Long userId = 1L;

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("Pass1234");
        request.setNewPassword("Pass1234"); // Same as current password
        request.setConfirmPassword("Pass1234");

        User user = new User();
        user.setUserId(userId);
        user.setPassword("encodedPassword123");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("Pass1234", "encodedPassword123"))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> authService.changePassword(userId, request)
        );

        // Verify
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        // Arrange
        Long userId = 1L;

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("CurrentPass123");
        request.setNewPassword("NewPass456");
        request.setConfirmPassword("NewPass456");

        User user = new User();
        user.setUserId(userId);
        user.setPassword("encodedCurrentPassword");
        user.setStatus(UserStatus.ACTIVE);
        user.setTokenVersion(0);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "CurrentPass123", "encodedCurrentPassword"))
                .thenReturn(true);

        when(passwordEncoder.matches(
                "NewPass456", "encodedCurrentPassword"))
                .thenReturn(false);

        when(passwordEncoder.encode("NewPass456"))
                .thenReturn("encodedNewPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.changePassword(userId, request);

        // Assert
        assertEquals("encodedNewPassword", user.getPassword());
        assertEquals(1, user.getTokenVersion());

        verify(passwordEncoder).encode("NewPass456");
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectPasswordChangeWhenActiveUserNotFound() {
        // Arrange
        Long userId = 99L;

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("CurrentPass123");
        request.setNewPassword("NewPass456");
        request.setConfirmPassword("NewPass456");

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.changePassword(userId, request)
        );

        // Verify
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectLogoutWhenAuthorizationHeaderIsMissing() {
        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.logoutUser(null)
        );

        // Verify
        verifyNoInteractions(jwtService);
        verifyNoInteractions(tokenRevocationService);
    }

    @Test
    void shouldRejectLogoutWhenAuthorizationSchemeIsInvalid() {
        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.logoutUser("Basic some-token")
        );

        // Verify
        verifyNoInteractions(jwtService);
        verifyNoInteractions(tokenRevocationService);
    }

    @Test
    void shouldRejectLogoutWhenBearerTokenIsMissing() {
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.logoutUser("Bearer ")
        );

        verifyNoInteractions(tokenRevocationService);
    }

    @Test
    void shouldRejectLogoutWhenJwtIsInvalid() {
        // Arrange
        Long userId = 1L;
        String authorizationHeader = "Bearer invalid.jwt.token";

        when(jwtService.isTokenValid("invalid.jwt.token", userId))
                .thenThrow(new InvalidCredentialsException("Invalid token"));

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.logoutUser(authorizationHeader)
        );

        // Verify
        verifyNoInteractions(tokenRevocationService);
    }

    @Test
    void shouldLogoutSuccessfullyAndRevokeToken() {
        // Arrange
        String token = "valid.jwt.token";
        String authorizationHeader = "Bearer " + token;

        Long userId = 1L;
        String tokenId = "token-123";
        java.time.LocalDateTime expiresAt =
                java.time.LocalDateTime.parse("2027-01-01T00:00:00");

        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(jwtService.isTokenValid(token, userId)).thenReturn(true);
        when(jwtService.extractTokenId(token)).thenReturn(tokenId);
        when(jwtService.extractExpiration(token)).thenReturn(LocalDateTime.from(expiresAt));

        // Act
        authService.logoutUser(authorizationHeader);

        // Assert
        verify(tokenRevocationService, times(1))
                .revokeToken(tokenId, userId, LocalDateTime.from(expiresAt));
    }

    @Test
    void shouldRejectLogoutWhenTokenIsInvalidForUser() {
        // Arrange
        String token = "invalid-for-user.jwt.token";
        String authorizationHeader = "Bearer " + token;
        Long userId = 1L;

        when(jwtService.extractUserId(token)).thenReturn(userId);
        when(jwtService.isTokenValid(token, userId)).thenReturn(false);

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.logoutUser(authorizationHeader)
        );

        // Verify
        verifyNoInteractions(tokenRevocationService);
    }

    @Test
    void shouldConvertRuntimeExceptionDuringLogoutToInvalidCredentials() {
        // Arrange
        String token = "valid-format.jwt.token";

        when(jwtService.extractUserId(token))
                .thenThrow(new RuntimeException("Unexpected JWT error"));

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> authService.logoutUser("Bearer " + token)
        );

        // Verify
        verifyNoInteractions(tokenRevocationService);
    }
}