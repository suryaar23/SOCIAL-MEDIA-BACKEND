package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(
        description = "Request for creating a post. At least one of text or mediaIds must be provided."
)
public class CreatePostRequest {

    @Size(max = 250, message = "Post text cannot exceed 250 characters")
    @Schema(
            description = "Optional text content of the post. Maximum 250 characters.",
            example = "Exploring Spring Boot today!",
            maxLength = 250,
            nullable = true
    )
    private String text;

    @Schema(
            description = "Optional list of IDs of previously uploaded media files attached to the post. A post must contain text, media, or both.",
            example = "[101, 102]",
            nullable = true
    )
    private List<Long> mediaIds;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Long> getMediaIds() {
        return mediaIds;
    }

    public void setMediaIds(List<Long> mediaIds) {
        this.mediaIds = mediaIds;
    }
}