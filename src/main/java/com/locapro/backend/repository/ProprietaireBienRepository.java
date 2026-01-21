package com.locapro.backend.repository;

import com.locapro.backend.entity.ProprietaireBienEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProprietaireBienRepository extends JpaRepository<ProprietaireBienEntity, Long> {
    Optional<ProprietaireBienEntity> findFirstByBienIdAndEnabledTrueOrderByIdAsc(Long bienId);

    boolean existsByBienIdAndProprietaireUtilisateurIdAndEnabledTrue(Long bienId, Long currentUserId);

    Optional<ProprietaireBienEntity> findFirstByBienIdAndEnabledTrue(Long bienId);
}
