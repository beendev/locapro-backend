package com.locapro.backend.repository;

import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.entity.ModeleBailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModeleBailRepository extends JpaRepository<ModeleBailEntity, Long> {

    // Récupère le modèle de bail actif le plus récent
    // pour une région donnée, une langue donnée, et un type de document donné.
    Optional<ModeleBailEntity> findFirstByRegionBailAndLangueAndTypeDocumentAndActifBoolIsTrueOrderByCreeLeDesc(
            RegionBail regionBail,
            LangueContrat langue,
            String typeDocument
    );
}
