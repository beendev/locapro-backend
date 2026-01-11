package com.locapro.backend.dto.bien;

public record ProprietairePersonneRequest(
        Long utilisateurId,  // si propriétaire existe déjà comme utilisateur
        String nom,
        String prenom,
        String email
) {}
