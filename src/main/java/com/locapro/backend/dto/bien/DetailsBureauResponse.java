package com.locapro.backend.dto.bien;

public record DetailsBureauResponse(
        Double surfaceBureauxM2,
        Integer nbBureauxCloisonnes,
        Boolean salleReunion,
        Boolean cablageInformatique
) {}