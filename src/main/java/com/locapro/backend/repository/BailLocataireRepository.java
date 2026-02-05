package com.locapro.backend.repository;

import com.locapro.backend.entity.BailLocataireEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BailLocataireRepository extends JpaRepository<BailLocataireEntity, Long> {

    // Trouver tous les liens pour un bail donné
    List<BailLocataireEntity> findByBailId(Long bailId);

    // Supprimer les liens quand on supprime un bail (nettoyage)
    void deleteByBailId(Long bailId);
}