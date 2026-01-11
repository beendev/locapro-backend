package com.locapro.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "proprietaires_biens")
public class ProprietaireBienEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bien_id", nullable = false)
    private Long bienId;

    @Column(name = "proprietaire_utilisateur_id")
    private Long proprietaireUtilisateurId;

    @Column(name = "proprietaire_entreprise_id")
    private Long proprietaireEntrepriseId;

    @Column(name = "quote_part")
    private Double quotePart;

    @Column(name = "enabled")
    private Boolean enabled = true;

    // ==== Champs snapshot déjà présents dans ta table SQL ====

    @Column(name = "proprietaire_type", nullable = false)
    private String proprietaireType;          // "PERSONNE" ou "ENTREPRISE"

    @Column(name = "proprietaire_nom")
    private String proprietaireNom;

    @Column(name = "proprietaire_prenom")
    private String proprietairePrenom;

    @Column(name = "proprietaire_email")
    private String proprietaireEmail;

    @Column(name = "proprietaire_entreprise_nom")
    private String proprietaireEntrepriseNom;

    // ====== Getters / Setters ======

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBienId() {
        return bienId;
    }

    public void setBienId(Long bienId) {
        this.bienId = bienId;
    }

    public Long getProprietaireUtilisateurId() {
        return proprietaireUtilisateurId;
    }

    public void setProprietaireUtilisateurId(Long proprietaireUtilisateurId) {
        this.proprietaireUtilisateurId = proprietaireUtilisateurId;
    }

    public Long getProprietaireEntrepriseId() {
        return proprietaireEntrepriseId;
    }

    public void setProprietaireEntrepriseId(Long proprietaireEntrepriseId) {
        this.proprietaireEntrepriseId = proprietaireEntrepriseId;
    }

    public Double getQuotePart() {
        return quotePart;
    }

    public void setQuotePart(Double quotePart) {
        this.quotePart = quotePart;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getProprietaireType() {
        return proprietaireType;
    }

    public void setProprietaireType(String proprietaireType) {
        this.proprietaireType = proprietaireType;
    }

    public String getProprietaireNom() {
        return proprietaireNom;
    }

    public void setProprietaireNom(String proprietaireNom) {
        this.proprietaireNom = proprietaireNom;
    }

    public String getProprietairePrenom() {
        return proprietairePrenom;
    }

    public void setProprietairePrenom(String proprietairePrenom) {
        this.proprietairePrenom = proprietairePrenom;
    }

    public String getProprietaireEmail() {
        return proprietaireEmail;
    }

    public void setProprietaireEmail(String proprietaireEmail) {
        this.proprietaireEmail = proprietaireEmail;
    }

    public String getProprietaireEntrepriseNom() {
        return proprietaireEntrepriseNom;
    }

    public void setProprietaireEntrepriseNom(String proprietaireEntrepriseNom) {
        this.proprietaireEntrepriseNom = proprietaireEntrepriseNom;
    }
}
