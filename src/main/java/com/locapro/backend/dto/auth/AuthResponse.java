package com.locapro.backend.dto.auth;
public record AuthResponse(
        String accessToken,
        long accessExpiresInSeconds

) {}