package com.locapro.backend.repository;

import com.locapro.backend.domain.context.InvitationStatut;
import com.locapro.backend.entity.AgenceInvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgenceInvitationRepository extends JpaRepository<AgenceInvitationEntity, Long> {

    boolean existsByAgence_IdAndEmailInviteIgnoreCaseAndStatut(
            Long agence_id, String emailInvite, InvitationStatut statut
    );

    Optional<AgenceInvitationEntity> findByToken(UUID token);


    // 👉 pour la liste des EN_ATTENTE d’une agence
    List<AgenceInvitationEntity> findByAgence_IdAndStatutOrderByCreatedAtDesc(
            Long agenceId,
            InvitationStatut statut
    );

    // (éventuellement utile plus tard pour d’autres cas)
    Optional<AgenceInvitationEntity> findByIdAndStatut(Long id, InvitationStatut statut);
}
