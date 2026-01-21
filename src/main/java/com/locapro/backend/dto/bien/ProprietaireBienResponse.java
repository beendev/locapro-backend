package com.locapro.backend.dto.bien;

import java.time.LocalDate;

public record ProprietaireBienResponse(
        String type,            // "PERSONNE" ou "ENTREPRISE"
        String nom,
        String prenom,
        String email,
        LocalDate dateNaissance,
        String lieuNaissance,
        String raisonSociale,

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