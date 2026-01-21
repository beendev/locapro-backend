package com.locapro.backend.dto.auth;

import com.locapro.backend.dto.user.UserResponse;

public record LoginResult(
        String accessToken,
        long accessExpiresInSeconds,
        String refreshTokenValue,
        UserResponse user // 👈 AJOUTÉ
) {}