
package com.suryaar.socialmedia.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdminPostResponse {

    private Long postId;
    private String username;
    private String profilePicture;
    private String text;
    private List<MediaResponse> media;
    private long likeCount;
    private long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AdminPostResponse(
            Long postId,
            String username,
            String profilePicture,
            String text,
            List<MediaResponse> media,
            long likeCount,
            long commentCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        this.postId = postId;
        this.username = username;
        this.profilePicture = profilePicture;
        this.text = text;
        this.media = media;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getPostId() { return postId; }
    public String getUsername() { return username; }
    public String getProfilePicture() { return profilePicture; }
    public String getText() { return text; }
    public List<MediaResponse> getMedia() { return media; }
    public long getLikeCount() { return likeCount; }
    public long getCommentCount() { return commentCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
