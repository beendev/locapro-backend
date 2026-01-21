package com.locapro.backend.dto.portefeuille;

public record PortefeuilleMembreResponse(
        Long userId,
        String nom,
        String prenom,
        String email,
        String role // "GESTIONNAIRE", "ADMIN"...
) {}