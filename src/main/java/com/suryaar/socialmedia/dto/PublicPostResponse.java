
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response containing publicly accessible post details and viewer-specific like information")
public class PublicPostResponse {

    @Schema(description = "Unique identifier of the post", example = "101")
    private Long postId;

    @Schema(description = "Username of the post author", example = "surya_dev")
    private String username;

    @Schema(
            description = "Text content of the post",
            example = "Exploring Spring Boot!",
            nullable = true
    )
    private String text;

    @Schema(
            description = "Date and time when the post was created",
            example = "2026-10-25T14:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the post was last updated",
            example = "2026-10-25T15:00:00"
    )
    private LocalDateTime updatedAt;

    @Schema(
            description = "Indicates whether the current viewer has liked this post. May be null for unauthenticated viewers.",
            example = "true",
            nullable = true
    )
    private Boolean likedByMe;

    public PublicPostResponse(
            Long postId,
            String username,
            String text,
            LocalDateTime createdAt,
            LocalDateTime updatedAt, Boolean likedByMe) {
        this.postId = postId;
        this.username = username;
        this.text = text;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likedByMe = likedByMe;
    }

    public PublicPostResponse(Long postId, String username, String profilePicture, String text, List<MediaResponse> mediaResponses, long likeCount, long commentCount, LocalDateTime createdAt, LocalDateTime updatedAt, Boolean likedByMe) {

        this.postId = postId;
        this.username = username;
        this.text = text;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.likedByMe = likedByMe;
    }

    public Long getPostId() {
        return postId;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Boolean getLikedByMe() {
        return likedByMe;
    }
}
