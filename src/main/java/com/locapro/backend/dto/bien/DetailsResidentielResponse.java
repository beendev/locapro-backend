package com.locapro.backend.dto.bien;

import com.locapro.backend.entity.DetailsResidentielEntity;

public record DetailsResidentielResponse (
        Double superficieHabitableM2,
        Integer nombreFacades,
        Integer etage,
        Integer anneeConstruction,
        Integer anneeRenovation,
        Integer nbChambres,
        Integer nbSallesBain,
        Integer nbSallesDouche,
        Integer nbWc,
        Boolean hallEntree,
        String typeCuisine,           // NON_EQUIPEE / SEMI_EQUIPEE / EQUIPEE
        String pebClasse,
        Double pebConsoKwhM2An,
        String typeChassis,
        String typeChauffage,
        String electriciteConforme,
        Boolean detecteursFumee,
        Boolean meuble,
        Boolean parlophone,
        Boolean alarme,
        String qualiteSols,
        Boolean jardin,
        Double jardinSurfaceM2,
        Boolean terrasse,
        Double terrasseSurfaceM2,
        Boolean balcon,
        Boolean cave,
        Boolean grenier


)
{ public DetailsResidentielResponse(DetailsResidentielEntity d) {
    this(
            d.getSuperficieHabitableM2(),
            d.getNombreFacades(),
            d.getEtage(),
            d.getAnneeConstruction(),
            d.getAnneeRenovation(),
            d.getNbChambres(),
            d.getNbSallesBain(),
            d.getNbSallesDouche(),
            d.getNbWc(),
            d.getHallEntree(),
            d.getTypeCuisine(),
            d.getPebClasse(),
            d.getPebConsoKwhM2An(),
            d.getTypeChassis(),
            d.getTypeChauffage(),
            d.getElectriciteConforme(),
            d.getDetecteursFumee(),
            d.getMeuble(),
            d.getParlophone(),
            d.getAlarme(),
            d.getQualiteSols(),
            d.getJardin(),
            d.getJardinSurfaceM2(),
            d.getTerrasse(),
            d.getTerrasseSurfaceM2(),
            d.getBalcon(),
            d.getCave(),
            d.getGrenier()
    );
}}
