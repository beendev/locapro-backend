package com.locapro.backend.repository;

import com.locapro.backend.entity.BailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BailRepository extends JpaRepository<BailEntity, Long> {

    // Pour récupérer le bail d'un utilisateur/responsable si besoin plus tard
    // Optional<BailEntity> findByIdAndUtilisateurResponsableId(Long id, Long userId);

}
