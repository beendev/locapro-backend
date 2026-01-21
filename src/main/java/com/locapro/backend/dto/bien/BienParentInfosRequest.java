package com.locapro.backend.dto.bien;

import com.locapro.backend.domain.context.SousType;
import com.locapro.backend.domain.context.TypeBien;

public record BienParentInfosRequest(
        String nomReferenceInterne,
        String libelleVisible,
        TypeBien typeBien,
        SousType sousType,
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
