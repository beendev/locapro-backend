package com.locapro.backend.service;

import com.locapro.backend.entity.RefreshTokenEntity;
import com.locapro.backend.entity.UtilisateurEntity;

public interface RefreshTokenService {
    RefreshTokenEntity create(UtilisateurEntity user);
    RefreshTokenEntity rotate(String rawToken);
    void revoke(String token);
    void revokeAllForUser(Long userId);
    void revokeByToken(String token);

}
