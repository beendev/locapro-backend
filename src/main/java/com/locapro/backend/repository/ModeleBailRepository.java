package com.locapro.backend.repository;

import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.TypeContratBail;
import com.locapro.backend.entity.ModeleBailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModeleBailRepository extends JpaRepository<ModeleBailEntity, Long> {

    // LA méthode critique pour ton générateur
    Optional<ModeleBailEntity> findFirstByRegionBailAndTypeContratAndLangueAndActifBoolTrue(
            RegionBail regionBail,
            TypeContratBail typeContrat,
            LangueContrat langue
    );
}