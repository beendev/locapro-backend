package com.locapro.backend.dto.user;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String prenom,
        String nom,
        String email,
        LocalDate dateNaissance,
        String telephone,
        String numeroIpi,       // ✅ Présent
        boolean emailVerified,
        String rue,
        String numero,
        String boite,
        String codePostal,
        String ville,
        String pays,

        // 👇 AJOUT INDISPENSABLE POUR L'ONBOARDING
        // Ce champ sera null si l'utilisateur n'a pas encore créé d'entreprise
        Long entrepriseId
) {}