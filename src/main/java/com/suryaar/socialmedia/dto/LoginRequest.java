
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request containing the credentials required to authenticate a user")
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    @Schema(
            description = "Username or email address used to log in",
            example = "surya_dev",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String identifier;

    @NotBlank(message = "Password is required")
    @Schema(
            description = "Account password",
            example = "Pass@123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    public LoginRequest() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
