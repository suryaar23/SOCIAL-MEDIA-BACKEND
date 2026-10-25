
package com.suryaar.socialmedia.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PublicPostResponse {

    private Long postId;
    private String username;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
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
