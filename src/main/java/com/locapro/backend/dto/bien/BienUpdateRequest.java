package com.locapro.backend.dto.bien;

public record BienUpdateRequest(
        BienInfosDeBaseRequest bien,
        DetailsSpecifiquesRequest details,
        BienOwnershipRequest proprietaire,
        BienParentInfosRequest parent
) {}
