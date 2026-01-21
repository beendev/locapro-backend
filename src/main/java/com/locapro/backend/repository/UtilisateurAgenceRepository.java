package com.locapro.backend.repository;

import com.locapro.backend.entity.UtilisateurAgenceEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


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

    boolean existsByUtilisateur_IdAndAgence_Id(Long userId, Long id);

    @Query("SELECT ua.utilisateur FROM UtilisateurAgenceEntity ua " +
            "WHERE ua.agence.id = :agenceId " +
            "AND ua.enabled = true " +
            "AND (LOWER(ua.utilisateur.nom) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(ua.utilisateur.prenom) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(ua.utilisateur.email) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<UtilisateurEntity> searchMembresAgence(Long agenceId, String query);
}
