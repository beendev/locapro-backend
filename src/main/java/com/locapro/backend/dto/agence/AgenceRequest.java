package com.locapro.backend.dto.agence;

import jakarta.validation.constraints.NotBlank;

public record AgenceRequest(
        @NotBlank String nom
) {}
