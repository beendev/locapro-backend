package com.locapro.backend.repository;

import com.locapro.backend.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @EntityGraph(attributePaths = "user") // <-- charge user avec le token
    Optional<RefreshTokenEntity> findByTokenAndRevokedFalse(String token);

    @Modifying
    @Query("update RefreshTokenEntity r set r.revoked = true where r.token = :token")
    int revokeByToken(String token);

    @Modifying
    @Query("update RefreshTokenEntity r set r.revoked = true where r.user.id = :userId and r.revoked = false")
    int revokeAllActiveByUserId(Long userId);


    List<RefreshTokenEntity> findAllByUser_IdAndRevokedFalse(Long userId);
}
