package com.locapro.backend.repository;

import com.locapro.backend.entity.BienEntity;
import com.locapro.backend.entity.ProprietaireBienEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BienRepository extends JpaRepository<BienEntity, Long> {



    // 1) Biens d’une agence
    @Query("""
        select b
        from BienEntity b
        where b.enabled = true
          and b.agenceId = :agenceId
    """)
    List<BienEntity> findAllByAgenceId(Long agenceId);

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

    List<BienEntity> findByAgenceIdAndEnabledTrueAndEstUniteLocativeTrue(Long agenceId);



}
