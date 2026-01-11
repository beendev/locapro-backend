package com.locapro.backend.repository;

import com.locapro.backend.entity.PortefeuilleMembreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortefeuilleMembreRepository extends JpaRepository<PortefeuilleMembreEntity, Long> {

    List<PortefeuilleMembreEntity> findByPortefeuilleIdAndEnabledTrue(Long portefeuilleId);

    List<PortefeuilleMembreEntity> findByUtilisateurIdAndEnabledTrue(Long utilisateurId);

    boolean existsByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(Long portefeuilleId, Long utilisateurId);

    Optional<PortefeuilleMembreEntity> findByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(
            Long portefeuilleId,
            Long utilisateurId
    );


    Optional<PortefeuilleMembreEntity>  findByPortefeuilleIdAndUtilisateurId(Long portefeuilleId, Long utilisateurId);
}
