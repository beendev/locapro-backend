package com.locapro.backend.entity;

import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.TypeContratBail; // <--- Import Important

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "modeles_bail")
public class ModeleBailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BRUXELLES, WALLONIE, FLANDRE
    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    private RegionBail regionBail;

    // FR, NL, EN
    @Enumerated(EnumType.STRING)
    @Column(name = "langue", nullable = false)
    private LangueContrat langue;

    // C'EST ICI QU'ON CHANGE : On utilise l'Enum strict
    // CLASSIQUE_9ANS, ETUDIANT, COLOCATION...
    @Enumerated(EnumType.STRING)
    @Column(name = "type_contrat", nullable = false)
    private TypeContratBail typeContrat;

    // "templates/baux/bruxelles_2024_v1.docx"
    @Column(name = "url_fichier", nullable = false)
    private String urlFichier;

    @Column(name = "version")
    private String version;

    @Column(name = "actif_bool", nullable = false)
    private Boolean actifBool = Boolean.TRUE;

    @Column(name = "cree_le", updatable = false)
    private OffsetDateTime creeLe;

    @PrePersist
    public void prePersist() {
        if (creeLe == null) creeLe = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RegionBail getRegionBail() {
        return regionBail;
    }

    public void setRegionBail(RegionBail regionBail) {
        this.regionBail = regionBail;
    }

    public LangueContrat getLangue() {
        return langue;
    }

    public void setLangue(LangueContrat langue) {
        this.langue = langue;
    }

    public TypeContratBail getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(TypeContratBail typeContrat) {
        this.typeContrat = typeContrat;
    }

    public String getUrlFichier() {
        return urlFichier;
    }

    public void setUrlFichier(String urlFichier) {
        this.urlFichier = urlFichier;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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