package com.locapro.backend.service;

import com.locapro.backend.dto.agence.AgenceRequest;
import com.locapro.backend.dto.agence.AgenceResponse;

public interface AgenceService {

    AgenceResponse create(Long userId, AgenceRequest req);

    AgenceResponse getCurrent(Long userId);
}
