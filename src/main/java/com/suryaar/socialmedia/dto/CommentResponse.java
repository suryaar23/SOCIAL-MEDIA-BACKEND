
package com.suryaar.socialmedia.dto;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long commentId;
    private String username;
    private String profilePicture;
    private String text;
    private LocalDateTime createdAt;
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
