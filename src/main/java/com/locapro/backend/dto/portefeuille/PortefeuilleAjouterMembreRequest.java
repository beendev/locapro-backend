package com.locapro.backend.dto.portefeuille;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PortefeuilleAjouterMembreRequest(
        @NotNull Long utilisateurId,
        @Size(min = 1, max = 50)
        String roleDansPortefeuille
) {}
