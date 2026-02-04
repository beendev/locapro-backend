package com.locapro.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(schema = "public", name = "utilisateurs")
public class UtilisateurEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "telephone")
    private String telephone;

    // Date civile -> LocalDate (pas de fuseau)
    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    // Optionnel mais unique s'il est fourni (nullable côté DB)
    @Column(name = "numero_ipi", unique = true)
    private String numeroIpi;

    // Authentification
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    // Soft-delete / activation
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "rue")
    private String rue;

    @Column(name = "numero")
    private String numero;

    @Column(name = "boite")
    private String boite;

    @Column(name = "code_postal")
    private String codePostal;

    @Column(name = "ville")
    private String ville;

    @Column(name = "commune")
    private String commune;

    @Column(name = "pays")
    private String pays;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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

    public String getNumeroIpi() {
        return numeroIpi;
    }

    public void setNumeroIpi(String numeroIpi) {
        this.numeroIpi = numeroIpi;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRue() {
        return rue;
    }

    public void setRue(String rue) {
        this.rue = rue;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBoite() {
        return boite;
    }

    public void setBoite(String boite) {
        this.boite = boite;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCommune() {
        return commune;
    }

    public void setCommune(String commune) {
        this.commune = commune;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
