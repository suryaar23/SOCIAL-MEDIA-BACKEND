
package com.suryaar.socialmedia.repository;

import com.suryaar.socialmedia.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, String> {

    boolean existsByTokenId(String tokenId);
    long deleteByExpiresAtBefore(LocalDateTime expirationTime);
}
