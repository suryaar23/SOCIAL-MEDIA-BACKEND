
package com.suryaar.socialmedia.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateMyProfileRequest {

    @Size(min = 5, max = 10)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Username may contain only letters, numbers and underscores")
    private String username;

    @Email(message = "Email must be valid")
    private String email;

    private String phone;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
