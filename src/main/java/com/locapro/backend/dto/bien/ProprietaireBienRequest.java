package com.locapro.backend.dto.bien;

import com.locapro.backend.domain.context.ProprietaireType;

public record ProprietaireBienRequest(
        ProprietaireType type,
        String nom,
        String prenom,
        String email,
        String entrepriseNom,
        Long proprietaireUtilisateurId,
        Long proprietaireEntrepriseId
) {}