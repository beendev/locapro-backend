// com.locapro.backend.repository.PasswordResetTokenRepository
package com.locapro.backend.repository;

import com.locapro.backend.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByTokenAndUsedFalse(UUID token);
    void deleteByUtilisateur_Id(Long userId); // utile si tu veux nettoyer avant d’en créer un nouveau
}
