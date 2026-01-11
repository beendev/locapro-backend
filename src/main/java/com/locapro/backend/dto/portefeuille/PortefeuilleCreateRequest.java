package com.locapro.backend.dto.portefeuille;

import jakarta.validation.constraints.NotBlank;

public record PortefeuilleCreateRequest(
        @NotBlank String nom,
        @NotBlank String code,
        // "AGENCE", "PERSO", "AUTRE" (on part sur AGENCE par défaut ici)
        String typeGestion
) {}
