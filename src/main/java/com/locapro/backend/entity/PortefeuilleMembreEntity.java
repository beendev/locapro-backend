package com.locapro.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "portefeuille_membres",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_portefeuille_membre",
                        columnNames = {"portefeuille_id", "utilisateur_id"}
                )
        })
public class PortefeuilleMembreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relation vers le portefeuille
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portefeuille_id", nullable = false)
    private PortefeuilleEntity portefeuille;

    // relation vers l'utilisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private UtilisateurEntity utilisateur;

    @Column(name = "role_dans_portefeuille", nullable = false)
    private String roleDansPortefeuille; // "GESTIONNAIRE", etc.

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    // ==== Getters / setters ====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PortefeuilleEntity getPortefeuille() {
        return portefeuille;
    }

    public void setPortefeuille(PortefeuilleEntity portefeuille) {
        this.portefeuille = portefeuille;
    }

    public UtilisateurEntity getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurEntity utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getRoleDansPortefeuille() {
        return roleDansPortefeuille;
    }

    public void setRoleDansPortefeuille(String roleDansPortefeuille) {
        this.roleDansPortefeuille = roleDansPortefeuille;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(OffsetDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
