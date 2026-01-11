package com.locapro.backend.dto.agence;

public record AgenceResponse(
        Long id,
        String nom,
        Long entrepriseId,
        Long adminAgenceId,
        Boolean enabled
) {}
