
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing basic public information about a user in a followers or following list")
public class FollowUserResponse {

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
