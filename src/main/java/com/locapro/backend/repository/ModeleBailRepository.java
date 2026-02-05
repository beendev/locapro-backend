package com.locapro.backend.repository;

import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.TypeContratBail;
import com.locapro.backend.entity.ModeleBailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModeleBailRepository extends JpaRepository<ModeleBailEntity, Long> {

    // On ne garde QUE celle-ci : La version stricte et sécurisée
    Optional<ModeleBailEntity> findFirstByRegionBailAndTypeContratAndLangueAndActifBoolTrue(
            RegionBail region,
            TypeContratBail typeContrat,
            LangueContrat langue
    );
}
