package com.locapro.backend.service.impl;

import com.locapro.backend.entity.RefreshTokenEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.repository.RefreshTokenRepository;
import com.locapro.backend.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository repo;

    @Value("${app.auth.refresh-ttl-days:30}")
    private long refreshTtlDays;

    public RefreshTokenServiceImpl(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    @Override
    public RefreshTokenEntity create(UtilisateurEntity user) {
        var now = Instant.now();
        var entity = new RefreshTokenEntity();
        entity.setToken(UUID.randomUUID().toString()); // opaque et imprévisible
        entity.setUser(user);
        entity.setExpiresAt(now.plus(Duration.ofDays(refreshTtlDays)));
        entity.setRevoked(false);
        return repo.save(entity);
    }

    @Override
    public RefreshTokenEntity rotate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token manquant");
        }
        final String token = rawToken.trim();

        // DEBUG
        System.out.println(">>> [DEBUG] rotate() called with token = " + token);

        var currentOpt = repo.findByTokenAndRevokedFalse(token);
        if (currentOpt.isEmpty()) {
            System.out.println(">>> [DEBUG] token introuvable ou déjà révoqué en DB");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalide");
        }

        var current = currentOpt.get();
        System.out.println(">>> [DEBUG] token FOUND: revoked=" + current.isRevoked()
                + ", expiresAt=" + current.getExpiresAt());

        var now = Instant.now();

        if (current.getExpiresAt() == null || current.getExpiresAt().isBefore(now)) {
            current.setRevoked(true);
            repo.save(current);
            System.out.println(">>> [DEBUG] token expiré -> marqué révoqué");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expiré");
        }

        current.setRevoked(true);
        repo.save(current);

        var next = new RefreshTokenEntity();
        next.setUser(current.getUser());
        next.setToken(UUID.randomUUID().toString());
        next.setExpiresAt(now.plus(Duration.ofDays(refreshTtlDays)));
        next.setRevoked(false);

        var saved = repo.save(next);
        System.out.println(">>> [DEBUG] nouveau refresh token = " + saved.getToken());

        return saved;
    }

    @Override
    public void revoke(String token) {
        if (token == null || token.isBlank()) return;
        repo.findByTokenAndRevokedFalse(token.trim()).ifPresent(rt -> {
            rt.setRevoked(true);
            repo.save(rt);
        });
    }

    @Override
    public void revokeByToken(String token) {
        if (token == null || token.isBlank()) return;
        repo.revokeByToken(token.trim());
    }

    @Override
    public void revokeAllForUser(Long userId) {
        if (userId == null) return;
        repo.revokeAllActiveByUserId(userId);
    }
}
