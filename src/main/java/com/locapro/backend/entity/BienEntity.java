package com.locapro.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "biens")
public class BienEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // colonne renommée en DB : nom_reference -> reference_interne
    @Column(name = "reference_interne",updatable = false)
    private String nomReference;

    @Column(name = "type_bien", nullable = false)
    private String typeBien; // RESIDENTIEL, PARKING, COMMERCE, BUREAU, AUTRE

    @Column(name = "sous_type")
    private String sousType;

    // lot_ou_unite SUPPRIMÉ en DB → champ supprimé

    @Column(name = "statut")
    private String statut; // ACTIF, ARCHIVE, ...

    @Column(name = "cree_le")
    private OffsetDateTime creeLe;

    @Column(name = "maj_le")
    private OffsetDateTime majLe;

    @Column(name = "parent_bien_id")
    private Long parentBienId;

    @Column(name = "code_public")
    private UUID codePublic;

    @Column(name = "agence_id")
    private Long agenceId;
    // colonne renommée en DB : libelle_unite -> libelle
    @Column(name = "libelle")
    private String libelleUnite;

    @Column(name = "est_unite_locative", nullable = false)
    private boolean estUniteLocative;

    @Column(name = "boite_unite")
    private String boiteUnite;


    @Column(name = "numero_porte")
    private String numeroPorte;


    @Column(name = "notes_identification")
    private String notesIdentification;

    @Column(name = "gestionnaire_utilisateur_id")
    private Long gestionnaireUtilisateurId;


    @Column(name = "portefeuille_id")
    private Long portefeuilleId;

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

    @Column(name = "pays")
    private String pays;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.creeLe = now;
        this.majLe = now;
        if (this.statut == null) {
            this.statut = "ACTIF";
        }
        if (this.codePublic == null) {
            this.codePublic = UUID.randomUUID();
        }
        // plus de verifStatut ici, la colonne n’existe plus
    }

    @PreUpdate
    public void preUpdate() {
        this.majLe = OffsetDateTime.now();
    }

    // Getters & setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getNomReference() {
        return nomReference;
    }



    public void setNomReference(String nomReference) {
        this.nomReference = nomReference;
    }

    public String getTypeBien() {
        return typeBien;
    }

    public void setTypeBien(String typeBien) {
        this.typeBien = typeBien;
    }

    public String getSousType() {
        return sousType;
    }

    public void setSousType(String sousType) {
        this.sousType = sousType;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
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

    public Long getParentBienId() {
        return parentBienId;
    }

    public void setParentBienId(Long parentBienId) {
        this.parentBienId = parentBienId;
    }

    public UUID getCodePublic() {
        return codePublic;
    }

    public void setCodePublic(UUID codePublic) {
        this.codePublic = codePublic;
    }

    public String getLibelleUnite() {
        return libelleUnite;
    }

    public void setLibelleUnite(String libelleUnite) {
        this.libelleUnite = libelleUnite;
    }

    public boolean isEstUniteLocative() {
        return estUniteLocative;
    }

    public void setEstUniteLocative(boolean estUniteLocative) {
        this.estUniteLocative = estUniteLocative;
    }

    public String getBoiteUnite() {
        return boiteUnite;
    }

    public void setBoiteUnite(String boiteUnite) {
        this.boiteUnite = boiteUnite;
    }


    public String getNumeroPorte() {
        return numeroPorte;
    }

    public void setNumeroPorte(String numeroPorte) {
        this.numeroPorte = numeroPorte;
    }

    public String getNotesIdentification() {
        return notesIdentification;
    }

    public void setNotesIdentification(String notesIdentification) {
        this.notesIdentification = notesIdentification;
    }

    public Long getPortefeuilleId() {
        return portefeuilleId;
    }

    public void setPortefeuilleId(Long portefeuilleId) {
        this.portefeuilleId = portefeuilleId;
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

    public Long getAgenceId() {
        return agenceId;
    }

    public void setAgenceId(Long agenceId) {
        this.agenceId = agenceId;
    }

    public Long getGestionnaireUtilisateurId() {
        return gestionnaireUtilisateurId;
    }

    public void setGestionnaireUtilisateurId(Long gestionnaireUtilisateurId) {
        this.gestionnaireUtilisateurId = gestionnaireUtilisateurId;
    }
}
