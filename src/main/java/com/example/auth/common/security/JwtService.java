package com.example.auth.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import com.example.auth.entities.User;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    // ─── Generate access token ─────────────────────────────
    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpiry, "access");
    }

    // ─── Generate refresh token ────────────────────────────
    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpiry, "refresh");
    }

    // ─── Build token ───────────────────────────────────────
    private String buildToken(User user, long expiry, String tokenType) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("tokenType", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(getSigningKey())
                .compact();
    }

    // ─── Extract all claims ────────────────────────────────
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ─── Extract email ─────────────────────────────────────
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ─── Extract userId ────────────────────────────────────
    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    // ─── Check if token is valid ───────────────────────────
    public boolean isTokenValid(String token, User user) {
        final String email = extractEmail(token);
        return email.equals(user.getEmail()) && !isTokenExpired(token);
    }

    // ─── Check if access token ─────────────────────────────
    public boolean isAccessToken(String token) {
        return "access".equals(extractAllClaims(token).get("tokenType", String.class));
    }

    // ─── Check expiry ──────────────────────────────────────
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // ─── Signing key ───────────────────────────────────────
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
