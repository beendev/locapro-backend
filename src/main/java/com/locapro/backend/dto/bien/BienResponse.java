package com.locapro.backend.dto.bien;

public record BienResponse(

        Long id,
        String nomReference,
        String typeBien,
        String sousType,
        String libelleUnite,
        String codePublic,

        // Adresse affichée
        String rue,
        String numero,
        String boiteAdresse,   // boîte du parent (immeuble / maison)
        String boiteUnite,     // boîte de l’unité (boite_unite)
        String codePostal,
        String ville,
        String pays,

        // Parent
        Long parentId,
        String parentNomReference,
        String parentLibelle,

        // Détails
        DetailsResidentielResponse detailsResidentiel,
        DetailsCommerceResponse detailsCommerce,
        DetailsBureauResponse detailsBureau,
        DetailsParkingResponse detailsParking,

        // Propriétaire
        ProprietaireBienResponse proprietaire

) {}
