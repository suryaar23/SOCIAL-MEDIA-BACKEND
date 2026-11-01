package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.MeResponse;
import com.suryaar.socialmedia.dto.PublicProfileResponse;
import com.suryaar.socialmedia.dto.UpdateMyProfileRequest;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.exception.DuplicateResourceException;
import com.suryaar.socialmedia.exception.InvalidCredentialsException;
import com.suryaar.socialmedia.repository.UserRepository;
import com.suryaar.socialmedia.repository.FollowRepository;
import com.suryaar.socialmedia.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

class ProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostService postService;

    private ProfileService profileService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        profileService = new ProfileService(
                userRepository,
                followRepository,
                postRepository,
                postService
        );
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldGetMyProfileSuccessfully() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPhone("9876543210");
        user.setProfilePicture("profile.jpg");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act
        MeResponse response = profileService.getMyProfile(userId);

        // Assert
        assertNotNull(response);
        assertEquals("surya_dev", response.getUsername());
        assertEquals("surya@example.com", response.getEmail());
        assertEquals("9876543210", response.getPhone());
        assertEquals("profile.jpg", response.getProfilePicture());
    }

    @Test
    void shouldRejectProfileRetrievalWhenUserDoesNotExist() {
        // Arrange
        Long userId = 99L;

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> profileService.getMyProfile(userId)
        );
    }

    @Test
    void shouldRejectProfileRetrievalForInactiveUser() {
        // Arrange
        Long userId = 2L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("inactive_user");
        user.setStatus(UserStatus.DELETED);

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(
                InvalidCredentialsException.class,
                () -> profileService.getMyProfile(userId)
        );
    }

    @Test
    void shouldUpdateUsernameSuccessfully() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPhone("9876543210");
        user.setStatus(UserStatus.ACTIVE);

        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setUsername("surya_new");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByUsernameAndStatus(
                "surya_new", UserStatus.ACTIVE))
                .thenReturn(false);

        when(userRepository.save(user))
                .thenReturn(user);

        // Act
        MeResponse response = profileService.updateMyProfile(userId, request);

        // Assert
        assertEquals("surya_new", response.getUsername());
        assertEquals("surya@example.com", response.getEmail());
        assertEquals("9876543210", response.getPhone());

        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectUpdateWhenUsernameAlreadyExists() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPhone("9876543210");
        user.setStatus(UserStatus.ACTIVE);

        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setUsername("another_user");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.existsByUsernameAndStatus(
                "another_user", UserStatus.ACTIVE))
                .thenReturn(true);

        // Act & Assert
        assertThrows(
                DuplicateResourceException.class,
                () -> profileService.updateMyProfile(userId, request)
        );

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldAllowKeepingExistingUsername() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPhone("9876543210");
        user.setStatus(UserStatus.ACTIVE);

        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setUsername("surya_dev");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        // Act
        MeResponse response = profileService.updateMyProfile(userId, request);

        // Assert
        assertEquals("surya_dev", response.getUsername());

        verify(userRepository, never())
                .existsByUsernameAndStatus("surya_dev", UserStatus.ACTIVE);

        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectBlankUsername() {
        // Arrange
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPhone("9876543210");
        user.setStatus(UserStatus.ACTIVE);

        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setUsername("   ");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> profileService.updateMyProfile(userId, request)
        );

        // Verify
        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldRejectDuplicateEmail() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPhone("9876543210");
        user.setStatus(UserStatus.ACTIVE);

        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setEmail("taken@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmailAndStatus(
                "taken@example.com", UserStatus.ACTIVE)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> profileService.updateMyProfile(userId, request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectDuplicatePhone() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPhone("9876543210");
        user.setStatus(UserStatus.ACTIVE);

        UpdateMyProfileRequest request = new UpdateMyProfileRequest();
        request.setPhone("9123456780");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.existsByPhoneAndStatus(
                "9123456780", UserStatus.ACTIVE)).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> profileService.updateMyProfile(userId, request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldRejectBlankEmailAndPhone() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setUsername("surya_dev");
        user.setEmail("surya@example.com");
        user.setPhone("9876543210");
        user.setStatus(UserStatus.ACTIVE);

        UpdateMyProfileRequest blankEmail = new UpdateMyProfileRequest();
        blankEmail.setEmail("   ");

        UpdateMyProfileRequest blankPhone = new UpdateMyProfileRequest();
        blankPhone.setPhone("   ");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> profileService.updateMyProfile(userId, blankEmail));

        assertThrows(IllegalArgumentException.class,
                () -> profileService.updateMyProfile(userId, blankPhone));

        verify(userRepository, never()).save(any(User.class));
    }


    @Test
    void shouldGetPublicProfileSuccessfully() {
        User user = new User();
        user.setUserId(2L);
        user.setUsername("another_user");
        user.setProfilePicture("avatar.jpg");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsernameAndStatus(
                "another_user", UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(followRepository.countByFollowing_UserId(2L)).thenReturn(10L);
        when(followRepository.countByFollower_UserId(2L)).thenReturn(5L);
        when(followRepository.existsByFollower_UserIdAndFollowing_UserId(1L, 2L))
                .thenReturn(true);

        PublicProfileResponse response =
                profileService.getPublicProfile("another_user", 1L);

        assertEquals("another_user", response.getUsername());
        assertEquals("avatar.jpg", response.getProfilePicture());
        assertEquals(10L, response.getFollowersCount());
        assertEquals(5L, response.getFollowingCount());
        assertEquals(true, response.getIsFollowing());
    }

    @Test
    void shouldReturnNotFoundForMissingPublicProfile() {
        when(userRepository.findByUsernameAndStatus(
                "unknown_user", UserStatus.ACTIVE))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileService.getPublicProfile("unknown_user", 1L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldLeaveIsFollowingNullWhenViewerIsProfileOwner() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("surya_dev");
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByUsernameAndStatus(
                "surya_dev", UserStatus.ACTIVE))
                .thenReturn(Optional.of(user));
        when(followRepository.countByFollowing_UserId(1L)).thenReturn(3L);
        when(followRepository.countByFollower_UserId(1L)).thenReturn(4L);

        PublicProfileResponse response =
                profileService.getPublicProfile("surya_dev", 1L);

        //assertEquals(null, response.getIsFollowing());
        assertNull(response.getIsFollowing());
        verify(followRepository, never())
                .existsByFollower_UserIdAndFollowing_UserId(1L, 1L);
    }

    @Test
    void shouldRejectBlankUsernameSearch() {
        assertThrows(ResponseStatusException.class,
                () -> profileService.searchUsers("  ", 0, 10, 1L));
    }

    @Test
    void shouldRejectInvalidPaginationParameters() {
        assertThrows(ResponseStatusException.class,
                () -> profileService.searchUsers("sur", -1, 10, 1L));

        assertThrows(ResponseStatusException.class,
                () -> profileService.searchUsers("sur", 0, 0, 1L));

        assertThrows(ResponseStatusException.class,
                () -> profileService.searchUsers("sur", 0, 101, 1L));
    }

    @Test
    void shouldSearchUsersWithPagination() {
        User user = new User();
        user.setUserId(2L);
        user.setUsername("surya_dev");
        user.setProfilePicture("avatar.jpg");
        user.setStatus(UserStatus.ACTIVE);

        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findByStatusAndUsernameContainingIgnoreCase(
                eq(UserStatus.ACTIVE),
                eq("sur"),
                any(Pageable.class)))
                .thenReturn(userPage);

        when(followRepository.countByFollowing_UserId(2L)).thenReturn(3L);
        when(followRepository.countByFollower_UserId(2L)).thenReturn(4L);
        when(followRepository.existsByFollower_UserIdAndFollowing_UserId(1L, 2L))
                .thenReturn(true);

        Page<PublicProfileResponse> result =
                profileService.searchUsers(" sur ", 0, 10, 1L);

        assertEquals(1, result.getTotalElements());
        assertEquals("surya_dev", result.getContent().get(0).getUsername());
        assertEquals(true, result.getContent().get(0).getIsFollowing());

        verify(userRepository).findByStatusAndUsernameContainingIgnoreCase(
                eq(UserStatus.ACTIVE), eq("sur"), any(Pageable.class));
    }

    @Test
    void shouldReturnEmptyPageWhenNoUsersMatchSearch() {
        when(userRepository.findByStatusAndUsernameContainingIgnoreCase(
                eq(UserStatus.ACTIVE),
                eq("unknown"),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<PublicProfileResponse> result =
                profileService.searchUsers("unknown", 0, 10, null);

        assertEquals(0, result.getTotalElements());
        assertTrue(result.isEmpty());
    }


    @Test
    void shouldRejectAccountDeletionWhenUserDoesNotExist() {
        Long userId = 99L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> profileService.deleteMyAccount(userId)
        );

        verifyNoInteractions(postRepository, followRepository, postService);
    }

    @Test
    void shouldDeleteAccountSuccessfully() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findByUserUserId(userId)).thenReturn(List.of());
        when(userRepository.softDeleteUser(userId)).thenReturn(1);

        profileService.deleteMyAccount(userId);

        verify(followRepository).deleteAllRelationshipsForUser(userId);
        verify(userRepository).softDeleteUser(userId);
    }

    @Test
    void shouldThrowWhenSoftDeleteDoesNotUpdateExactlyOneRow() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);
        user.setStatus(UserStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(postRepository.findByUserUserId(userId)).thenReturn(List.of());
        when(userRepository.softDeleteUser(userId)).thenReturn(0);

        assertThrows(
                IllegalStateException.class,
                () -> profileService.deleteMyAccount(userId)
        );

        verify(followRepository).deleteAllRelationshipsForUser(userId);
    }

}