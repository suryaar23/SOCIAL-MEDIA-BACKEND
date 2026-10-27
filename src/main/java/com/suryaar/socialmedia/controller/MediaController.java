package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.entity.Media;
import com.suryaar.socialmedia.service.MediaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Media uploadMedia(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long userId
    ) throws IOException {
        return mediaService.uploadMedia(file, userId);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable Long mediaId,
            @AuthenticationPrincipal Long userId
    ) throws IOException {

        mediaService.deleteMedia(mediaId, userId);

        return ResponseEntity.noContent().build();
    }
}