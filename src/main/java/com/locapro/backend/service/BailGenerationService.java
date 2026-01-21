package com.locapro.backend.service;

public interface BailGenerationService {
    // Une seule méthode simple pour le Controller
    byte[] genererBailPourId(Long bailId) throws Exception;
}