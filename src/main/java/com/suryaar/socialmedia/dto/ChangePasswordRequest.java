
package com.suryaar.socialmedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request containing the current password and new password details")
public class ChangePasswordRequest {

    @Schema(
            description = "User's existing account password",
            example = "OldPass1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(
            description = "New account password. Must be exactly 8 characters.",
            example = "NewPass12",
            minLength = 8,
            maxLength = 8,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 8, message = "New password must be exactly 8 characters")
    private String newPassword;

    @Schema(
            description = "Confirmation of the new password. Must match newPassword.",
            example = "NewPass12",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
