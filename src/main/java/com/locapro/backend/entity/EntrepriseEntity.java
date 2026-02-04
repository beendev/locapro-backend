// src/main/java/com/locapro/backend/entity/EntrepriseEntity.java
package com.locapro.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "entreprises")
public class EntrepriseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> utilisateurs.id (obligatoire)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proprietaire_utilisateur_id", nullable = false)
    private UtilisateurEntity proprietaire;

    @Column(name = "raison_sociale", nullable = false)
    private String raisonSociale;

    // UNIQUE en DB
    @Column(name = "numero_tva", unique = true)
    private String numeroTva;


    @Column(name = "email_pro")
    private String emailPro;

    @Column(name = "telephone_pro")
    private String telephonePro;

    private String iban;

    // Optionnel — si tu gardes la colonne en DB
    private String bic;

    @Column(name = "site_web")
    private String siteWeb;

    @CreationTimestamp
    @Column(name = "cree_le", updatable = false)
    private OffsetDateTime creeLe;

    @UpdateTimestamp
    @Column(name = "maj_le")
    private OffsetDateTime majLe;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "rue", nullable = false)
    private String rue;

    @Column(name = "numero", nullable = false)
    private String numero;

    @Column(name = "boite")
    private String boite;

    @Column(name = "code_postal", nullable = false)
    private String codePostal;

    @Column(name = "ville", nullable = false)
    private String ville;

    @Column(name = "commune")
    private String commune;

    @Column(name = "pays", nullable = false)
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

    public UtilisateurEntity getProprietaire() {
        return proprietaire;
    }

    public void setProprietaire(UtilisateurEntity proprietaire) {
        this.proprietaire = proprietaire;
    }

    public String getRaisonSociale() {
        return raisonSociale;
    }

    public void setRaisonSociale(String raisonSociale) {
        this.raisonSociale = raisonSociale;
    }

    public String getNumeroTva() {
        return numeroTva;
    }

    public void setNumeroTva(String numeroTva) {
        this.numeroTva = numeroTva;
    }

    public String getEmailPro() {
        return emailPro;
    }

    public void setEmailPro(String emailPro) {
        this.emailPro = emailPro;
    }

    public String getTelephonePro() {
        return telephonePro;
    }

    public void setTelephonePro(String telephonePro) {
        this.telephonePro = telephonePro;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
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
