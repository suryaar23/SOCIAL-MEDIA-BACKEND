
package com.suryaar.socialmedia.dto;

public class MeResponse {

    private String username;
    private String email;
    private String phone;
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
