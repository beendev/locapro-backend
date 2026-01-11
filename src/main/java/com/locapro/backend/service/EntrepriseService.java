package com.locapro.backend.service;

import com.locapro.backend.dto.agence.EntrepriseRequest;
import com.locapro.backend.dto.agence.EntrepriseResponse;

public interface EntrepriseService {
    EntrepriseResponse createNewVersion(Long ownerUserId, EntrepriseRequest req);
    EntrepriseResponse updateCurrent(Long ownerUserId, EntrepriseRequest req);
    EntrepriseResponse getCurrent(Long ownerUserId);
}
