package com.locapro.backend.dto.bail;

public record BailConflitResponse(
        boolean conflit,           // true si un bail existe déjà
        BailResponse bailExistant, // Le bail qui pose problème (pour afficher les infos)
        String message             // Le message d'alerte pour l'utilisateur
) {}