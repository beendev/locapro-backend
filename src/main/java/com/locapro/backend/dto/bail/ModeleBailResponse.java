package com.locapro.backend.dto.bail;

import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.LangueContrat;

public record ModeleBailResponse(
        Long id,
        RegionBail regionBail,
        LangueContrat langue,
        String version,
        String typeDocument,
        String urlFichier
) {}
