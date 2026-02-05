package com.locapro.backend.repository;

import com.locapro.backend.entity.PortefeuilleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortefeuilleRepository extends JpaRepository<PortefeuilleEntity, Long> {

    List<PortefeuilleEntity> findByAgenceIdAndEnabledTrue(Long agenceId);
}
