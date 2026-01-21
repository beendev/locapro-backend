package com.locapro.backend.dto.auth;

import com.locapro.backend.dto.user.UserResponse;

public record AuthResponse(
        String accessToken,
        long accessExpiresInSeconds,
             UserResponse user
) {}