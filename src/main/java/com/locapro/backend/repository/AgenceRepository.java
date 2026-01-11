package com.locapro.backend.repository;

import com.locapro.backend.entity.AgenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgenceRepository extends JpaRepository<AgenceEntity, Long> {

    Optional<AgenceEntity> findByEntreprise_IdAndEnabledTrue(Long entrepriseId);


}
