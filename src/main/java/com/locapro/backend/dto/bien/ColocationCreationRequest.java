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

        // Propriétaire commun
        BienOwnershipRequest proprietaire
) {}
