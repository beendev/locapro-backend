package com.locapro.backend.dto.agence;

public record EntrepriseResponse(
        Long id,
        String raisonSociale,
        String numeroTva,
        String emailPro,
        String telephonePro,
        String iban,
        String siteWeb,
        String adresseRue,
        String adresseNumero,
        String adresseBoite,
        String adresseCodePostal,
        String adresseVille,
        String adresseCommune,
        String adressePays,
        Double adresseLatitude,
        Double adresseLongitude,
        Boolean enabled
) {}