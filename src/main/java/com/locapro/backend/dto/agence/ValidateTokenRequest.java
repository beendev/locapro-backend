package com.locapro.backend.dto.agence;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequest(
        @NotBlank(message = "Le token est obligatoire")
        String token
) {}