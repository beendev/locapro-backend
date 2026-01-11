package com.locapro.backend.dto.bien;

public record ChambreColocCreationRequest(

        // Identification de la chambre
        String nomReferenceInterne,
        String libelleVisible,
        String lotOuUnite,
        String numeroPorte,
        String boiteUnite,

        // Détails propres à la chambre (surface, meublé, etc.)
        DetailsSpecifiquesRequest details
) {}
