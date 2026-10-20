
package com.suryaar.socialmedia.security;

import com.suryaar.socialmedia.entity.RevokedToken;
import com.suryaar.socialmedia.repository.RevokedTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenRevocationService {

    private final RevokedTokenRepository revokedTokenRepository;

    public TokenRevocationService(
            RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional
    public void revokeToken(
            String tokenId,
            Long userId,
            LocalDateTime expiresAt) {

        if (!revokedTokenRepository.existsByTokenId(tokenId)) {
            RevokedToken revokedToken = new RevokedToken(
                    tokenId,
                    userId,
                    expiresAt,
                    LocalDateTime.now(ZoneOffset.UTC)
            );

            revokedTokenRepository.save(revokedToken);
        }
    }

    public boolean isTokenRevoked(String tokenId) {
        return revokedTokenRepository.existsByTokenId(tokenId);
    }
}
