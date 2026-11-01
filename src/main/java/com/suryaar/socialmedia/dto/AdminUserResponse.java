
package com.suryaar.socialmedia.dto;

import com.suryaar.socialmedia.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Response containing user details for administrative views")
public class AdminUserResponse {

    @Schema(description = "Unique identifier of the user", example = "12")
    private Long userId;

    @Schema(description = "Username of the user", example = "surya_dev")
    private String username;

    @Schema(description = "Email address of the user", example = "surya@example.com")
    private String email;

    @Schema(description = "Current account status", example = "ACTIVE")
    private UserStatus status;

    @Schema(description = "Date and time when the account was created",
            example = "2026-10-25T14:30:00")
    private LocalDateTime createdAt;

    public AdminUserResponse(
            Long userId,
            String username,
            String email,
            UserStatus status,
            LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
