package com.locapro.backend.dto.bien;

public record DetailsParkingResponse(
        String numeroPlace,
        Double longueurM,
        Double largeurM,
        String typePorte,
        Boolean priseElectrique
) {}