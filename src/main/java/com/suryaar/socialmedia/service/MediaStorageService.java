package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.entity.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.apache.tika.Tika;
import java.io.InputStream;

@Service
public class MediaStorageService {

    private final Tika tika = new Tika();

    private final Path uploadDirectory = Paths.get("uploads")
            .toAbsolutePath()
            .normalize();

    public MediaStorageService() throws IOException {
        Files.createDirectories(uploadDirectory);
    }

    public String storeFile(MultipartFile file) throws IOException {

        validateFile(file);

        validateFileType(file);

        validateActualFileType(file);

        String originalFilename = file.getOriginalFilename();

        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(
                    originalFilename.lastIndexOf(".")
            );
        }

        String storedFilename = UUID.randomUUID() + extension;

        Path targetPath = uploadDirectory.resolve(storedFilename).normalize();

        if (!targetPath.startsWith(uploadDirectory)) {
            throw new IOException("Invalid file path");
        }

        Files.copy(file.getInputStream(), targetPath);

        return storedFilename;
    }

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        long maxFileSize = 25L * 1024 * 1024;

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    "File size cannot exceed 25 MB"
            );
        }
    }

    private void validateFileType(MultipartFile file) {

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException(
                    "File must have a valid extension"
            );
        }

        String extension = filename.substring(
                filename.lastIndexOf(".") + 1
        ).toLowerCase();

        boolean allowed = switch (extension) {
            case "jpg", "jpeg", "png", "webp",
                 "mp4", "webm", "mov",
                 "mp3", "wav", "ogg" -> true;
            default -> false;
        };

        if (!allowed) {
            throw new IllegalArgumentException(
                    "Unsupported file type"
            );
        }
    }

    public MediaType determineMediaType(MultipartFile file) {

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException(
                    "File must have a valid extension"
            );
        }

        String extension = filename.substring(
                filename.lastIndexOf(".") + 1
        ).toLowerCase();

        return switch (extension) {
            case "jpg", "jpeg", "png", "webp" -> MediaType.IMAGE;
            case "mp4", "webm", "mov" -> MediaType.VIDEO;
            case "mp3", "wav", "ogg" -> MediaType.AUDIO;
            default -> throw new IllegalArgumentException(
                    "Unsupported media type"
            );
        };
    }

    public String getFileUrl(String storedFilename) {
        return "/uploads/" + storedFilename;
    }

    private void validateActualFileType(MultipartFile file) throws IOException {

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException(
                    "File must have a valid extension"
            );
        }

        String extension = filename.substring(
                filename.lastIndexOf(".") + 1
        ).toLowerCase();

        String detectedType;

        try (InputStream inputStream = file.getInputStream()) {
            detectedType = tika.detect(inputStream, filename);
        }

        boolean valid = switch (extension) {
            case "jpg", "jpeg" -> detectedType.equals("image/jpeg");
            case "png" -> detectedType.equals("image/png");
            case "webp" -> detectedType.equals("image/webp");
            case "mp4" -> detectedType.equals("video/mp4");
            case "webm" -> detectedType.equals("video/webm");
            case "mov" -> detectedType.equals("video/quicktime");
            case "mp3" -> detectedType.equals("audio/mpeg");
            case "wav" -> detectedType.equals("audio/vnd.wave")
                    || detectedType.equals("audio/wav")
                    || detectedType.equals("audio/x-wav");
            case "ogg" -> detectedType.equals("audio/ogg");
            default -> false;
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "File content does not match its extension"
            );
        }
    }

    public void deleteFile(String storedFilename) throws IOException {
        Path filePath = uploadDirectory.resolve(storedFilename).normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new IOException("Invalid file path");
        }

        Files.deleteIfExists(filePath);
    }
}