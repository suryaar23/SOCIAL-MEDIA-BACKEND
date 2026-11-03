package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.entity.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

class MediaStorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    private MediaStorageService mediaStorageService;
    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws IOException {
        mocks = MockitoAnnotations.openMocks(this);
        mediaStorageService = new MediaStorageService(s3Client, s3Presigner);
        ReflectionTestUtils.setField(
                mediaStorageService, "bucketName", "test-bucket");
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    private byte[] validJpegBytes() throws IOException {
        try (var input = getClass().getResourceAsStream("/test-image.jpg")) {
            if (input == null) {
                throw new IOException("Test image not found");
            }
            return input.readAllBytes();
        }
    }

    @Test
    void shouldReturnCorrectMediaType() {
        MockMultipartFile image = new MockMultipartFile(
                "file", "photo.jpg", "image/jpeg",
                new byte[]{1, 2, 3}
        );

        assertEquals(
                MediaType.IMAGE,
                mediaStorageService.determineMediaType(image)
        );
    }

    @Test
    void shouldRejectEmptyFile() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> mediaStorageService.storeFile(file)
        );
    }

    @Test
    void shouldRejectUnsupportedFileExtension() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.exe", "application/octet-stream",
                new byte[]{1, 2, 3}
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> mediaStorageService.storeFile(file)
        );
    }

    @Test
    void shouldRejectFileWhenContentDoesNotMatchExtension()
            throws IOException {
        // Plain text content disguised as a JPEG image.
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.jpg", "image/jpeg",
                "This is not an image".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> mediaStorageService.storeFile(file)
        );
    }

    @Test
    void shouldDeleteStoredFileSuccessfully() throws IOException {
        // Use a valid, minimal JPEG file for this test.
        byte[] jpegBytes = java.util.Base64.getDecoder().decode(
                "/9j/4AAQSkZJRgABAQAAAQABAAD/2Q=="
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", jpegBytes
        );

        String storedFilename = mediaStorageService.storeFile(file);

        mediaStorageService.deleteFile(storedFilename);

        // Deleting again should be harmless because deleteIfExists is used.
        assertDoesNotThrow(
                () -> mediaStorageService.deleteFile(storedFilename)
        );
    }

    @Test
    void shouldStoreValidFileSuccessfully() throws IOException {
        // Use a known-valid JPEG fixture for Apache Tika.
        byte[] validJpegBytes = validJpegBytes();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                validJpegBytes
        );

        String storedFilename = mediaStorageService.storeFile(file);

        assertNotNull(storedFilename);
        assertTrue(storedFilename.endsWith(".jpg"));
        verify(s3Client).putObject(
                any(software.amazon.awssdk.services.s3.model.PutObjectRequest.class),
                any(software.amazon.awssdk.core.sync.RequestBody.class)
        );
    }

    @Test
    void shouldRejectFileExceedingMaximumSize() throws IOException {
        byte[] oversizedContent = new byte[25 * 1024 * 1024 + 1];

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                oversizedContent
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> mediaStorageService.storeFile(file)
        );
    }
}