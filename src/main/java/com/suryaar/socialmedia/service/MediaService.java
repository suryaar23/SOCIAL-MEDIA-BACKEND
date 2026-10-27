package com.suryaar.socialmedia.service;

import com.suryaar.socialmedia.entity.User;
import com.suryaar.socialmedia.entity.UserStatus;
import com.suryaar.socialmedia.repository.MediaRepository;
import com.suryaar.socialmedia.repository.PostMediaRepository;
import com.suryaar.socialmedia.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.suryaar.socialmedia.entity.Media;
import com.suryaar.socialmedia.entity.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Service
public class MediaService {

    private final MediaStorageService mediaStorageService;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final PostMediaRepository postMediaRepository;

    public MediaService(
            MediaStorageService mediaStorageService,
            MediaRepository mediaRepository, UserRepository userRepository, PostMediaRepository postMediaRepository
    ) {
        this.mediaStorageService = mediaStorageService;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
        this.postMediaRepository = postMediaRepository;
    }

    public Media uploadMedia(MultipartFile file, Long userId) throws IOException {

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active user not found"
                ));

        String originalFilename = file.getOriginalFilename();

        String storedFilename = mediaStorageService.storeFile(file);

        try {
            String fileUrl = mediaStorageService.getFileUrl(storedFilename);

            MediaType mediaType = mediaStorageService.determineMediaType(file);

            Media media = new Media(
                    mediaType,
                    fileUrl,
                    originalFilename,
                    file.getSize(),
                    user
            );

            return mediaRepository.save(media);

        } catch (Exception e) {
            try {
                mediaStorageService.deleteFile(storedFilename);
            } catch (IOException cleanupException) {
                e.addSuppressed(cleanupException);
            }

            throw e;
        }
    }
    
    public void deleteMedia(Long mediaId, Long userId) throws IOException {

        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Media not found"
                ));

        if (media.getUploadedBy() == null
                || !media.getUploadedBy().getUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot delete media uploaded by another user"
            );
        }

        if (postMediaRepository.existsByMediaMediaId(mediaId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete media that is attached to a post"
            );
        }

        String storedFilename = media.getUrl().substring(
                media.getUrl().lastIndexOf("/") + 1
        );

        mediaStorageService.deleteFile(storedFilename);
        mediaRepository.delete(media);
    }

}