package com.locapro.backend.repository;

import com.locapro.backend.entity.PortefeuilleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortefeuilleRepository extends JpaRepository<PortefeuilleEntity, Long> {
    // Pour l’instant, rien de spécial
}
