
package com.suryaar.socialmedia.dto;

public class PublicProfileResponse {

    private String username;
    private String profilePicture;
    private long followersCount;
    private long followingCount;
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
