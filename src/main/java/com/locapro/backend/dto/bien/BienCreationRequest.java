package com.locapro.backend.dto.bien;



public record BienCreationRequest(
        // 👇 NOUVEAU CHAMP : Si rempli, on ignore l'objet 'parent' ci-dessous
        Long existingParentId,

        BienParentInfosRequest parent, // Utilisé seulement si existingParentId est null

        BienInfosDeBaseRequest bien,   // L'unité (toujours requise)
        DetailsSpecifiquesRequest details,
        BienOwnershipRequest proprietaire,
        BienOwnershipRequest proprietaireParent // Ignoré si existingParentId est rempli
) {}