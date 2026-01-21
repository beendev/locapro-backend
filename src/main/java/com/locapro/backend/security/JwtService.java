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
        // convertie minute en seconde car l'ordinateur compte en secondes
        this.expSeconds = Math.max(60, expMinutes * 60); // min 60s pour éviter les tokens trop courts
    }

    /** Génère un JWT “access token” minimal avec uid + sub (email). */

    public String generateAccessToken(long userId, String email) {
        // 1. Le Chronomètre ⏱️
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expSeconds);
        // On définit "Maintenant" (date de début) et "Dans 15 min" (date de fin).

        return Jwts.builder()
                // 2. L'En-tête (Header) 🏷️
                .header().type("JWT").and()
                // On écrit "Ceci est un document officiel JWT" sur le papier.

                // 3. Les Données (Payload) 📝
                .subject(email)                 // On écrit "Pour : jean@test.com"
                .claims(Map.of("uid", userId))  // On écrit "ID interne : 12"

                // 4. Les Dates de validité ⏳
                .issuedAt(Date.from(now))       // "Fabriqué à : 12h00"
                .expiration(Date.from(exp))     // "Expire à : 12h15"

                // 5. La Signature (Le Sceau) 🔒 <--- LE PLUS IMPORTANT
                .signWith(key)
                // On prend le tampon encreur officiel (la SecretKey du constructeur)
                // et on scelle le tout. Si quelqu'un change une lettre du token, le sceau se brise.

                // 6. L'emballage (Compact) 📦
                .compact();
        // On transforme tout ça en une longue chaîne de caractères "aaa.bbb.ccc"
    }

    public io.jsonwebtoken.Claims parseAndValidate(String token) {
        return Jwts.parser()
                // 1. Charger la clé de vérification 🔑
                .verifyWith(key)
                // On dit au scanner : "Utilise cette clé pour vérifier la signature".
                // Si la signature du token a été faite avec une autre clé (un faux token), ça plante.

                .build()

                // 2. L'Analyse (Le moment de vérité) 🕵️‍♂️
                .parseSignedClaims(token)
                // Ici, la librairie fait 3 vérifications AUTOMATIQUES :
                // a) Est-ce que la signature est valide ? (Intégrité)
                // b) Est-ce que le token est expiré ? (Date d'expiration < Maintenant)
                // c) Est-ce que le format est bon ?

                // 🚨 Si un seul truc cloche => Ça lance une EXCEPTION (le scanner sonne rouge !)

                // 3. L'Extraction 📤
                .getPayload();
        // Si tout est vert, on récupère les infos qui étaient dedans (email, userId...).
    }


}
