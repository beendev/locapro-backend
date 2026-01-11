package com.locapro.backend.repository;

import com.locapro.backend.entity.UtilisateurEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<UtilisateurEntity, Long> {

    // ---- Email (inscription, login)
    boolean existsByEmail(String email);

    Optional<UtilisateurEntity> findByEmailIgnoreCase(String email);

    // ---- IPI (facultatif mais unique si renseigné)
    boolean existsByNumeroIpi(String numeroIpi);
    Optional<UtilisateurEntity> findByNumeroIpi(String numeroIpi);



}
