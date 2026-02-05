package com.locapro.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "baux_locataires")
public class BailLocataireEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bail_id", nullable = false)
    private Long bailId;

    @Column(name = "locataire_id", nullable = false)
    private Long locataireId;

    @Column(name = "role_dans_bail", nullable = false)
    private String roleDansBail = "PRENEUR"; // PRENEUR, CONJOINT, GARANT

    @Column(name = "a_signe")
    private Boolean aSigne = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBailId() {
        return bailId;
    }

    public void setBailId(Long bailId) {
        this.bailId = bailId;
    }

    public Long getLocataireId() {
        return locataireId;
    }

    public void setLocataireId(Long locataireId) {
        this.locataireId = locataireId;
    }

    public String getRoleDansBail() {
        return roleDansBail;
    }

    public void setRoleDansBail(String roleDansBail) {
        this.roleDansBail = roleDansBail;
    }

    public Boolean getaSigne() {
        return aSigne;
    }

    public void setaSigne(Boolean aSigne) {
        this.aSigne = aSigne;
    }
}