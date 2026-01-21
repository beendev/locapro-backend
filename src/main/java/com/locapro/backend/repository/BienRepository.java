package com.locapro.backend.repository;

import com.locapro.backend.entity.BienEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface BienRepository extends JpaRepository<BienEntity, Long> {





    // 2) Biens perso d’un utilisateur (propriétaire)
    @Query("""
        select distinct b
        from BienEntity b
          join ProprietaireBienEntity pb on pb.bienId = b.id
        where b.enabled = true
          and (pb.enabled = true or pb.enabled is null)
          and pb.proprietaireType = 'PERSONNE'
          and pb.proprietaireUtilisateurId = :userId
    """)
    List<BienEntity> findAllByProprietaireUtilisateur(Long userId);

    List<BienEntity> findByAgenceIdAndEnabledTrue(Long agenceId);

    // 1. Pour afficher le contenu d'un portefeuille
    List<BienEntity> findByPortefeuilleIdAndEnabledTrue(Long portefeuilleId);

    // 2. Pour la modale d'ajout (Les biens de l'agence qui n'ont PAS de portefeuille)
    List<BienEntity> findByAgenceIdAndPortefeuilleIdIsNullAndEnabledTrueAndEstUniteLocativeTrue(Long agenceId);



}
