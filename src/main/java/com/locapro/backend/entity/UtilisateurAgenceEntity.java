package com.locapro.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "utilisateurs_agences")
public class UtilisateurAgenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private UtilisateurEntity utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id", nullable = false)
    private AgenceEntity agence;

    @Column(name = "role_dans_agence", nullable = false)
    private String roleDansAgence;


    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UtilisateurEntity getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurEntity utilisateur) {
        this.utilisateur = utilisateur;
    }

    public AgenceEntity getAgence() {
        return agence;
    }

    public void setAgence(AgenceEntity agence) {
        this.agence = agence;
    }

    public String getRoleDansAgence() {
        return roleDansAgence;
    }

    public void setRoleDansAgence(String roleDansAgence) {
        this.roleDansAgence = roleDansAgence;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
