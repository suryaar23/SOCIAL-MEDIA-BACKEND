
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing the authenticated user's profile information")
public class MeResponse {

    @Schema(description = "Username of the authenticated user", example = "surya_dev")
    private String username;

    @Schema(description = "Email address of the authenticated user", example = "surya@example.com")
    private String email;

    @Schema(description = "Phone number of the authenticated user", example = "9876543210")
    private String phone;

    @Schema(
            description = "URL of the user's profile picture",
            example = "https://example.com/profile.jpg",
            nullable = true
    )
    private String profilePicture;

    public MeResponse(
            String username,
            String email,
            String phone,
            String profilePicture) {
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.profilePicture = profilePicture;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfilePicture() {
        return profilePicture;
    }
}
