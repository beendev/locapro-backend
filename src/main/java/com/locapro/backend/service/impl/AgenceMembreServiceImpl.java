package com.locapro.backend.service.impl;

import com.locapro.backend.domain.context.InvitationStatut;
import com.locapro.backend.dto.agence.AgenceInvitationResponse;
import com.locapro.backend.dto.agence.AgencyMemberResponse;
import com.locapro.backend.dto.agence.UpdateMemberRoleRequest;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.entity.AgenceEntity;
import com.locapro.backend.entity.AgenceInvitationEntity;
import com.locapro.backend.entity.UtilisateurAgenceEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.exception.BadRequestException;
import com.locapro.backend.exception.ConflictException;
import com.locapro.backend.exception.ForbiddenException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.mapper.AgenceInvitationMapper;
import com.locapro.backend.notification.NotificationPublisher;
import com.locapro.backend.repository.AgenceInvitationRepository;
import com.locapro.backend.repository.AgenceRepository;
import com.locapro.backend.repository.UtilisateurAgenceRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.service.AgenceMembreService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AgenceMembreServiceImpl implements AgenceMembreService {

    private final AgenceRepository agenceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AgenceInvitationRepository agenceInvitationRepository;
    private final UtilisateurAgenceRepository utilisateurAgenceRepository;
    private final NotificationPublisher notificationPublisher;
    private final AgenceInvitationRepository agenceInvitationRepo;
    private final AgenceInvitationMapper mapper;

    public AgenceMembreServiceImpl(AgenceRepository agenceRepository,
            UtilisateurRepository utilisateurRepository,
            AgenceInvitationRepository agenceInvitationRepository,
            UtilisateurAgenceRepository utilisateurAgenceRepository,
            NotificationPublisher notificationPublisher,
            AgenceInvitationMapper mapper) {
        this.agenceRepository = agenceRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.agenceInvitationRepository = agenceInvitationRepository;
        this.utilisateurAgenceRepository = utilisateurAgenceRepository;
        this.notificationPublisher = notificationPublisher;
        this.agenceInvitationRepo = agenceInvitationRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ApiMessageResponse inviterGestionnaire(Long agenceId,
            String emailInvite,
            Long adminUtilisateurId) {

        String emailNormalise = normalizeEmail(emailInvite);

        // 1) Vérifier que l'utilisateur courant est bien ADMIN_AGENCE pour cette agence
        verifierDroitsAdminAgence(agenceId, adminUtilisateurId);

        // 2) Charger l'agence et l'admin
        AgenceEntity agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new NotFoundException("Agence introuvable"));

        UtilisateurEntity admin = utilisateurRepository.findById(adminUtilisateurId)
                .orElseThrow(() -> new NotFoundException("Utilisateur administrateur introuvable"));

        // 1 bis) Vérifier si un utilisateur existe déjà avec cet e-mail
        utilisateurRepository.findByEmailIgnoreCase(emailNormalise)
                .ifPresent(utilisateur -> {
                    boolean dejaMembreActif = utilisateurAgenceRepository
                            .existsByAgence_IdAndUtilisateur_IdAndEnabledTrue(
                                    agenceId, utilisateur.getId());
                    if (dejaMembreActif) {
                        throw new ConflictException(
                                "Cet utilisateur fait déjà partie de cette agence.");
                    }
                });

        // 3) Éviter les doublons d'invitation EN_ATTENTE
        boolean dejaInvite = agenceInvitationRepository
                .existsByAgence_IdAndEmailInviteIgnoreCaseAndStatut(
                        agenceId,
                        emailNormalise,
                        InvitationStatut.EN_ATTENTE);
        if (dejaInvite) {
            throw new ConflictException("Une invitation est déjà en attente pour cet e-mail.");
        }

        // 4) Créer l'entité d'invitation
        AgenceInvitationEntity invitation = new AgenceInvitationEntity();
        invitation.setAgence(agence);
        invitation.setEmailInvite(emailNormalise);
        invitation.setToken(UUID.randomUUID());
        invitation.setInvitePar(admin);// ici on passe bien l'entity
        invitation.setStatut(InvitationStatut.EN_ATTENTE);

        AgenceInvitationEntity saved = agenceInvitationRepository.save(invitation);

        // 5) Publier l'événement (V1 : envoi du mail)
        notificationPublisher.publishAgenceInvitationCreated(saved);

        // 6) Réponse API simple et propre
        String message = "Invitation envoyée à " + emailNormalise
                + " pour l'agence " + agence.getNom() + ".";
        return new ApiMessageResponse(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgenceInvitationResponse> listerInvitationsEnAttente(Long agenceId, Long adminUtilisateurId) {
        // sécurité : seulement un ADMIN_AGENCE de cette agence
        verifierDroitsAdminAgence(agenceId, adminUtilisateurId);

        var invitations = agenceInvitationRepository
                .findByAgence_IdAndStatutOrderByCreatedAtDesc(agenceId, InvitationStatut.EN_ATTENTE);

        return mapper.toResponseList(invitations);
    }

    @Override
    @Transactional
    public ApiMessageResponse annulerInvitation(Long invitationId, Long adminUtilisateurId) {

        AgenceInvitationEntity invitation = agenceInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation introuvable"));

        Long agenceId = invitation.getAgence().getId();

        // sécurité : l’utilisateur doit être ADMIN_AGENCE de l’agence liée à cette
        // invitation
        verifierDroitsAdminAgence(agenceId, adminUtilisateurId);

        if (invitation.getStatut() != InvitationStatut.EN_ATTENTE) {
            throw new ConflictException("Seules les invitations en attente peuvent être annulées.");
        }

        invitation.setStatut(InvitationStatut.ANNULEE);
        agenceInvitationRepository.save(invitation);

        // plus tard : on pourra publier un événement "invitation annulée" si besoin
        String msg = "Invitation " + invitationId + " annulée pour l'agence " + invitation.getAgence().getNom() + ".";
        return new ApiMessageResponse(msg);
    }

    @Override
    @Transactional
    public ApiMessageResponse accepterInvitation(Long invitationId, Long utilisateurId) {

        // 1) Charger l’invitation
        AgenceInvitationEntity invitation = agenceInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation introuvable"));

        // 2) Vérifier le statut
        if (invitation.getStatut() != InvitationStatut.EN_ATTENTE) {
            throw new ConflictException(
                    "Cette invitation a déjà été traitée (" + invitation.getStatut() + ").");
        }

        // 3) Vérifier que l’utilisateur connecté est le destinataire
        UtilisateurEntity user = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        String emailUser = normalizeEmail(user.getEmail());
        String emailInvite = normalizeEmail(invitation.getEmailInvite());

        if (!emailUser.equals(emailInvite)) {
            // Important : impossible d’accepter l’invitation de quelqu’un d’autre
            throw new ForbiddenException("Cette invitation ne vous est pas adressée.");
        }

        // 4) Rattacher l’utilisateur à l’agence (GESTIONNAIRE), en évitant les doublons
        UtilisateurAgenceEntity lien = utilisateurAgenceRepository
                .findByAgence_IdAndUtilisateur_Id(invitation.getAgence().getId(), user.getId())
                .orElseGet(() -> {
                    UtilisateurAgenceEntity ua = new UtilisateurAgenceEntity();
                    ua.setAgence(invitation.getAgence());
                    ua.setUtilisateur(user);
                    return ua;
                });

        lien.setRoleDansAgence("GESTIONNAIRE"); // pour l’instant rôle unique
        lien.setEnabled(true); // l’utilisateur devient actif dans l’agence
        utilisateurAgenceRepository.save(lien);

        // 5) Marquer l’invitation comme acceptée
        invitation.setStatut(InvitationStatut.ACCEPTEE);
        agenceInvitationRepository.save(invitation);

        // (Optionnel plus tard :
        // notificationPublisher.publishAgenceInvitationAccepted(invitation);)

        return new ApiMessageResponse(
                "Invitation acceptée. Vous faites maintenant partie de l'agence " + invitation.getAgence().getNom()
                        + ".");
    }

    @Override
    @Transactional
    public ApiMessageResponse refuserInvitation(Long invitationId, Long utilisateurId) {

        // 1) Charger l’invitation
        AgenceInvitationEntity invitation = agenceInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new NotFoundException("Invitation introuvable"));

        // 2) Vérifier le statut
        if (invitation.getStatut() != InvitationStatut.EN_ATTENTE) {
            throw new ConflictException(
                    "Cette invitation a déjà été traitée (" + invitation.getStatut() + ").");
        }

        // 3) Vérifier que l’utilisateur connecté est bien le destinataire

        UtilisateurEntity user = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        String emailUser = normalizeEmail(user.getEmail());
        String emailInvite = normalizeEmail(invitation.getEmailInvite());
        System.out.println(" emailUser: " + emailUser + " / emailInvite: " + emailInvite);
        if (emailUser.equals(emailInvite)) {
            throw new ForbiddenException("Cette invitation ne vous est pas adressée.");
        }

        // 4) Changer le statut → REFUSEE
        invitation.setStatut(InvitationStatut.REFUSEE);
        agenceInvitationRepository.save(invitation);

        // (Optionnel plus tard :
        // notificationPublisher.publishAgenceInvitationRefused(invitation);)

        return new ApiMessageResponse(
                "Invitation refusée pour l'agence " + invitation.getAgence().getNom() + ".");
    }

    @Override
    @Transactional
    public ApiMessageResponse quitterAgence(Long currentUserId, Long agenceId) {

        // On cherche le lien actif user ↔ agence
        var lien = utilisateurAgenceRepository
                .findByUtilisateurIdAndAgenceIdAndEnabledTrue(currentUserId, agenceId)
                .orElseThrow(() -> new NotFoundException("Vous n'êtes pas membre de cette agence."));

        // Option : empêcher le dernier admin de sortir
        if ("ADMIN".equalsIgnoreCase(lien.getRoleDansAgence())) {
            long nbAdmins = utilisateurAgenceRepository
                    .countByAgenceIdAndRoleDansAgenceAndEnabledTrue(agenceId, "ADMIN");

            if (nbAdmins <= 1) {
                throw new ForbiddenException(
                        "Vous êtes le dernier administrateur de cette agence, vous ne pouvez pas la quitter.");
            }
        }

        // Soft delete : on désactive le lien
        lien.setEnabled(false);
        utilisateurAgenceRepository.save(lien);

        return new ApiMessageResponse("Vous avez quitté l'agence avec succès.");
    }

    @Override
    public ApiMessageResponse accepterInvitationParToken(String tokenStr, Long userId) {

        // 1. Validation du format (Logique déplacée du Controller vers le Service)
        if (tokenStr == null || tokenStr.isBlank()) {
            throw new BadRequestException("Le token est manquant.");
        }

        UUID token;
        try {
            token = UUID.fromString(tokenStr);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Le format du token d'invitation est invalide.");
        }
        // 1. Trouver l'invitation via le Token
        AgenceInvitationEntity invitation = agenceInvitationRepo.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Invitation introuvable ou lien expiré"));

        // 2. Vérifier si elle est toujours valide
        if (invitation.getStatut() != InvitationStatut.EN_ATTENTE) { // Assure-toi d'avoir cet Enum ou String
            throw new ConflictException("Cette invitation n'est plus valide (déjà acceptée ou refusée).");
        }

        // 3. Vérifier que l'utilisateur n'est pas déjà dans l'agence
        boolean dejaMembre = utilisateurAgenceRepository.existsByUtilisateur_IdAndAgence_Id(userId,
                invitation.getAgence().getId());
        if (dejaMembre) {
            // On peut soit lancer une erreur, soit juste fermer l'invitation gentiment
            invitation.setStatut(InvitationStatut.ACCEPTEE);
            invitation.setRespondedAt(OffsetDateTime.now());
            agenceInvitationRepo.save(invitation);

        }

        // 4. Récupérer l'utilisateur
        var user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        // 5. CRÉER LE LIEN (Ajouter le membre)
        UtilisateurAgenceEntity lien = new UtilisateurAgenceEntity();
        lien.setUtilisateur(user);
        lien.setAgence(invitation.getAgence());
        lien.setRoleDansAgence("GESTIONNAIRE"); // Rôle par défaut via invitation
        lien.setEnabled(true);

        utilisateurAgenceRepository.save(lien);

        // 6. Mettre à jour l'invitation
        invitation.setStatut(InvitationStatut.ACCEPTEE);
        invitation.setRespondedAt(OffsetDateTime.now());
        agenceInvitationRepo.save(invitation);

        return new ApiMessageResponse("Vous avez rejoint l'agence.");
    }

    // ------------------ helpers ------------------

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void verifierDroitsAdminAgence(Long agenceId, Long utilisateurId) {
        // Vérifier si l'utilisateur est ADMIN_AGENCE ou RESPONSABLE_AGENCE (ou l'ancien RESPONSABLE)
        var lien = utilisateurAgenceRepository
                .findByUtilisateurIdAndAgenceIdAndEnabledTrue(utilisateurId, agenceId)
                .orElseThrow(() -> new ForbiddenException("Vous n'êtes pas membre de cette agence."));

        String role = lien.getRoleDansAgence();
        boolean isAdmin = "ADMIN_AGENCE".equalsIgnoreCase(role)
                || "RESPONSABLE_AGENCE".equalsIgnoreCase(role)
                || "RESPONSABLE".equalsIgnoreCase(role);

        if (!isAdmin) {
            throw new ForbiddenException("Vous devez être administrateur pour effectuer cette action.");
        }
    }

    private com.locapro.backend.domain.context.Role mapDatabaseRoleToEnum(String roleDb) {
        if (roleDb == null) {
            return com.locapro.backend.domain.context.Role.GESTIONNAIRE;
        }
        return switch (roleDb.toUpperCase()) {
            case "ADMIN_AGENCE" -> com.locapro.backend.domain.context.Role.ADMIN_AGENCE;
            case "RESPONSABLE", "RESPONSABLE_AGENCE" -> com.locapro.backend.domain.context.Role.RESPONSABLE_AGENCE;
            case "GESTIONNAIRE_SENIOR" -> com.locapro.backend.domain.context.Role.GESTIONNAIRE_SENIOR;
            default -> com.locapro.backend.domain.context.Role.GESTIONNAIRE;
        };
    }

    // ============================================
    // IMPLÉMENTATION DES NOUVELLES MÉTHODES
    // ============================================

    @Override
    @Transactional(readOnly = true)
    public List<AgencyMemberResponse> getAgencyMembers(Long agenceId, Long currentUserId) {
        // Vérifier que l'utilisateur est ADMIN_AGENCE ou RESPONSABLE_AGENCE
        verifierDroitsAdminAgence(agenceId, currentUserId);

        // Récupérer tous les membres actifs de l'agence
        List<UtilisateurAgenceEntity> membres = utilisateurAgenceRepository
                .findByAgenceIdAndEnabledTrue(agenceId);

        return membres.stream()
                .map(membre -> {
                    UtilisateurEntity user = membre.getUtilisateur();
                    // Mapper le rôle depuis la DB
                    var role = mapDatabaseRoleToEnum(membre.getRoleDansAgence());
                    return new AgencyMemberResponse(
                            user.getId(),
                            user.getEmail(),
                            user.getNom(),
                            user.getPrenom(),
                            role,
                            membre.isEnabled());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    @Transactional
    public ApiMessageResponse updateMemberRole(Long agenceId, Long memberUserId, UpdateMemberRoleRequest request,
            Long currentUserId) {
        // 1. Vérifier que l'utilisateur courant est ADMIN_AGENCE
        verifierDroitsAdminAgence(agenceId, currentUserId);

        // 2. Vérifier qu'on ne modifie pas son propre rôle (sécurité)
        if (memberUserId.equals(currentUserId)) {
            throw new ForbiddenException("Vous ne pouvez pas modifier votre propre rôle.");
        }

        // 3. Trouver le lien utilisateur-agence
        UtilisateurAgenceEntity lien = utilisateurAgenceRepository
                .findByUtilisateurIdAndAgenceIdAndEnabledTrue(memberUserId, agenceId)
                .orElseThrow(() -> new NotFoundException("Membre non trouvé dans cette agence"));

        // 4. Vérifier les règles métier (ex: pas plus d'1 ADMIN_AGENCE, etc.)
        if (request.getRole() == com.locapro.backend.domain.context.Role.ADMIN_AGENCE) {
            long nbAdmins = utilisateurAgenceRepository
                    .countByAgenceIdAndRoleDansAgenceAndEnabledTrue(agenceId, "ADMIN_AGENCE");
            // On pourrait limiter le nombre d'admins si nécessaire
        }

        // 5. Mettre à jour le rôle
        String nouveauRole = request.getRole().name();
        lien.setRoleDansAgence(nouveauRole);
        utilisateurAgenceRepository.save(lien);

        return new ApiMessageResponse(
                "Rôle mis à jour avec succès : " + request.getRole().getLabel());
    }

    @Override
    @Transactional
    public ApiMessageResponse removeMemberFromAgency(Long agenceId, Long memberUserId, Long currentUserId) {
        // 1. Vérifier que l'utilisateur courant est ADMIN_AGENCE
        verifierDroitsAdminAgence(agenceId, currentUserId);

        // 2. Vérifier qu'on ne se supprime pas soi-même
        if (memberUserId.equals(currentUserId)) {
            throw new ForbiddenException("Vous ne pouvez pas vous retirer vous-même de l'agence.");
        }

        // 3. Trouver le lien utilisateur-agence
        UtilisateurAgenceEntity lien = utilisateurAgenceRepository
                .findByUtilisateurIdAndAgenceIdAndEnabledTrue(memberUserId, agenceId)
                .orElseThrow(() -> new NotFoundException("Membre non trouvé dans cette agence"));

        // 4. Vérifier si c'est le dernier admin
        if ("ADMIN_AGENCE".equalsIgnoreCase(lien.getRoleDansAgence())) {
            long nbAdmins = utilisateurAgenceRepository
                    .countByAgenceIdAndRoleDansAgenceAndEnabledTrue(agenceId, "ADMIN_AGENCE");

            if (nbAdmins <= 1) {
                throw new ForbiddenException(
                        "Vous ne pouvez pas supprimer le dernier administrateur de l'agence.");
            }
        }

        // 5. Soft delete : désactiver le lien
        lien.setEnabled(false);
        utilisateurAgenceRepository.save(lien);

        return new ApiMessageResponse("Membre retiré de l'agence avec succès.");
    }

}