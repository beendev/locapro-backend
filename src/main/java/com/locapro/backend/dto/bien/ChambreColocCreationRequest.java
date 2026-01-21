package com.locapro.backend.dto.bien;

import com.locapro.backend.domain.context.SousType;

public record ChambreColocCreationRequest(

        // Identification
        String nomReferenceInterne,
        String libelleVisible,

        // --- AJOUT 1 : Le type (Kot, Studio, Chambre...) ---
        SousType sousType,

        String lotOuUnite,
        String numeroPorte,
        String boiteUnite,

        // Détails
        DetailsSpecifiquesRequest details,

        // --- AJOUT 2 : Le proprio spécifique (Optionnel) ---
        BienOwnershipRequest proprietaire
) {}
