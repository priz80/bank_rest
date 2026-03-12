package com.example.bankcards.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import jakarta.annotation.PostConstruct;

@Component
public class JwtUtil {

    private final Key key;
    private final long expirationMs;

    @Value("${jwt.secret}")
    private String secret;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    @PostConstruct
    public void init() {
        System.out.println("🔑 JWT Secret length: " + secret.length() + " chars");
        System.out.println(
                "🔐 Key size: " + key.getEncoded().length + " bytes (" + (key.getEncoded().length * 8) + " bits)");
    }

    public String generateToken(String username, String role) {
        System.out.println("🔐 Генерация токена для: " + username + ", роль: " + role);
        try {
            String token = Jwts.builder()
                    .setSubject(username)
                    .claim("role", role)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();
            System.out.println("✅ Токен успешно сгенерирован: " + token.substring(0, 20) + "...");
            return token;
        } catch (Exception e) {
            System.err.println("❌ ОШИБКА при генерации токена:");
            e.printStackTrace();
            throw e;
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.err.println("⚠️ JWT expired, but claims extracted");
            return e.getClaims();
        } catch (Exception e) {
            System.err.println("❌ Invalid JWT: " + e.getMessage());
            return null;
        }
    }

    public String extractUsername(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String extractRole(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    public Date extractExpiration(String token) {
        Claims claims = getClaims(token);
        return claims != null ? claims.getExpiration() : null;
    }

    public boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (username != null &&
                extractedUsername != null &&
                extractedUsername.equals(username) &&
                !isTokenExpired(token));
    }
}