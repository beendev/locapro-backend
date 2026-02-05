package com.locapro.backend.repository;

import com.locapro.backend.entity.LocataireEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface LocataireRepository extends JpaRepository<LocataireEntity, Long> {

    // Pour éviter les doublons : on cherche si l'agent a déjà ce locataire (par email)
    Optional<LocataireEntity> findFirstByEmailAndGestionnaireId(String email, Long gestionnaireId);

    // Ou par Registre National (plus précis pour la Belgique)
    Optional<LocataireEntity> findFirstByRegistreNationalAndGestionnaireId(String rn, Long gestionnaireId);

    Optional<LocataireEntity> findByEmailAndGestionnaireId(String email, Long utilisateurResponsableId);
}