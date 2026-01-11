package com.locapro.backend.entity;

import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.LangueContrat;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "modeles_bail")
public class ModeleBailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    private RegionBail regionBail;

    @Enumerated(EnumType.STRING)
    @Column(name = "langue", nullable = false)
    private LangueContrat langue;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "type_document", nullable = false)
    private String typeDocument;

    @Column(name = "url_fichier", nullable = false)
    private String urlFichier;

    @Column(name = "actif_bool", nullable = false)
    private Boolean actifBool = Boolean.TRUE;

    @Column(name = "cree_le", updatable = false)
    private OffsetDateTime creeLe;

    @PrePersist
    public void prePersist() {
        if (creeLe == null) {
            creeLe = OffsetDateTime.now();
        }
    }

    // ----- GETTERS / SETTERS -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegionBail getRegion() {
        return regionBail;
    }

    public void setRegion(RegionBail regionBail) {
        this.regionBail = regionBail;
    }

    public LangueContrat getLangue() {
        return langue;
    }

    public void setLangue(LangueContrat langue) {
        this.langue = langue;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }

    public String getUrlFichier() {
        return urlFichier;
    }

    public void setUrlFichier(String urlFichier) {
        this.urlFichier = urlFichier;
    }

    public Boolean getActifBool() {
        return actifBool;
    }

    public void setActifBool(Boolean actifBool) {
        this.actifBool = actifBool;
    }

    public OffsetDateTime getCreeLe() {
        return creeLe;
    }

    public void setCreeLe(OffsetDateTime creeLe) {
        this.creeLe = creeLe;
    }
}
