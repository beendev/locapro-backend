package com.locapro.backend.repository;

import com.locapro.backend.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {

    Optional<EmailVerificationTokenEntity> findByToken(UUID token);

    void deleteByUtilisateurId(Long utilisateurId);

    // 👇 LA MÉTHODE MAGIQUE
    // Elle exécute une requête SQL directe.
    // Si les tokens ont déjà été supprimés par une autre requête, elle ne plante pas.
    @Modifying
    @Query("DELETE FROM EmailVerificationTokenEntity t WHERE t.utilisateurId = :userId")
    void deleteAllTokensByUser(Long userId);

}
