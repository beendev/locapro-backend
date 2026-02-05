package com.locapro.backend.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "baux")
public class BailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =======================
    // Métadonnées
    // =======================

    @Column(name = "cree_le", nullable = false)
    private OffsetDateTime creeLe;

    @Column(name = "maj_le")
    private OffsetDateTime majLe;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    // =======================
    // Core bail
    // =======================

    @Column(name = "nom_bail", nullable = false)
    private String nomBail;

    @Column(name = "bien_id", nullable = false)
    private Long bienId;

    // BXL / WAL / VLA
    @Column(name = "region", nullable = false)
    private String region;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    // BROUILLON / EN_SIGNATURE / SIGNE / RESILIE / ARCHIVE
    @Column(name = "statut", nullable = false)
    private String statut;

    // =======================
    // Utilisateurs / gestion
    // =======================

    // utilisateur qui a créé le bail
    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    // utilisateur responsable du bail
    @Column(name = "utilisateur_responsable_id", nullable = false)
    private Long utilisateurResponsableId;

    // gestionnaire principal
    @Column(name = "gestionnaire_utilisateur_id", nullable = false)
    private Long gestionnaireUtilisateurId;

    // contexte agence (nullable si gestion perso)
    @Column(name = "agence_id")
    private Long agenceId;

    // AGENCE / PROPRIETAIRE / AUTRE
    @Column(name = "gestion_mode")
    private String gestionMode;

    @Column(name = "garantie_montant")
    private BigDecimal garantieMontant;

    @Column(name = "garantie_type")
    private String garantieType;

    @Column(name = "garantie_constitu_bool")
    private Boolean garantieConstituee = false;

    // =======================
    // Contrat & signature
    // =======================

    // CLASSIQUE_9ANS / COURTE_DUREE
    @Column(name = "type_contrat")
    private String typeContrat;

    // NUMERIQUE / PAPIER
    @Column(name = "source_contrat")
    private String sourceContrat;

    @Column(name = "date_signature_papier")
    private LocalDate dateSignaturePapier;

    @Column(name = "preuve_signature_document_id")
    private Long preuveSignatureDocumentId;

    // =======================
    // Paiement & langue
    // =======================

    // FR / NL
    @Column(name = "langue_contrat", nullable = false)
    private String langueContrat;

    @Column(name = "iban_paiement", nullable = false)
    private String ibanPaiement;

    @Column(name = "loyer_reference")
    private BigDecimal loyerReference;

    // =======================
    // Snapshots descriptifs
    // =======================

    @Column(name = "description_bien_snapshot", nullable = false)
    private String descriptionBienSnapshot = "";

    @Column(name = "description_communes_snapshot", nullable = false)
    private String descriptionCommunesSnapshot = "";

    // =======================
    // JSON Wizard (source de vérité)
    // =======================

    @Column(name = "reponses_bail", columnDefinition = "jsonb", nullable = false)
    private String reponsesBail;

    // =======================
    // EDL
    // =======================

    // ENTRE_PARTIES / EXPERT
    @Column(name = "edl_mode")
    private String edlMode;

    @Column(name = "edl_expert_id")
    private Long edlExpertId;

    @Column(name = "edl_avant_document_id")
    private Long edlAvantDocumentId;

    @Column(name = "edl_apres_document_id")
    private Long edlApresDocumentId;

    // =======================
    // Portefeuille
    // =======================

    @Column(name = "portefeuille_id")
    private Long portefeuilleId;

    // =======================
    // Modèle juridique
    // =======================

    @Column(name = "modele_bail_id", nullable = false)
    private Long modeleBailId;


    // =======================
    // Getters / Setters
    // =======================


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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getNomBail() {
        return nomBail;
    }

    public void setNomBail(String nomBail) {
        this.nomBail = nomBail;
    }

    public Long getBienId() {
        return bienId;
    }

    public void setBienId(Long bienId) {
        this.bienId = bienId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }

    public Long getUtilisateurResponsableId() {
        return utilisateurResponsableId;
    }

    public void setUtilisateurResponsableId(Long utilisateurResponsableId) {
        this.utilisateurResponsableId = utilisateurResponsableId;
    }

    public Long getGestionnaireUtilisateurId() {
        return gestionnaireUtilisateurId;
    }

    public void setGestionnaireUtilisateurId(Long gestionnaireUtilisateurId) {
        this.gestionnaireUtilisateurId = gestionnaireUtilisateurId;
    }

    public Long getAgenceId() {
        return agenceId;
    }

    public void setAgenceId(Long agenceId) {
        this.agenceId = agenceId;
    }

    public String getGestionMode() {
        return gestionMode;
    }

    public void setGestionMode(String gestionMode) {
        this.gestionMode = gestionMode;
    }

    public BigDecimal getGarantieMontant() {
        return garantieMontant;
    }

    public void setGarantieMontant(BigDecimal garantieMontant) {
        this.garantieMontant = garantieMontant;
    }

    public String getGarantieType() {
        return garantieType;
    }

    public void setGarantieType(String garantieType) {
        this.garantieType = garantieType;
    }

    public Boolean getGarantieConstituee() {
        return garantieConstituee;
    }

    public void setGarantieConstituee(Boolean garantieConstituee) {
        this.garantieConstituee = garantieConstituee;
    }

    public String getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(String typeContrat) {
        this.typeContrat = typeContrat;
    }

    public String getSourceContrat() {
        return sourceContrat;
    }

    public void setSourceContrat(String sourceContrat) {
        this.sourceContrat = sourceContrat;
    }

    public LocalDate getDateSignaturePapier() {
        return dateSignaturePapier;
    }

    public void setDateSignaturePapier(LocalDate dateSignaturePapier) {
        this.dateSignaturePapier = dateSignaturePapier;
    }

    public Long getPreuveSignatureDocumentId() {
        return preuveSignatureDocumentId;
    }

    public void setPreuveSignatureDocumentId(Long preuveSignatureDocumentId) {
        this.preuveSignatureDocumentId = preuveSignatureDocumentId;
    }

    public String getLangueContrat() {
        return langueContrat;
    }

    public void setLangueContrat(String langueContrat) {
        this.langueContrat = langueContrat;
    }

    public String getIbanPaiement() {
        return ibanPaiement;
    }

    public void setIbanPaiement(String ibanPaiement) {
        this.ibanPaiement = ibanPaiement;
    }

    public BigDecimal getLoyerReference() {
        return loyerReference;
    }

    public void setLoyerReference(BigDecimal loyerReference) {
        this.loyerReference = loyerReference;
    }

    public String getDescriptionBienSnapshot() {
        return descriptionBienSnapshot;
    }

    public void setDescriptionBienSnapshot(String descriptionBienSnapshot) {
        this.descriptionBienSnapshot = descriptionBienSnapshot;
    }

    public String getDescriptionCommunesSnapshot() {
        return descriptionCommunesSnapshot;
    }

    public void setDescriptionCommunesSnapshot(String descriptionCommunesSnapshot) {
        this.descriptionCommunesSnapshot = descriptionCommunesSnapshot;
    }

    public String getReponsesBail() {
        return reponsesBail;
    }

    public void setReponsesBail(String reponsesBail) {
        this.reponsesBail = reponsesBail;
    }

    public String getEdlMode() {
        return edlMode;
    }

    public void setEdlMode(String edlMode) {
        this.edlMode = edlMode;
    }

    public Long getEdlExpertId() {
        return edlExpertId;
    }

    public void setEdlExpertId(Long edlExpertId) {
        this.edlExpertId = edlExpertId;
    }

    public Long getEdlAvantDocumentId() {
        return edlAvantDocumentId;
    }

    public void setEdlAvantDocumentId(Long edlAvantDocumentId) {
        this.edlAvantDocumentId = edlAvantDocumentId;
    }

    public Long getEdlApresDocumentId() {
        return edlApresDocumentId;
    }

    public void setEdlApresDocumentId(Long edlApresDocumentId) {
        this.edlApresDocumentId = edlApresDocumentId;
    }

    public Long getPortefeuilleId() {
        return portefeuilleId;
    }

    public void setPortefeuilleId(Long portefeuilleId) {
        this.portefeuilleId = portefeuilleId;
    }

    public Long getModeleBailId() {
        return modeleBailId;
    }

    public void setModeleBailId(Long modeleBailId) {
        this.modeleBailId = modeleBailId;
    }
}
