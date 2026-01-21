// src/main/java/com/locapro/backend/repository/EntrepriseRepository.java
package com.locapro.backend.repository;

import com.locapro.backend.entity.EntrepriseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EntrepriseRepository extends JpaRepository<EntrepriseEntity, Long> {

    // l’actuelle (active) pour un user
    Optional<EntrepriseEntity> findByProprietaire_IdAndEnabledTrue(Long ownerUserId);

    @Modifying
    @Query("update EntrepriseEntity e set e.enabled = false where e.proprietaire.id = :ownerUserId and e.enabled = true")
    void disableCurrentForUser(Long ownerUserId);

    @Query("SELECT e.id FROM EntrepriseEntity e WHERE e.proprietaire.id = :userId AND e.enabled = true")
    Optional<Long> findIdByUserIdAndEnabledTrue(Long userId);
}

