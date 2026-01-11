package com.locapro.backend.service;

import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.dto.bail.ModeleBailResponse;
import com.locapro.backend.domain.context.LangueContrat;

public interface ModeleBailService {

    ModeleBailResponse getLatestActiveModel(
            RegionBail regionBail,
            LangueContrat langue,
            String typeDocument
    );
}
