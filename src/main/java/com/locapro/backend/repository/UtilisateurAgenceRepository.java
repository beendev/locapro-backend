package com.locapro.backend.repository;

import com.locapro.backend.entity.UtilisateurAgenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;



import java.util.List;
import java.util.Optional;

public interface UtilisateurAgenceRepository extends JpaRepository<UtilisateurAgenceEntity, Long> {

    boolean existsByUtilisateur_Id(Long utilisateurId);

    Optional<UtilisateurAgenceEntity> findByUtilisateur_Id(Long utilisateurId);

    List<UtilisateurAgenceEntity> findByUtilisateurIdAndEnabledTrue(Long utilisateurId);


    boolean existsByAgence_IdAndUtilisateur_IdAndRoleDansAgence(Long agenceId, Long utilisateurId, String adminAgence);
    boolean existsByAgence_IdAndUtilisateur_IdAndEnabledTrue(Long agenceId, Long utilisateurId);

    Optional<UtilisateurAgenceEntity> findByAgence_IdAndUtilisateur_Id(Long id, Long id1);

    boolean existsByUtilisateurIdAndAgenceIdAndEnabledTrue(Long currentUserId, Long agenceId);
    long countByAgenceIdAndRoleDansAgenceAndEnabledTrue(Long agenceId, String roleDansAgence);

    Optional<UtilisateurAgenceEntity> findByUtilisateurIdAndAgenceIdAndEnabledTrue(Long utilisateurId, Long agenceId);


    Optional<UtilisateurAgenceEntity>  findFirstByUtilisateurIdAndEnabledTrue(Long currentUserId);
}
