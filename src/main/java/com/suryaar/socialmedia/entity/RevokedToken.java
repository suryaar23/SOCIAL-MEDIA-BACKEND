
package com.suryaar.socialmedia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "revoked_tokens",
        indexes = {
                @Index(name = "idx_revoked_tokens_user_id", columnList = "user_id"),
                @Index(name = "idx_revoked_tokens_expires_at", columnList = "expires_at")
        }
)
public class RevokedToken {

    @Id
    @Column(name = "token_id", length = 36, nullable = false)
    private String tokenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at", nullable = false)
    private LocalDateTime revokedAt;

    protected RevokedToken() {
    }

    public RevokedToken(
            String tokenId,
            Long userId,
            LocalDateTime expiresAt,
            LocalDateTime revokedAt) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.revokedAt = revokedAt;
    }

    public String getTokenId() {
        return tokenId;
    }

    public Long getUserId() {
        return userId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
}
