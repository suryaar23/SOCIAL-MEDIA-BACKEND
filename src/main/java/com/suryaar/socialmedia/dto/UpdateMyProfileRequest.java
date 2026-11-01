
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for updating the authenticated user's profile. All fields are optional.")
public class UpdateMyProfileRequest {

    @Size(min = 5, max = 10)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Username may contain only letters, numbers and underscores")
    @Schema(
            description = "New username. Must contain 5–10 characters and may include only letters, numbers and underscores.",
            example = "surya_dev",
            minLength = 5,
            maxLength = 10,
            nullable = true
    )
    private String username;

    @Email(message = "Email must be valid")
    @Schema(
            description = "New email address",
            example = "surya@example.com",
            nullable = true
    )
    private String email;

    @Schema(
            description = "New phone number",
            example = "9876543210",
            nullable = true
    )
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
