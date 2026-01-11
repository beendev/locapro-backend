package com.locapro.backend.dto.auth;

// Ce DTO sert à transporter les infos du Service vers le Controller
public record LoginResult(
        String accessToken,
        long accessExpiresInSeconds,
        String refreshTokenValue // <--- IL DOIT ÊTRE LÀ ! (Pour que le controller puisse créer le cookie)
) {}