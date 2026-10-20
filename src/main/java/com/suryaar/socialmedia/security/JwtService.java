
package com.suryaar.socialmedia.security;

import com.suryaar.socialmedia.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final TokenRevocationService tokenRevocationService;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs, TokenRevocationService tokenRevocationService) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
        this.tokenRevocationService = tokenRevocationService;
    }

    public String generateToken(User user) {

        if (user == null
                || user.getUserId() == null
                || user.getTokenVersion() == null) {
            throw new IllegalArgumentException(
                    "User, user ID and token version must not be null");
        }

        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getUserId().toString())
                .id(UUID.randomUUID().toString())
                .claim("tokenVersion", user.getTokenVersion())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }

    public Long extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    public boolean isTokenValid(String token, Long expectedUserId) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject().equals(expectedUserId.toString())
                    && claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }


    public String extractTokenId(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getId();
    }

    public LocalDateTime extractExpiration(String token) {
        Instant expiration = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .toInstant();

        return LocalDateTime.ofInstant(expiration, ZoneOffset.UTC);
    }

    public Integer extractTokenVersion(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("tokenVersion", Integer.class);
    }

}
