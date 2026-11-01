package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing a user's public profile information and follow statistics")
public class PublicProfileResponse {

    @Schema(
            description = "Username of the user",
            example = "surya_dev"
    )
    private String username;

    @Schema(
            description = "URL of the user's profile picture",
            example = "https://example.com/profile.jpg",
            nullable = true
    )
    private String profilePicture;

    @Schema(
            description = "Total number of followers",
            example = "150"
    )
    private long followersCount;

    @Schema(
            description = "Total number of users this user follows",
            example = "85"
    )
    private long followingCount;

    @Schema(
            description = "Indicates whether the current viewer follows this user. May be null for unauthenticated viewers.",
            example = "true",
            nullable = true
    )
    private Boolean isFollowing;

    public PublicProfileResponse(
            String username,
            String profilePicture,
            long followersCount,
            long followingCount,
            Boolean isFollowing) {
        this.username = username;
        this.profilePicture = profilePicture;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
        this.isFollowing = isFollowing;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public long getFollowersCount() {
        return followersCount;
    }

    public long getFollowingCount() {
        return followingCount;
    }

    public Boolean getIsFollowing() {
        return isFollowing;
    }
}
