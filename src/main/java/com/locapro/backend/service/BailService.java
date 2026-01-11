package com.locapro.backend.service;

import com.locapro.backend.dto.bail.BailResponse;
import com.locapro.backend.dto.bail.CreateBailRequest;



public interface BailService {

    BailResponse creerBail(Long currentUser, CreateBailRequest request);
}
