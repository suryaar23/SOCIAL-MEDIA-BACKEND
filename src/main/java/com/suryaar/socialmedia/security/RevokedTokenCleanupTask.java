
package com.suryaar.socialmedia.security;

import com.suryaar.socialmedia.repository.RevokedTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class RevokedTokenCleanupTask {

    private final RevokedTokenRepository revokedTokenRepository;

    public RevokedTokenCleanupTask(
            RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void deleteExpiredRevokedTokens() {
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        revokedTokenRepository
                .deleteByExpiresAtBefore(nowUtc);
    }
}
