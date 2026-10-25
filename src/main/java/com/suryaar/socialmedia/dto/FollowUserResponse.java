
package com.suryaar.socialmedia.dto;

public class FollowUserResponse {

    private String username;
    private String profilePicture;

    public FollowUserResponse(
            String username,
            String profilePicture) {
        this.username = username;
        this.profilePicture = profilePicture;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
