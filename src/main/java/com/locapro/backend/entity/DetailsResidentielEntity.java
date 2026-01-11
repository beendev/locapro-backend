package com.locapro.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "details_residentiel")
public class DetailsResidentielEntity {

    @Id
    @Column(name = "bien_id")
    private Long bienId; // PK = FK vers biens.id

    @Column(name = "superficie_habitable_m2")
    private Double superficieHabitableM2;

    @Column(name = "nombre_facades")
    private Integer nombreFacades;

    @Column(name = "etage")
    private Integer etage;

    @Column(name = "annee_construction")
    private Integer anneeConstruction;

    @Column(name = "annee_renovation")
    private Integer anneeRenovation;

    @Column(name = "nb_chambres")
    private Integer nbChambres;

    @Column(name = "nb_salles_bain")
    private Integer nbSallesBain;

    @Column(name = "nb_salles_douche")
    private Integer nbSallesDouche;

    @Column(name = "nb_wc")
    private Integer nbWc;

    @Column(name = "hall_entree_bool")
    private Boolean hallEntree;

    @Column(name = "type_cuisine")
    private String typeCuisine;

    @Column(name = "peb_classe")
    private String pebClasse;

    @Column(name = "peb_conso_kwh_m2_an")
    private Double pebConsoKwhM2An;

    @Column(name = "type_chassis")
    private String typeChassis;

    @Column(name = "type_chauffage")
    private String typeChauffage;

    @Column(name = "electricite_conforme")
    private String electriciteConforme;

    @Column(name = "detecteurs_fumee_bool")
    private Boolean detecteursFumee;

    @Column(name = "meuble_bool")
    private Boolean meuble;

    @Column(name = "parlophone_bool")
    private Boolean parlophone;

    @Column(name = "alarme_bool")
    private Boolean alarme;

    @Column(name = "qualite_sols")
    private String qualiteSols;

    @Column(name = "jardin_bool")
    private Boolean jardin;

    @Column(name = "jardin_surface_m2")
    private Double jardinSurfaceM2;

    @Column(name = "terrasse_bool")
    private Boolean terrasse;

    @Column(name = "terrasse_surface_m2")
    private Double terrasseSurfaceM2;

    @Column(name = "balcon_bool")
    private Boolean balcon;

    @Column(name = "cave_bool")
    private Boolean cave;

    @Column(name = "grenier_bool")
    private Boolean grenier;

    @Column(name = "enabled")
    private Boolean enabled = true;

    public Long getBienId() {
        return bienId;
    }

    public void setBienId(Long bienId) {
        this.bienId = bienId;
    }

    public Double getSuperficieHabitableM2() {
        return superficieHabitableM2;
    }

    public void setSuperficieHabitableM2(Double superficieHabitableM2) {
        this.superficieHabitableM2 = superficieHabitableM2;
    }

    public Integer getNombreFacades() {
        return nombreFacades;
    }

    public void setNombreFacades(Integer nombreFacades) {
        this.nombreFacades = nombreFacades;
    }

    public Integer getEtage() {
        return etage;
    }

    public void setEtage(Integer etage) {
        this.etage = etage;
    }

    public Integer getAnneeConstruction() {
        return anneeConstruction;
    }

    public void setAnneeConstruction(Integer anneeConstruction) {
        this.anneeConstruction = anneeConstruction;
    }

    public Integer getAnneeRenovation() {
        return anneeRenovation;
    }

    public void setAnneeRenovation(Integer anneeRenovation) {
        this.anneeRenovation = anneeRenovation;
    }

    public Integer getNbChambres() {
        return nbChambres;
    }

    public void setNbChambres(Integer nbChambres) {
        this.nbChambres = nbChambres;
    }

    public Integer getNbSallesBain() {
        return nbSallesBain;
    }

    public void setNbSallesBain(Integer nbSallesBain) {
        this.nbSallesBain = nbSallesBain;
    }

    public Integer getNbSallesDouche() {
        return nbSallesDouche;
    }

    public void setNbSallesDouche(Integer nbSallesDouche) {
        this.nbSallesDouche = nbSallesDouche;
    }

    public Integer getNbWc() {
        return nbWc;
    }

    public void setNbWc(Integer nbWc) {
        this.nbWc = nbWc;
    }

    public Boolean getHallEntree() {
        return hallEntree;
    }

    public void setHallEntree(Boolean hallEntree) {
        this.hallEntree = hallEntree;
    }

    public String getTypeCuisine() {
        return typeCuisine;
    }

    public void setTypeCuisine(String typeCuisine) {
        this.typeCuisine = typeCuisine;
    }

    public String getPebClasse() {
        return pebClasse;
    }

    public void setPebClasse(String pebClasse) {
        this.pebClasse = pebClasse;
    }

    public Double getPebConsoKwhM2An() {
        return pebConsoKwhM2An;
    }

    public void setPebConsoKwhM2An(Double pebConsoKwhM2An) {
        this.pebConsoKwhM2An = pebConsoKwhM2An;
    }

    public String getTypeChassis() {
        return typeChassis;
    }

    public void setTypeChassis(String typeChassis) {
        this.typeChassis = typeChassis;
    }

    public String getTypeChauffage() {
        return typeChauffage;
    }

    public void setTypeChauffage(String typeChauffage) {
        this.typeChauffage = typeChauffage;
    }

    public String getElectriciteConforme() {
        return electriciteConforme;
    }

    public void setElectriciteConforme(String electriciteConforme) {
        this.electriciteConforme = electriciteConforme;
    }

    public Boolean getDetecteursFumee() {
        return detecteursFumee;
    }

    public void setDetecteursFumee(Boolean detecteursFumee) {
        this.detecteursFumee = detecteursFumee;
    }

    public Boolean getMeuble() {
        return meuble;
    }

    public void setMeuble(Boolean meuble) {
        this.meuble = meuble;
    }

    public Boolean getParlophone() {
        return parlophone;
    }

    public void setParlophone(Boolean parlophone) {
        this.parlophone = parlophone;
    }

    public Boolean getAlarme() {
        return alarme;
    }

    public void setAlarme(Boolean alarme) {
        this.alarme = alarme;
    }

    public String getQualiteSols() {
        return qualiteSols;
    }

    public void setQualiteSols(String qualiteSols) {
        this.qualiteSols = qualiteSols;
    }

    public Boolean getJardin() {
        return jardin;
    }

    public void setJardin(Boolean jardin) {
        this.jardin = jardin;
    }

    public Double getJardinSurfaceM2() {
        return jardinSurfaceM2;
    }

    public void setJardinSurfaceM2(Double jardinSurfaceM2) {
        this.jardinSurfaceM2 = jardinSurfaceM2;
    }

    public Boolean getTerrasse() {
        return terrasse;
    }

    public void setTerrasse(Boolean terrasse) {
        this.terrasse = terrasse;
    }

    public Double getTerrasseSurfaceM2() {
        return terrasseSurfaceM2;
    }

    public void setTerrasseSurfaceM2(Double terrasseSurfaceM2) {
        this.terrasseSurfaceM2 = terrasseSurfaceM2;
    }

    public Boolean getBalcon() {
        return balcon;
    }

    public void setBalcon(Boolean balcon) {
        this.balcon = balcon;
    }

    public Boolean getCave() {
        return cave;
    }

    public void setCave(Boolean cave) {
        this.cave = cave;
    }

    public Boolean getGrenier() {
        return grenier;
    }

    public void setGrenier(Boolean grenier) {
        this.grenier = grenier;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    // Getters/setters...
}
