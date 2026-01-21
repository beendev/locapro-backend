package com.locapro.backend.dto.bien;

import com.locapro.backend.domain.context.SousType;
import com.locapro.backend.domain.context.TypeBien;

public record BienInfosDeBaseRequest(
        String nomReferenceInterne,
        String libelleVisible,
        TypeBien typeBien,
        SousType sousType,
        String lotOuUnite,
        String numeroPorte,
        String boiteUnite,
        Boolean estUniteLocative,

        // Adresse du bien
        String rue,
        String numero,
        String boite,
        String codePostal,
        String ville,
        String pays,
        Double latitude,
        Double longitude
) {}
