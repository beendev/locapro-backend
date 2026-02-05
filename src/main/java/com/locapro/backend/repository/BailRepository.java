package com.locapro.backend.repository;

import com.locapro.backend.entity.BailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BailRepository extends JpaRepository<BailEntity, Long> {

    // ✅ C'est cette méthode qui manquait !
    // Elle permet de trouver les baux d'un bien qui ont l'un des statuts donnés (ex: ACTIF ou EN_SIGNATURE)
    List<BailEntity> findByBienIdAndStatutIn(Long bienId, List<String> statuts);
}
