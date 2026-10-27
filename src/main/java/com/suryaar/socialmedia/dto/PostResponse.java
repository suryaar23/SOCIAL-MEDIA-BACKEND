package com.suryaar.socialmedia.dto;

import java.time.LocalDateTime;

public class PostResponse {

    private Long postId;
    private Long userId;
    private String username;
    private String text;
    private LocalDateTime createdAt;
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