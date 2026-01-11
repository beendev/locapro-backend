package com.locapro.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "details_bureau")
public class DetailsBureauEntity {

    @Id
    @Column(name = "bien_id")
    private Long bienId;

    @Column(name = "surface_bureaux_m2")
    private Double surfaceBureauxM2;

    @Column(name = "nb_bureaux_cloisonnes")
    private Integer nbBureauxCloisonnes;

    @Column(name = "salle_reunion_bool")
    private Boolean salleReunion;

    @Column(name = "cablage_informatique_bool")
    private Boolean cablageInformatique;

    @Column(name = "enabled")
    private Boolean enabled = true;

    public Long getBienId() {
        return bienId;
    }

    public void setBienId(Long bienId) {
        this.bienId = bienId;
    }

    public Double getSurfaceBureauxM2() {
        return surfaceBureauxM2;
    }

    public void setSurfaceBureauxM2(Double surfaceBureauxM2) {
        this.surfaceBureauxM2 = surfaceBureauxM2;
    }

    public Integer getNbBureauxCloisonnes() {
        return nbBureauxCloisonnes;
    }

    public void setNbBureauxCloisonnes(Integer nbBureauxCloisonnes) {
        this.nbBureauxCloisonnes = nbBureauxCloisonnes;
    }

    public Boolean getSalleReunion() {
        return salleReunion;
    }

    public void setSalleReunion(Boolean salleReunion) {
        this.salleReunion = salleReunion;
    }

    public Boolean getCablageInformatique() {
        return cablageInformatique;
    }

    public void setCablageInformatique(Boolean cablageInformatique) {
        this.cablageInformatique = cablageInformatique;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
