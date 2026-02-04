package com.locapro.backend.repository;

import com.locapro.backend.entity.BienEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface BienRepository extends JpaRepository<BienEntity, Long> {





    // 2) Biens perso d'un utilisateur (propriétaire) - seulement les unités locatives
    @Query("""
        select distinct b
        from BienEntity b
          join ProprietaireBienEntity pb on pb.bienId = b.id
        where b.enabled = true
          and b.estUniteLocative = true
          and (pb.enabled = true or pb.enabled is null)
          and pb.proprietaireType = 'PERSONNE'
          and pb.proprietaireUtilisateurId = :userId
        order by b.creeLe desc
    """)
    List<BienEntity> findAllByProprietaireUtilisateur(Long userId);

    // Biens perso - bâtiments parents uniquement
    @Query("""
        select distinct b
        from BienEntity b
          join ProprietaireBienEntity pb on pb.bienId = b.id
        where b.enabled = true
          and b.estUniteLocative = false
          and (pb.enabled = true or pb.enabled is null)
          and pb.proprietaireType = 'PERSONNE'
          and pb.proprietaireUtilisateurId = :userId
        order by b.creeLe desc
    """)
    List<BienEntity> findBuildingsByProprietaireUtilisateur(Long userId);

    List<BienEntity> findByAgenceIdAndEnabledTrue(Long agenceId);

    // Pour la liste: seulement les unités locatives (triées par date de création DESC)
    List<BienEntity> findByAgenceIdAndEnabledTrueAndEstUniteLocativeTrueOrderByCreeLeDesc(Long agenceId);

    // Pour la liste: seulement les bâtiments parents (triées par date de création DESC)
    List<BienEntity> findByAgenceIdAndEnabledTrueAndEstUniteLocativeFalseOrderByCreeLeDesc(Long agenceId);

    // 1. Pour afficher le contenu d'un portefeuille
    List<BienEntity> findByPortefeuilleIdAndEnabledTrue(Long portefeuilleId);

    // 2. Pour la modale d'ajout (Les biens de l'agence qui n'ont PAS de portefeuille)
    List<BienEntity> findByAgenceIdAndPortefeuilleIdIsNullAndEnabledTrueAndEstUniteLocativeTrue(Long agenceId);

    // 3. Pour récupérer les enfants d'un bien (sous-biens)
    List<BienEntity> findByParentBienIdAndEnabledTrue(Long parentBienId);

    // 4. Pour la génération du nomReference : compter les biens créés cette année par type
    @Query("""
        SELECT COUNT(b) FROM BienEntity b
        WHERE b.typeBien = :typeBien
        AND YEAR(b.creeLe) = :annee
    """)
    Long countByTypeBienAndYear(String typeBien, int annee);

    // 5. RBAC : Vérifier si un bien appartient à un des portefeuilles donnés
    @Query("""
        SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
        FROM BienEntity b
        WHERE b.id = :bienId
        AND b.portefeuilleId IN :portfolioIds
        AND b.enabled = true
    """)
    boolean existsByIdAndPortefeuilleIdIn(Long bienId, List<Long> portfolioIds);
}
