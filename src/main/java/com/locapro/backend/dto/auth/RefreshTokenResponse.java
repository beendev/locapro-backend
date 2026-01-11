package com.locapro.backend.dto.auth;

public record RefreshTokenResponse(
        String accessToken,
        long expiresInSeconds

) {}