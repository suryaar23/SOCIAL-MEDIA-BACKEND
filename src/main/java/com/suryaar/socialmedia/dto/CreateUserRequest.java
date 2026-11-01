
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request containing the details required to register a new user account")
public class CreateUserRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 10,
            message = "Username must be between 5 and 10 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Username can contain only letters, numbers, and underscores")
    @Schema(
            description = "Unique username. Allows letters, numbers and underscores.",
            example = "surya_dev",
            minLength = 5,
            maxLength = 10,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(
            description = "User's email address.",
            example = "surya@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "Phone number is required")
    @Schema(
            description = "User's phone number.",
            example = "9876543210",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 8,
            message = "Password must be exactly 8 characters long")
    @Schema(
            description = "Account password. Must be exactly 8 characters.",
            example = "Pass@123",
            minLength = 8,
            maxLength = 8,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    public CreateUserRequest() {
    }

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

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }
}
