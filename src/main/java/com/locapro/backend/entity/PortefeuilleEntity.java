package com.locapro.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "portefeuilles")
public class PortefeuilleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "code", nullable = false)
    private String code;

    // Dans la DB c'est un enum "portefeuille_type" → on mappe en String
    @Column(name = "type", nullable = false)
    private String type; // ex: "AGENCE", "PERSO", ...

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id")
    private AgenceEntity agence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private UtilisateurEntity utilisateur;

    @Column(name = "actif_bool", nullable = false)
    private boolean actifBool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UtilisateurEntity createdBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "type_gestion")
    private String typeGestion; // "AGENCE", "PERSO", "AUTRE"

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    // ==== Getters / setters ====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AgenceEntity getAgence() {
        return agence;
    }

    public void setAgence(AgenceEntity agence) {
        this.agence = agence;
    }

    public UtilisateurEntity getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(UtilisateurEntity utilisateur) {
        this.utilisateur = utilisateur;
    }

    public boolean isActifBool() {
        return actifBool;
    }

    public void setActifBool(boolean actifBool) {
        this.actifBool = actifBool;
    }

    public UtilisateurEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UtilisateurEntity createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getTypeGestion() {
        return typeGestion;
    }

    public void setTypeGestion(String typeGestion) {
        this.typeGestion = typeGestion;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
