package com.suryaar.socialmedia.controller;

import com.suryaar.socialmedia.dto.MediaResponse;
import com.suryaar.socialmedia.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Tag(name = "Media", description = "Upload and delete post media")
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Upload media",
            description = "Uploads an image or video file for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Media uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or upload request"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public MediaResponse uploadMedia(
            @Parameter(
                    description = "Media file to upload",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Long userId
    ) throws IOException {
        return mediaService.uploadMedia(file, userId);
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete media",
            description = "Deletes the specified media file belonging to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Media deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Media not found")
    })
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(
            @PathVariable Long mediaId,
            @AuthenticationPrincipal Long userId
    ) throws IOException {

        mediaService.deleteMedia(mediaId, userId);

        return ResponseEntity.noContent().build();
    }
}
