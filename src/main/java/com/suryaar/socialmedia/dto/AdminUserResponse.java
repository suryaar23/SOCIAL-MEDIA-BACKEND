
package com.suryaar.socialmedia.dto;

import com.suryaar.socialmedia.entity.UserStatus;
import java.time.LocalDateTime;

public class AdminUserResponse {

    private Long userId;
    private String username;
    private String email;
    private UserStatus status;
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
