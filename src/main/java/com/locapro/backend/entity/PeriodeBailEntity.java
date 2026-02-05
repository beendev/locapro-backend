package com.locapro.backend.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "periodes_bail")
public class PeriodeBailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- lien vers le bail ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bail_id", nullable = false)
    private BailEntity bail;

    // --- période temporelle ---

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    // --- finance ---

    @Column(name = "loyer_base", nullable = false)
    private BigDecimal loyerBase;

    @Column(name = "provision_charges", nullable = false)
    private BigDecimal provisionCharges;

    // jour entre 1 et 10
    @Column(name = "jour_echeance", nullable = false)
    private Integer jourEcheance;

    // ex: "2025-01" ou "01/2025" selon ce qu’on décidera
    @Column(name = "mois_indice_base", nullable = false)
    private String moisIndiceBase;

    // pour l’instant "SANTE" toujours
    @Column(name = "type_indice", nullable = false)
    private String typeIndice;

    // --- méta ---

    @Column(name = "cree_le")
    private OffsetDateTime creeLe;

    // INITIAL / INDEXATION / AVENANT / PROLONGATION
    @Column(name = "origine")
    private String origine;

    @Column(name = "motif")
    private String motif;

    // FK vers avenants_bail.id (on reste en Long pour l’instant)
    @Column(name = "source_avenant_id")
    private Long sourceAvenantId;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @Column(name = "type_charges", nullable = false)
    private String typeCharges = "PROVISION";

    // =======================
    // Getters / Setters
    // =======================

    public Long getId() {
        return id;
    }

    public BailEntity getBail() {
        return bail;
    }

    public void setBail(BailEntity bail) {
        this.bail = bail;
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

    public BigDecimal getLoyerBase() {
        return loyerBase;
    }

    public void setLoyerBase(BigDecimal loyerBase) {
        this.loyerBase = loyerBase;
    }

    public BigDecimal getProvisionCharges() {
        return provisionCharges;
    }

    public void setProvisionCharges(BigDecimal provisionCharges) {
        this.provisionCharges = provisionCharges;
    }

    public Integer getJourEcheance() {
        return jourEcheance;
    }

    public void setJourEcheance(Integer jourEcheance) {
        this.jourEcheance = jourEcheance;
    }

    public String getMoisIndiceBase() {
        return moisIndiceBase;
    }

    public void setMoisIndiceBase(String moisIndiceBase) {
        this.moisIndiceBase = moisIndiceBase;
    }

    public String getTypeIndice() {
        return typeIndice;
    }

    public void setTypeIndice(String typeIndice) {
        this.typeIndice = typeIndice;
    }

    public OffsetDateTime getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(OffsetDateTime creeLe) {
        this.creeLe = creeLe;
    }

    public String getOrigine() {
        return origine;
    }

    public void setOrigine(String origine) {
        this.origine = origine;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public Long getSourceAvenantId() {
        return sourceAvenantId;
    }

    public void setSourceAvenantId(Long sourceAvenantId) {
        this.sourceAvenantId = sourceAvenantId;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTypeCharges() {
        return typeCharges;
    }

    public void setTypeCharges(String typeCharges) {
        this.typeCharges = typeCharges;
    }
}
