package com.locapro.backend.mapper;

import com.locapro.backend.dto.bien.DetailsSpecifiquesRequest;
import com.locapro.backend.entity.*;
import org.springframework.stereotype.Component;

@Component
public class BienDetailsMapper {

    // ========================================================================
    // 1. RÉSIDENTIEL (Appart, Maison...)
    // ========================================================================

    public DetailsResidentielEntity toDetailsResidentielEntity(DetailsSpecifiquesRequest d, BienEntity bien) {
        if (d == null || bien == null) return null;
        DetailsResidentielEntity e = new DetailsResidentielEntity();
        e.setBienId(bien.getId());
        applyResidentielFields(d, e);
        return e;
    }

    public void updateDetailsResidentielEntity(DetailsSpecifiquesRequest d, DetailsResidentielEntity e) {
        if (d == null || e == null) return;
        applyResidentielFields(d, e); // <-- On réutilise la même logique
    }

    // 👇 LA LISTE COMPLÈTE (Vérifiée avec ton Frontend)
    private void applyResidentielFields(DetailsSpecifiquesRequest d, DetailsResidentielEntity e) {
        // Surfaces & Dimensions
        if(d.superficieHabitableM2() != null) e.setSuperficieHabitableM2(d.superficieHabitableM2());
        if(d.nombreFacades() != null) e.setNombreFacades(d.nombreFacades());
        if(d.etage() != null) e.setEtage(d.etage());

        // Années
        if(d.anneeConstruction() != null) e.setAnneeConstruction(d.anneeConstruction());
        if(d.anneeRenovation() != null) e.setAnneeRenovation(d.anneeRenovation());

        // Pièces
        if(d.nbChambres() != null) e.setNbChambres(d.nbChambres());
        if(d.nbSallesBain() != null) e.setNbSallesBain(d.nbSallesBain());
        if(d.nbSallesDouche() != null) e.setNbSallesDouche(d.nbSallesDouche());
        if(d.nbWc() != null) e.setNbWc(d.nbWc());

        // Techniques (Enums / Strings)
        if(d.typeCuisine() != null) e.setTypeCuisine(d.typeCuisine());
        if(d.typeChauffage() != null) e.setTypeChauffage(d.typeChauffage());
        if(d.typeChassis() != null) e.setTypeChassis(d.typeChassis()); // <-- Souvent oublié !
        if(d.pebClasse() != null) e.setPebClasse(d.pebClasse());
        if(d.pebConsoKwhM2An() != null) e.setPebConsoKwhM2An(d.pebConsoKwhM2An());
        if(d.electriciteConforme() != null) e.setElectriciteConforme(d.electriciteConforme());
        if(d.qualiteSols() != null) e.setQualiteSols(d.qualiteSols());
        if(d.pebNumero() != null) e.setPebNumero(d.pebNumero());
        if(d.pebDateValidite() != null) e.setPebDateValidite(d.pebDateValidite());

        // Booleans (Équipements)
        if(d.meuble() != null) e.setMeuble(d.meuble());
        if(d.terrasse() != null) e.setTerrasse(d.terrasse());
        if(d.terrasseSurfaceM2() != null) e.setTerrasseSurfaceM2(d.terrasseSurfaceM2());
        if(d.jardin() != null) e.setJardin(d.jardin());
        if(d.jardinSurfaceM2() != null) e.setJardinSurfaceM2(d.jardinSurfaceM2());
        if(d.balcon() != null) e.setBalcon(d.balcon());
        if(d.cave() != null) e.setCave(d.cave());
        if(d.grenier() != null) e.setGrenier(d.grenier());
        if(d.parlophone() != null) e.setParlophone(d.parlophone());
        if(d.alarme() != null) e.setAlarme(d.alarme());
        if(d.detecteursFumee() != null) e.setDetecteursFumee(d.detecteursFumee());
        if(d.hallEntree() != null) e.setHallEntree(d.hallEntree());
    }

    // ========================================================================
    // 2. COMMERCE
    // ========================================================================

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
        if(d.surfaceCommercialeM2() != null) e.setSurfaceCommercialeM2(d.surfaceCommercialeM2());
        if(d.surfaceVitrineM2() != null) e.setSurfaceVitrineM2(d.surfaceVitrineM2());
        if(d.surfaceReserveM2() != null) e.setSurfaceReserveM2(d.surfaceReserveM2());
        if(d.extractionHoreca() != null) e.setExtractionHoreca(d.extractionHoreca());
    }

    // ========================================================================
    // 3. BUREAU
    // ========================================================================

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
        if(d.surfaceBureauxM2() != null) e.setSurfaceBureauxM2(d.surfaceBureauxM2());
        if(d.nbBureauxCloisonnes() != null) e.setNbBureauxCloisonnes(d.nbBureauxCloisonnes());
        if(d.salleReunion() != null) e.setSalleReunion(d.salleReunion());
        if(d.cablageInformatique() != null) e.setCablageInformatique(d.cablageInformatique());
    }

    // ========================================================================
    // 4. PARKING
    // ========================================================================

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
        if(d.numeroPlace() != null) p.setNumeroPlace(d.numeroPlace());
        if(d.longueurM() != null) p.setLongueurM(d.longueurM());
        if(d.largeurM() != null) p.setLargeurM(d.largeurM());
        if(d.typePorte() != null) p.setTypePorte(d.typePorte());
        if(d.priseElectrique() != null) p.setPriseElectrique(d.priseElectrique());
    }

    // Utilitaire
    public boolean hasParkingInfo(DetailsSpecifiquesRequest d) {
        if (d == null) return false;
        return d.numeroPlace() != null || d.longueurM() != null
                || d.largeurM() != null || d.typePorte() != null || d.priseElectrique() != null;
    }

    // ========================================================================
    // 5. COLOCATION (Parties communes)
    // ========================================================================

    public DetailsColocationEntity toDetailsColocationEntity(DetailsSpecifiquesRequest d, BienEntity bien) {
        if (d == null || bien == null) return null;
        if (!hasColocationInfo(d)) return null;
        DetailsColocationEntity e = new DetailsColocationEntity();
        e.setBienId(bien.getId());
        applyColocationFields(d, e);
        return e;
    }

    public void updateDetailsColocationEntity(DetailsSpecifiquesRequest d, DetailsColocationEntity e) {
        if (d == null || e == null) return;
        applyColocationFields(d, e);
    }

    private void applyColocationFields(DetailsSpecifiquesRequest d, DetailsColocationEntity e) {
        if (d.cuisineCommune() != null) e.setCuisineCommune(d.cuisineCommune());
        if (d.salonCommun() != null) e.setSalonCommun(d.salonCommun());
        if (d.sdbCommune() != null) e.setSdbCommune(d.sdbCommune());
        if (d.wcCommun() != null) e.setWcCommun(d.wcCommun());
        if (d.buanderieCommune() != null) e.setBuanderieCommune(d.buanderieCommune());
        if (d.jardinCommun() != null) e.setJardinCommun(d.jardinCommun());
        if (d.terrasseCommune() != null) e.setTerrasseCommune(d.terrasseCommune());
        if (d.descriptionCommunes() != null) e.setDescriptionCommunes(d.descriptionCommunes());
    }

    public boolean hasColocationInfo(DetailsSpecifiquesRequest d) {
        if (d == null) return false;
        return d.cuisineCommune() != null || d.salonCommun() != null
                || d.sdbCommune() != null || d.wcCommun() != null
                || d.buanderieCommune() != null || d.jardinCommun() != null
                || d.terrasseCommune() != null || d.descriptionCommunes() != null;
    }
}