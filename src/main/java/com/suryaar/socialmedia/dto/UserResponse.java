
package com.suryaar.socialmedia.dto;

public class UserResponse {

    private Long userId;
    private String username;
    private String email;
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
