package com.locapro.backend.dto.bien;

public record ProprietaireBienResponse(
        String type,            // "PERSONNE" ou "ENTREPRISE"
        String nom,
        String prenom,
        String email,
        String entrepriseNom
) {}