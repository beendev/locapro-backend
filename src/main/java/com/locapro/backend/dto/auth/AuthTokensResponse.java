package com.locapro.backend.dto.auth;

public record AuthTokensResponse(
        String accessToken,
        long expiresInSeconds,
        String refreshTokenValue
) {}
