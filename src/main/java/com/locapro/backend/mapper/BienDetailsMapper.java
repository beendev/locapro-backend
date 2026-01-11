package com.locapro.backend.mapper;

import com.locapro.backend.dto.bien.DetailsSpecifiquesRequest;
import com.locapro.backend.entity.DetailsResidentielEntity;
import com.locapro.backend.entity.DetailsCommerceEntity;
import com.locapro.backend.entity.DetailsBureauEntity;
import com.locapro.backend.entity.DetailsParkingEntity;
import com.locapro.backend.entity.BienEntity;
import org.springframework.stereotype.Component;

@Component
public class BienDetailsMapper {

    // ========= RESIDENTIEL =========

    public DetailsResidentielEntity toDetailsResidentielEntity(DetailsSpecifiquesRequest d, BienEntity bien) {
        if (d == null || bien == null) return null;

        DetailsResidentielEntity e = new DetailsResidentielEntity();
        e.setBienId(bien.getId());

        applyResidentielFields(d, e);

        return e;
    }

    public void updateDetailsResidentielEntity(DetailsSpecifiquesRequest d, DetailsResidentielEntity e) {
        if (d == null || e == null) return;
        // on ne touche PAS à e.setId(...) ni e.setBienId(...)
        applyResidentielFields(d, e);
    }

    private void applyResidentielFields(DetailsSpecifiquesRequest d, DetailsResidentielEntity e) {
        e.setSuperficieHabitableM2(d.superficieHabitableM2());
        e.setNombreFacades(d.nombreFacades());
        e.setEtage(d.etage());
        e.setAnneeConstruction(d.anneeConstruction());
        e.setAnneeRenovation(d.anneeRenovation());
        e.setNbChambres(d.nbChambres());
        e.setNbSallesBain(d.nbSallesBain());
        e.setNbSallesDouche(d.nbSallesDouche());
        e.setNbWc(d.nbWc());
        e.setHallEntree(d.hallEntree());
        e.setTypeCuisine(d.typeCuisine());
        e.setPebClasse(d.pebClasse());
        e.setPebConsoKwhM2An(d.pebConsoKwhM2An());
        e.setTypeChassis(d.typeChassis());
        e.setTypeChauffage(d.typeChauffage());
        e.setElectriciteConforme(d.electriciteConforme());
        e.setDetecteursFumee(d.detecteursFumee());
        e.setMeuble(d.meuble());
        e.setParlophone(d.parlophone());
        e.setAlarme(d.alarme());
        e.setQualiteSols(d.qualiteSols());
        e.setJardin(d.jardin());
        e.setJardinSurfaceM2(d.jardinSurfaceM2());
        e.setTerrasse(d.terrasse());
        e.setTerrasseSurfaceM2(d.terrasseSurfaceM2());
        e.setBalcon(d.balcon());
        e.setCave(d.cave());
        e.setGrenier(d.grenier());
    }

    // ========= COMMERCE =========

    public DetailsCommerceEntity toDetailsCommerceEntity(DetailsSpecifiquesRequest d, BienEntity bien) {
        if (d == null || bien == null) return null;

        DetailsCommerceEntity e = new DetailsCommerceEntity();
        e.setBienId(bien.getId());
        applyCommerceFields(d, e);
        return e;
    }

    public void updateDetailsCommerceEntity(DetailsSpecifiquesRequest d, DetailsCommerceEntity e) {
        if (d == null || e == null) return;
        applyCommerceFields(d, e);
    }

    private void applyCommerceFields(DetailsSpecifiquesRequest d, DetailsCommerceEntity e) {
        e.setSurfaceCommercialeM2(d.surfaceCommercialeM2());
        e.setSurfaceVitrineM2(d.surfaceVitrineM2());
        e.setSurfaceReserveM2(d.surfaceReserveM2());
        e.setExtractionHoreca(d.extractionHoreca());
    }

    // ========= BUREAU =========

    public DetailsBureauEntity toDetailsBureauEntity(DetailsSpecifiquesRequest d, BienEntity bien) {
        if (d == null || bien == null) return null;

        DetailsBureauEntity e = new DetailsBureauEntity();
        e.setBienId(bien.getId());
        applyBureauFields(d, e);
        return e;
    }

    public void updateDetailsBureauEntity(DetailsSpecifiquesRequest d, DetailsBureauEntity e) {
        if (d == null || e == null) return;
        applyBureauFields(d, e);
    }

    private void applyBureauFields(DetailsSpecifiquesRequest d, DetailsBureauEntity e) {
        e.setSurfaceBureauxM2(d.surfaceBureauxM2());
        e.setNbBureauxCloisonnes(d.nbBureauxCloisonnes());
        e.setSalleReunion(d.salleReunion());
        e.setCablageInformatique(d.cablageInformatique());
    }

    // ========= PARKING =========

    public DetailsParkingEntity toDetailsParkingEntity(DetailsSpecifiquesRequest d, BienEntity bien) {
        if (d == null || bien == null) return null;

        DetailsParkingEntity p = new DetailsParkingEntity();
        p.setBienId(bien.getId());
        applyParkingFields(d, p);
        return p;
    }

    public void updateDetailsParkingEntity(DetailsSpecifiquesRequest d, DetailsParkingEntity p) {
        if (d == null || p == null) return;
        applyParkingFields(d, p);
    }

    private void applyParkingFields(DetailsSpecifiquesRequest d, DetailsParkingEntity p) {
        p.setNumeroPlace(d.numeroPlace());
        p.setLongueurM(d.longueurM());
        p.setLargeurM(d.largeurM());
        p.setTypePorte(d.typePorte());
        p.setPriseElectrique(d.priseElectrique());
    }

    // ========= UTILITAIRE : parking intégré ? =========

    public boolean hasParkingInfo(DetailsSpecifiquesRequest d) {
        if (d == null) return false;

        return d.numeroPlace() != null
                || d.longueurM() != null
                || d.largeurM() != null
                || d.typePorte() != null
                || d.priseElectrique() != null;
    }
}
