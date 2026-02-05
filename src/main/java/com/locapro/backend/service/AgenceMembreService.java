package com.locapro.backend.service;

import com.locapro.backend.dto.agence.AgenceInvitationResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.agence.AgencyMemberResponse;
import com.locapro.backend.dto.agence.UpdateMemberRoleRequest;

import java.util.List;
import java.util.UUID;

public interface AgenceMembreService {

    ApiMessageResponse inviterGestionnaire(Long agenceId, String emailInvite, Long currentUserId);

    // 👉 nouveau : liste des invitations EN_ATTENTE
    List<AgenceInvitationResponse> listerInvitationsEnAttente(Long agenceId, Long adminUtilisateurId);

    // 👉 nouveau : annuler une invitation
    ApiMessageResponse annulerInvitation(Long invitationId, Long adminUtilisateurId);

    ApiMessageResponse accepterInvitation(Long invitationId, Long utilisateurId);

    ApiMessageResponse refuserInvitation(Long invitationId, Long utilisateurId);

    ApiMessageResponse quitterAgence(Long currentUserId, Long agenceId);

    ApiMessageResponse accepterInvitationParToken(String tokenStr, Long userId);

    // ============================================
    // NOUVELLES MÉTHODES POUR LA GESTION DES MEMBRES
    // ============================================

    /**
     * Récupère la liste des membres actifs d'une agence avec leurs rôles
     */
    List<AgencyMemberResponse> getAgencyMembers(Long agenceId, Long currentUserId);

    /**
     * Met à jour le rôle d'un membre dans l'agence
     */
    ApiMessageResponse updateMemberRole(Long agenceId, Long memberUserId, UpdateMemberRoleRequest request,
            Long currentUserId);

    /**
     * Supprime un membre de l'agence (soft delete)
     */
    ApiMessageResponse removeMemberFromAgency(Long agenceId, Long memberUserId, Long currentUserId);
}