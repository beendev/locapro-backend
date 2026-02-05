package com.locapro.backend.service;

import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.TypeContratBail;
import com.locapro.backend.dto.bail.ModeleBailResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ModeleBailService {
    // Ta méthode existante pour lister
    List<ModeleBailResponse> getAllModeles();

    // LA NOUVELLE MÉTHODE D'UPLOAD
    ModeleBailResponse uploadModele(
            MultipartFile fichier,
            RegionBail region,
            TypeContratBail typeContrat,
            LangueContrat langue,
            String version
    ) throws IOException;
}