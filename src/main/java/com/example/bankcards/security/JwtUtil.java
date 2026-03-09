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
    private final long expiration;

    @Value("${jwt.secret}")
    private String secret;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    @PostConstruct
    public void init() {
        System.out.println("🔑 JWT Secret length: " + secret.length() + " chars");
        System.out.println("🔐 Key size: " + key.getEncoded().length + " bytes (" + (key.getEncoded().length * 8) + " bits)");
    }

    public String generateToken(String username, String role) {
    System.out.println("🔐 Генерация токена для: " + username + ", роль: " + role);
    try {
        String token = Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
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

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String extractRole(String token) {
    Claims claims = getClaims(token);
    return claims.get("role", String.class);
}
}