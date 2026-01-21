package com.locapro.backend.dto.bien;

public record ProprietaireEntrepriseRequest(
        Long entrepriseId, // optionnel
        String raisonSociale,
        String email,
        String numeroTva, // C'est le BCE
        String representantLegal, // Nouveau
        String telephone, // Nouveau

        // Siège Social
        String rue,
        String numero,
        String boite,
        String codePostal,
        String ville,
        String pays
) {}