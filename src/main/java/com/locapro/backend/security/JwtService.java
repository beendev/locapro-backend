package com.locapro.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expSeconds;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.exp.minutes:15}") long expMinutes
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret manquant (JWT_SECRET)");
        }
        // HS256: clé >= 256 bits recommandé. Utilise une chaîne bien longue.
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expSeconds = Math.max(60, expMinutes * 60); // min 60s pour éviter les tokens trop courts
    }

    /** Génère un JWT “access token” minimal avec uid + sub (email). */
    public String generateAccessToken(long userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expSeconds);

        return Jwts.builder()
                .header()                       // header typ=JWT/alg=HS256 implicites
                .type("JWT").and()
                .subject(email)                 // sub
                .claims(Map.of("uid", userId))  // claims custom minimal
                .issuedAt(Date.from(now))       // iat
                .expiration(Date.from(exp))     // exp
                .signWith(key)                  // HS256 avec notre clé
                .compact();
    }

    /** (On s’en servira au filtre plus tard) : parse et valide la signature/exp. */
    public io.jsonwebtoken.Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpiresInSeconds() {
        return expSeconds;
    }
}
