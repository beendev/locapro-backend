package com.locapro.backend.service;

import com.locapro.backend.dto.bail.BailResponse;
import com.locapro.backend.dto.bail.CreateBailRequest;
import com.locapro.backend.dto.bail.UpdateBailRequest;


public interface BailService {

    BailResponse creerBail(Long currentUser, CreateBailRequest request);

    BailResponse updateBail(Long bailId, UpdateBailRequest request);
    BailResponse getBail(Long bailId);
    void deleteBail(Long bailId); // La porte de sortie
}
