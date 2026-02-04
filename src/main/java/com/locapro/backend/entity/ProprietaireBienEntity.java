package com.locapro.backend.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

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


    @Column(name = "proprietaire_date_naissance")
    private LocalDate proprietaireDateNaissance;

    @Column(name = "proprietaire_lieu_naissance") // snake_case
    private String proprietaireLieuNaissance;



    @Column(name = "proprietaire_entreprise_nom")
    private String proprietaireEntrepriseNom;

    // Ajoute ces champs dans ta classe Entity
    @Column(name = "adresse_rue")
    private String adresseRue;

    @Column(name = "adresse_numero")
    private String adresseNumero;

    @Column(name = "adresse_boite")
    private String adresseBoite;

    @Column(name = "adresse_code_postal")
    private String adresseCodePostal;

    @Column(name = "adresse_ville")
    private String adresseVille;

    @Column(name = "adresse_commune")
    private String adresseCommune;

    @Column(name = "adresse_pays")
    private String adressePays;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "numero_bce")
    private String numeroBce;

    @Column(name = "representant_legal")
    private String representantLegal;


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

    public String getAdresseRue() {
        return adresseRue;
    }

    public void setAdresseRue(String adresseRue) {
        this.adresseRue = adresseRue;
    }

    public String getAdresseNumero() {
        return adresseNumero;
    }

    public void setAdresseNumero(String adresseNumero) {
        this.adresseNumero = adresseNumero;
    }

    public String getAdresseBoite() {
        return adresseBoite;
    }

    public void setAdresseBoite(String adresseBoite) {
        this.adresseBoite = adresseBoite;
    }

    public String getAdresseCodePostal() {
        return adresseCodePostal;
    }

    public void setAdresseCodePostal(String adresseCodePostal) {
        this.adresseCodePostal = adresseCodePostal;
    }

    public String getAdresseVille() {
        return adresseVille;
    }

    public void setAdresseVille(String adresseVille) {
        this.adresseVille = adresseVille;
    }

    public String getAdresseCommune() {
        return adresseCommune;
    }

    public void setAdresseCommune(String adresseCommune) {
        this.adresseCommune = adresseCommune;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdressePays() {
        return adressePays;
    }

    public void setAdressePays(String adressePays) {
        this.adressePays = adressePays;
    }

    public String getNumeroBce() {
        return numeroBce;
    }

    public void setNumeroBce(String numeroBce) {
        this.numeroBce = numeroBce;
    }

    public String getRepresentantLegal() {
        return representantLegal;
    }

    public void setRepresentantLegal(String representantLegal) {
        this.representantLegal = representantLegal;
    }

    public LocalDate getProprietaireDateNaissance() {
        return proprietaireDateNaissance;
    }

    public void setProprietaireDateNaissance(LocalDate proprietaireDateNaissance) {
        this.proprietaireDateNaissance = proprietaireDateNaissance;
    }
    public String getProprietaireLieuNaissance() {
        return proprietaireLieuNaissance;
    }

    public void setProprietaireLieuNaissance(String proprietaireLieuNaissance) {
        this.proprietaireLieuNaissance = proprietaireLieuNaissance;
    }
}
