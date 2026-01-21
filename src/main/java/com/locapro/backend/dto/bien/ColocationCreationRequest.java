package com.locapro.backend.dto.bien;

import java.util.List;

public record ColocationCreationRequest(

        // Niveau 1 : parent (immeuble / maison)
        BienParentInfosRequest parent,

        // Niveau 2 : appart global (non locatif)
        BienInfosDeBaseRequest appartement,
        DetailsSpecifiquesRequest detailsAppartement,

        // Niveau 3 : chambres locatives
        List<ChambreColocCreationRequest> chambres,
// Le proprio de l'unité locative
        BienOwnershipRequest proprietaire,

        // Le proprio de l'immeuble (Optionnel, sinon on prend le même ou null)
        BienOwnershipRequest proprietaireParent
) {}
