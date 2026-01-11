package com.locapro.backend.repository;

import com.locapro.backend.entity.PeriodeBailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeriodeBailRepository extends JpaRepository<PeriodeBailEntity, Long> {

    // Si besoin plus tard : récupérer uniquement les périodes actives d’un bail
    // List<PeriodeBailEntity> findByBailIdAndEnabledTrue(Long bailId);
}
