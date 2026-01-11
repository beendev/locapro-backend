package com.locapro.backend.dto.bien;

public record BienCreationRequest(

        BienInfosDeBaseRequest bien,
        DetailsSpecifiquesRequest details,
        BienOwnershipRequest proprietaire,
        BienParentInfosRequest parent
) {}