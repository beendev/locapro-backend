package com.locapro.backend.service;


import com.locapro.backend.dto.agence.AgenceInvitationResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;

import java.util.List;

public interface AgenceMembreService {


    ApiMessageResponse inviterGestionnaire(Long agenceId, String emailInvite, Long currentUserId);

    // 👉 nouveau : liste des invitations EN_ATTENTE
    List<AgenceInvitationResponse> listerInvitationsEnAttente(Long agenceId, Long adminUtilisateurId);

    // 👉 nouveau : annuler une invitation
    ApiMessageResponse annulerInvitation(Long invitationId, Long adminUtilisateurId);

    ApiMessageResponse accepterInvitation(Long invitationId, Long utilisateurId);

    ApiMessageResponse refuserInvitation(Long invitationId, Long utilisateurId);

    ApiMessageResponse quitterAgence(Long currentUserId, Long agenceId);
}
