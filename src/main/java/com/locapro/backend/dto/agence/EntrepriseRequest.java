package com.locapro.backend.dto.agence;

public record EntrepriseRequest(
        String raisonSociale,
        String numeroTva,     // format BE à valider côté service
        String emailPro,      // peut être pré-rempli côté front
        String telephonePro,
        String iban,
        String siteWeb,

        // Adresse de l'entreprise (éclatée)
        String rue,
        String numero,
        String boite,
        String codePostal,
        String ville,
        String commune,
        String pays,
        Double latitude,
        Double longitude
) {}
