
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response containing post details for administrative views")
public class AdminPostResponse {

    @Schema(description = "Unique identifier of the post", example = "101")
    private Long postId;

    @Schema(description = "Username of the post author", example = "surya_dev")
    private String username;

    @Schema(description = "URL of the author's profile picture",
            example = "https://example.com/profile.jpg",
            nullable = true)
    private String profilePicture;

    @Schema(description = "Text content of the post",
            example = "Exploring Spring Boot and REST APIs",
            nullable = true)
    private String text;

    @Schema(description = "Media files attached to the post")
    private List<MediaResponse> media;

    @Schema(description = "Total number of likes on the post", example = "25")
    private long likeCount;

    @Schema(description = "Total number of comments on the post", example = "8")
    private long commentCount;

    @Schema(description = "Date and time when the post was created",
            example = "2026-10-25T14:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Date and time when the post was last updated",
            example = "2026-10-25T15:45:00")
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
