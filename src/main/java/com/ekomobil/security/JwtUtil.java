package com.ekomobil.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static final long DEFAULT_CLOCK_SKEW_SECONDS = 60;

    private final Key key;
    private final long defaultExpiresMs;

    public JwtUtil(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiresInMinutes}") long expiresInMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.defaultExpiresMs = expiresInMinutes * 60_000L;
    }


    public String generate(Long userId, String email) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + defaultExpiresMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(DEFAULT_CLOCK_SKEW_SECONDS)
                .build()
                .parseClaimsJws(token);
    }

    public Long getUserId(String token) {
        return Long.valueOf(parse(token).getBody().getSubject());
    }

    public String getEmail(String token) {
        Object e = parse(token).getBody().get("email");
        return (e == null) ? null : String.valueOf(e);
    }


    public String createToken(String subject, Map<String, Object> extraClaims, Duration ttl) {
        long nowMs = System.currentTimeMillis();
        Date now = new Date(nowMs);
        Date exp = new Date(nowMs + ttl.toMillis());
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createToken(String subject, Map<String, Object> extraClaims, Duration ttl, String audience) {
        long nowMs = System.currentTimeMillis();
        Date now = new Date(nowMs);
        Date exp = new Date(nowMs + ttl.toMillis());
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setAudience(audience)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createToken(String subject, Duration ttl) {
        return createToken(subject, Map.of(), ttl);
    }

    public String createToken(String subject, Map<String, Object> extraClaims) {
        return createToken(subject, extraClaims, Duration.ofMillis(defaultExpiresMs));
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(DEFAULT_CLOCK_SKEW_SECONDS)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String createResetToken(String email, Duration ttl) {
        Map<String, Object> claims = Map.of(
                "scope", "password_reset",
                "typ", "pwd-reset",
                "email", email // convenience if you want it (optional)
        );
        return createToken(email, claims, ttl, "reset");
    }

    public Claims parseAndValidateReset(String token) {
        var body = Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(DEFAULT_CLOCK_SKEW_SECONDS)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String scope = body.get("scope", String.class);
        String aud = body.getAudience();

        if (!"password_reset".equals(scope) || !"reset".equals(aud)) {
            throw new IllegalArgumentException("Invalid reset token");
        }
        return body;
    }
}
