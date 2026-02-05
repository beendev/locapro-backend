package com.locapro.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "locataires")
public class LocataireEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- MÉTADONNÉES ---
    @Column(name = "cree_le", nullable = false)
    private OffsetDateTime creeLe;

    @Column(name = "maj_le")
    private OffsetDateTime majLe;

    @Column(name = "gestionnaire_id", nullable = false)
    private Long gestionnaireId;

    @Column(name = "utilisateur_id")
    private Long utilisateurId; // Lien optionnel vers un compte User

    // --- IDENTITÉ ---
    @Column(name = "type_partie", nullable = false)
    private String typePartie = "PERSONNE_PHYSIQUE"; // ou SOCIETE

    @Column(name = "civilite")
    private String civilite; // M. ou Mme

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email")
    private String email;

    @Column(name = "telephone")
    private String telephone;

    // --- DONNÉES JURIDIQUES (BAIL BELGE) ---
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance")
    private String lieuNaissance;

    @Column(name = "etat_civil")
    private String etatCivil; // CELIBATAIRE, MARIE, ETC.

    @Column(name = "registre_national")
    private String registreNational; // NISS

    @Column(name = "nom_conjoint")
    private String nomConjoint;

    // --- ADRESSE ACTUELLE ---
    @Column(name = "adresse_rue")
    private String adresseRue;

    @Column(name = "adresse_numero")
    private String adresseNumero;

    @Column(name = "adresse_boite")
    private String adresseBoite;

    @Column(name = "adresse_cp")
    private String adresseCodePostal;

    @Column(name = "adresse_ville")
    private String adresseVille;

    @Column(name = "adresse_pays")
    private String adressePays = "Belgique";

    // --- SOLVABILITÉ ---
    @Column(name = "revenus_mensuels")
    private BigDecimal revenusMensuels;

    @Column(name = "garant_nom")
    private String garantNom;

    // --- LIFECYCLE ---
    @PrePersist
    public void onPrePersist() {
        this.creeLe = OffsetDateTime.now();
        if (this.majLe == null) this.majLe = OffsetDateTime.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.majLe = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OffsetDateTime getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(OffsetDateTime creeLe) {
        this.creeLe = creeLe;
    }

    public OffsetDateTime getMajLe() {
        return majLe;
    }

    public void setMajLe(OffsetDateTime majLe) {
        this.majLe = majLe;
    }

    public Long getGestionnaireId() {
        return gestionnaireId;
    }

    public void setGestionnaireId(Long gestionnaireId) {
        this.gestionnaireId = gestionnaireId;
    }

    public Long getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Long utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public String getTypePartie() {
        return typePartie;
    }

    public void setTypePartie(String typePartie) {
        this.typePartie = typePartie;
    }

    public String getCivilite() {
        return civilite;
    }

    public void setCivilite(String civilite) {
        this.civilite = civilite;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public String getEtatCivil() {
        return etatCivil;
    }

    public void setEtatCivil(String etatCivil) {
        this.etatCivil = etatCivil;
    }

    public String getRegistreNational() {
        return registreNational;
    }

    public void setRegistreNational(String registreNational) {
        this.registreNational = registreNational;
    }

    public String getNomConjoint() {
        return nomConjoint;
    }

    public void setNomConjoint(String nomConjoint) {
        this.nomConjoint = nomConjoint;
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

    public String getAdressePays() {
        return adressePays;
    }

    public void setAdressePays(String adressePays) {
        this.adressePays = adressePays;
    }

    public BigDecimal getRevenusMensuels() {
        return revenusMensuels;
    }

    public void setRevenusMensuels(BigDecimal revenusMensuels) {
        this.revenusMensuels = revenusMensuels;
    }

    public String getGarantNom() {
        return garantNom;
    }

    public void setGarantNom(String garantNom) {
        this.garantNom = garantNom;
    }
// Ajoute le reste avec Alt+Insert (IntelliJ) ou Source Action (VS Code)
}