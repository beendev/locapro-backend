package com.locapro.backend.dto.bien;

public record ProprietaireEntrepriseRequest(
        Long entrepriseId,   // si existe déjà dans `entreprises`
        String raisonSociale,
        String email,
        String numeroTva
) {}
