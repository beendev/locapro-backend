package com.locapro.backend.dto.user;

import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(

        @Size(max = 100)
        String prenom,

        @Size(max = 100)
        String nom,

        String telephone,

        // Adresse de domicile
        String rue,
        String numero,
        String boite,
        String codePostal,
        String ville,
        String commune,
        String pays,


        // Coordonnées GPS (si tu gères la géolocalisation de l'user)
        Double latitude,
        Double longitude
) {}