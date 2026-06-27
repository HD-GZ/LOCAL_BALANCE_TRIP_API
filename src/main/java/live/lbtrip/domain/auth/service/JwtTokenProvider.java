package live.lbtrip.domain.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import live.lbtrip.domain.auth.model.JwtTokenSubject;
import live.lbtrip.domain.user.model.User;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;
    private final Duration refreshTokenExpiration;

    public JwtTokenProvider(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.access-token-expiration}") Duration accessTokenExpiration,
        @Value("${app.jwt.refresh-token-expiration}") Duration refreshTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(User user) {
        return createToken(user, accessTokenExpiration);
    }

    public String createRefreshToken(User user) {
        return createToken(user, refreshTokenExpiration);
    }

    public boolean isValid(String token) {
        try {
            parseSubject(token);
            return false;
        } catch (JwtException | IllegalArgumentException exception) {
            return true;
        }
    }

    public JwtTokenSubject parseSubject(String token) {
        var claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return JwtTokenSubject.of(Long.parseLong(claims.getSubject()));
    }

    public LocalDateTime refreshTokenExpiresAt() {
        return LocalDateTime.now().plus(refreshTokenExpiration);
    }

    private String createToken(User user, Duration expiration) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiration);

        return Jwts.builder()
            .subject(String.valueOf(user.getId()))
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey)
            .compact();
    }
}
