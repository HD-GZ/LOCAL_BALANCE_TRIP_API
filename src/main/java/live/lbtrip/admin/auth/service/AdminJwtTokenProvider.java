package live.lbtrip.admin.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import live.lbtrip.admin.admin.model.Admin;
import live.lbtrip.admin.auth.model.AdminJwtTokenSubject;

@Component
public class AdminJwtTokenProvider {

    private final SecretKey secretKey;
    private final Duration accessTokenExpiration;

    public AdminJwtTokenProvider(
        @Value("${app.jwt.admin-secret}") String secret,
        @Value("${app.jwt.admin-access-token-expiration}") Duration accessTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String createAccessToken(Admin admin) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(accessTokenExpiration);

        return Jwts.builder()
            .subject(String.valueOf(admin.getId()))
            .id(UUID.randomUUID().toString())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey)
            .compact();
    }

    public boolean isValid(String token) {
        try {
            parseSubject(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public AdminJwtTokenSubject parseSubject(String token) {
        var claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();

        return AdminJwtTokenSubject.of(Long.parseLong(claims.getSubject()));
    }
}
