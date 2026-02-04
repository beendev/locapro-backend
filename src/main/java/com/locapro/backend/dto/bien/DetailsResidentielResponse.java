package com.locapro.backend.dto.bien;

import com.locapro.backend.entity.DetailsResidentielEntity;

import java.time.LocalDate;

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
        Boolean grenier,
        String pebNumero,
        LocalDate pebDateValidite


)
{}
