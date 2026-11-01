
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response containing comment details and author information")
public class CommentResponse {

    @Schema(description = "Unique identifier of the comment", example = "501")
    private Long commentId;

    @Schema(description = "Username of the comment author", example = "surya_dev")
    private String username;

    @Schema(
            description = "URL of the author's profile picture",
            example = "https://example.com/profile.jpg",
            nullable = true
    )
    private String profilePicture;

    @Schema(description = "Text content of the comment", example = "Great post!")
    private String text;

    @Schema(
            description = "Date and time when the comment was created",
            example = "2026-10-25T14:30:00"
    )
    private LocalDateTime createdAt;

    @Schema(
            description = "Date and time when the comment was last updated",
            example = "2026-10-25T15:00:00"
    )
    private LocalDateTime updatedAt;

    public CommentResponse(
            Long commentId,
            String username,
            String profilePicture,
            String text,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.commentId = commentId;
        this.username = username;
        this.profilePicture = profilePicture;
        this.text = text;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getCommentId() {
        return commentId;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
        return profilePicture;
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
}
