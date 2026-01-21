package com.locapro.backend.entity;

import com.locapro.backend.domain.context.InvitationStatut;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "agences_invitations")
public class AgenceInvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id", nullable = false)
    private AgenceEntity agence;

    @Column(name = "email_invite", nullable = false)
    private String emailInvite;

    @Column(name = "token", nullable = false, unique = true)
    private UUID token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_par_utilisateur_id", nullable = false)
    private UtilisateurEntity invitePar;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private InvitationStatut statut;

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AgenceEntity getAgence() {
        return agence;
    }

    public void setAgence(AgenceEntity agence) {
        this.agence = agence;
    }

    public String getEmailInvite() {
        return emailInvite;
    }

    public void setEmailInvite(String emailInvite) {
        this.emailInvite = emailInvite;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public UtilisateurEntity getInvitePar() {
        return invitePar;
    }

    public void setInvitePar(UtilisateurEntity invitePar) {
        this.invitePar = invitePar;
    }

    public InvitationStatut getStatut() {
        return statut;
    }

    public void setStatut(InvitationStatut statut) {
        this.statut = statut;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(OffsetDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
