package com.locapro.backend.dto.bien;

import java.time.LocalDate;

public record ProprietairePersonneRequest(
        String nom,
        String prenom,
        String email,
        String telephone,
        LocalDate dateNaissance,
        String lieuNaissance,// Nouveau

        // Adresse
        String rue,
        String numero,
        String boite,
        String codePostal,
        String ville,
        String pays
) {}