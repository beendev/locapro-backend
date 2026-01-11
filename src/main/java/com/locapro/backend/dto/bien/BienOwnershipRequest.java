package com.locapro.backend.dto.bien;

import com.locapro.backend.domain.context.BienOwnershipMode;

public record BienOwnershipRequest(
        BienOwnershipMode mode,
        ProprietairePersonneRequest personne,
        ProprietaireEntrepriseRequest entreprise
) {}
