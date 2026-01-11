package com.locapro.backend.dto.portefeuille;

public record PortefeuilleResponse(
        Long id,
        String nom,
        String code,
        String type,          // "AGENCE", "PERSO", ...
        String typeGestion,   // "AGENCE", "PERSO", "AUTRE"
        Long agenceId,
        Long utilisateurId,
        boolean enabled
) {}
