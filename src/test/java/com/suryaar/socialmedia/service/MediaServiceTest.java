package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.dto.MediaResponse;
import com.suryaar.socialmedia.entity.Media;
import com.suryaar.socialmedia.entity.MediaType;
import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.MediaRepository;
import com.suryaar.socialmedia.repository.PostMediaRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MediaServiceTest {

    @Mock
    private MediaStorageService mediaStorageService;

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostMediaRepository postMediaRepository;

    private MediaService mediaService;
    private AutoCloseable mocks;

    private User user;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);

        mediaService = new MediaService(
                mediaStorageService,
                mediaRepository,
                userRepository,
                postMediaRepository
        );

        user = new User();
        user.setUserId(1L);
        user.setStatus(UserStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void shouldUploadMediaSuccessfully() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(mediaStorageService.storeFile(file))
                .thenReturn("stored-photo.jpg");

        when(mediaStorageService.determineMediaType(file))
                .thenReturn(MediaType.IMAGE);

        when(mediaRepository.save(any(Media.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MediaResponse result = mediaService.uploadMedia(file, 1L);

        assertEquals("photo.jpg", result.getName());
        assertEquals(3L, result.getSize());
        verify(mediaRepository).save(any(Media.class));
    }

    @Test
    void shouldDeleteStoredFileWhenDatabaseSaveFails() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3}
        );

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(mediaStorageService.storeFile(file))
                .thenReturn("stored-photo.jpg");

        when(mediaStorageService.determineMediaType(file))
                .thenReturn(MediaType.IMAGE);

        when(mediaRepository.save(any(Media.class)))
                .thenThrow(new RuntimeException("Database failure"));

        assertThrows(RuntimeException.class,
                () -> mediaService.uploadMedia(file, 1L));

        verify(mediaStorageService).deleteFile("stored-photo.jpg");
    }

    @Test
    void shouldRejectDeletingAnotherUsersMedia() throws Exception {

        Media media = new Media(
                MediaType.IMAGE,
                "http://localhost/uploads/photo.jpg",
                "photo.jpg",
                100L,
                user
        );

        User anotherUser = new User();
        anotherUser.setUserId(2L);

        when(mediaRepository.findById(10L))
                .thenReturn(Optional.of(media));

        assertThrows(ResponseStatusException.class,
                () -> mediaService.deleteMedia(10L, 2L));

        verify(mediaStorageService, never()).deleteFile(anyString());
        verify(mediaRepository, never()).delete(any(Media.class));
    }

    @Test
    void shouldRejectDeletingMediaAttachedToPost() throws IOException {
        Media media = new Media(
                MediaType.IMAGE,
                "http://localhost/uploads/photo.jpg",
                "photo.jpg",
                100L,
                user
        );

        when(mediaRepository.findById(10L))
                .thenReturn(Optional.of(media));

        when(postMediaRepository.existsByMediaMediaId(10L))
                .thenReturn(true);

        assertThrows(ResponseStatusException.class,
                () -> mediaService.deleteMedia(10L, 1L));

        verify(mediaStorageService, never()).deleteFile(anyString());
        verify(mediaRepository, never()).delete(any(Media.class));
    }

    @Test
    void shouldDeleteUnattachedMediaSuccessfully() throws Exception {
        Media media = new Media(
                MediaType.IMAGE,
                "http://localhost/uploads/photo.jpg",
                "photo.jpg",
                100L,
                user
        );

        when(mediaRepository.findById(10L))
                .thenReturn(Optional.of(media));

        when(postMediaRepository.existsByMediaMediaId(10L))
                .thenReturn(false);

        mediaService.deleteMedia(10L, 1L);

        verify(mediaStorageService).deleteFile("photo.jpg");
        verify(mediaRepository).delete(media);
    }
}