package com.locapro.backend.service.impl;

import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.dto.bail.ModeleBailResponse;
import com.locapro.backend.entity.ModeleBailEntity;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.repository.ModeleBailRepository;
import com.locapro.backend.service.ModeleBailService;
import org.springframework.stereotype.Service;

@Service
public class ModeleBailServiceImpl implements ModeleBailService {

    private final ModeleBailRepository repository;

    public ModeleBailServiceImpl(ModeleBailRepository repository) {
        this.repository = repository;
    }

    @Override
    public ModeleBailResponse getLatestActiveModel(RegionBail regionBail, LangueContrat langue, String typeDocument) {

        ModeleBailEntity entity = repository
                .findFirstByRegionBailAndLangueAndTypeDocumentAndActifBoolIsTrueOrderByCreeLeDesc(
                        regionBail, langue, typeDocument
                )
                .orElseThrow(() ->
                        new NotFoundException("Aucun modèle actif pour " + regionBail + " / " + langue)
                );

        return new ModeleBailResponse(
                entity.getId(),
                entity.getRegion(),
                entity.getLangue(),
                entity.getVersion(),
                entity.getTypeDocument(),
                entity.getUrlFichier()
        );
    }
}
