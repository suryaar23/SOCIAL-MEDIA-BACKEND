package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response containing details of a created or updated post")
public class PostResponse {

    @Schema(description = "Unique identifier of the post", example = "101")
    private Long postId;

    @Schema(description = "ID of the user who created the post", example = "12")
    private Long userId;

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

    public PostResponse(
            Long postId,
            Long userId,
            String username,
            String text,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.text = text;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getUserId() {
        return userId;
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
}