package com.locapro.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "details_commerce")
public class DetailsCommerceEntity {

    @Id
    @Column(name = "bien_id")
    private Long bienId;

    @Column(name = "surface_commerciale_m2")
    private Double surfaceCommercialeM2;

    @Column(name = "surface_vitrine_m2")
    private Double surfaceVitrineM2;

    @Column(name = "extraction_horeca_bool")
    private Boolean extractionHoreca;

    @Column(name = "surface_reserve_m2")
    private Double surfaceReserveM2;

    @Column(name = "enabled")
    private Boolean enabled = true;

    public Long getBienId() {
        return bienId;
    }

    public void setBienId(Long bienId) {
        this.bienId = bienId;
    }

    public Double getSurfaceCommercialeM2() {
        return surfaceCommercialeM2;
    }

    public void setSurfaceCommercialeM2(Double surfaceCommercialeM2) {
        this.surfaceCommercialeM2 = surfaceCommercialeM2;
    }

    public Double getSurfaceVitrineM2() {
        return surfaceVitrineM2;
    }

    public void setSurfaceVitrineM2(Double surfaceVitrineM2) {
        this.surfaceVitrineM2 = surfaceVitrineM2;
    }

    public Boolean getExtractionHoreca() {
        return extractionHoreca;
    }

    public void setExtractionHoreca(Boolean extractionHoreca) {
        this.extractionHoreca = extractionHoreca;
    }

    public Double getSurfaceReserveM2() {
        return surfaceReserveM2;
    }

    public void setSurfaceReserveM2(Double surfaceReserveM2) {
        this.surfaceReserveM2 = surfaceReserveM2;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }


// Getters/setters...
}
