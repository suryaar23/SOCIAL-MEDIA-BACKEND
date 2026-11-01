
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing basic information about a registered user")
public class UserResponse {

    @Schema(
            description = "Unique identifier of the user",
            example = "101"
    )
    private Long userId;

    @Schema(
            description = "Username of the registered user",
            example = "surya_dev"
    )
    private String username;

    @Schema(
            description = "Email address of the user",
            example = "surya@example.com"
    )
    private String email;

    @Schema(
            description = "URL of the user's profile picture. Null if no picture has been uploaded.",
            example = "https://example.com/profile/surya.jpg",
            nullable = true
    )
    private String profilePicture;  

    public UserResponse() {
    }

    public UserResponse(
            Long userId,
            String username,
            String email,
            String profilePicture) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.profilePicture = profilePicture;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
