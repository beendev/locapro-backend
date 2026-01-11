package com.locapro.backend.dto.agence;

import com.locapro.backend.domain.context.InvitationStatut;

import java.time.OffsetDateTime;

public record AgenceInvitationResponse(
        Long id,
        Long agenceId,
        String agenceNom,
        String emailInvite,
        InvitationStatut statut,
        OffsetDateTime createdAt
) { }