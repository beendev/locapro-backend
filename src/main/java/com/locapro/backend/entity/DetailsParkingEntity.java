package com.locapro.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "details_parking")
public class DetailsParkingEntity {

    @Id
    @Column(name = "bien_id")
    private Long bienId;

    @Column(name = "numero_place")
    private String numeroPlace;

    @Column(name = "longueur_m")
    private Double longueurM;

    @Column(name = "largeur_m")
    private Double largeurM;

    @Column(name = "type_porte")
    private String typePorte;

    @Column(name = "prise_electrique_bool")
    private Boolean priseElectrique;

    public Long getBienId() {
        return bienId;
    }

    public void setBienId(Long bienId) {
        this.bienId = bienId;
    }

    public Double getLongueurM() {
        return longueurM;
    }

    public void setLongueurM(Double longueurM) {
        this.longueurM = longueurM;
    }

    public String getNumeroPlace() {
        return numeroPlace;
    }

    public void setNumeroPlace(String numeroPlace) {
        this.numeroPlace = numeroPlace;
    }

    public Double getLargeurM() {
        return largeurM;
    }

    public void setLargeurM(Double largeurM) {
        this.largeurM = largeurM;
    }

    public String getTypePorte() {
        return typePorte;
    }

    public void setTypePorte(String typePorte) {
        this.typePorte = typePorte;
    }

    public Boolean getPriseElectrique() {
        return priseElectrique;
    }

    public void setPriseElectrique(Boolean priseElectrique) {
        this.priseElectrique = priseElectrique;
    }
}
